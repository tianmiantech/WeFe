/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common.data.storage.service.fc.tencent;

import com.google.protobuf.ByteString;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.welab.wefe.common.data.storage.common.IntermediateDataFlag;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.service.fc.FcStorage;
import com.welab.wefe.common.proto.IntermediateDataOuterClass;
import net.razorvine.pickle.Pickler;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author jacky.jiang
 * @date 2022/8/19
 */
public class TencentCosStorage extends FcStorage {

    private final static String SPLIT_MAX_FREFIX = "MAX_";

    /**
     * byte, split size
     */
    private final static int SPLIT_EACH_SIZE = 1024 * 1024;

    /**
     * max row count per COS file
     */
    private final static int OBJECT_MAX_DATA_COUNT = 1000;

    /**
     * min row count per COS file
     */
    private final static int OBJECT_MIN_DATA_COUNT = 500;

    /**
     * max size per COS file
     */
    private final static int OBJECT_FILE_MAX_SIZE = 1024 * 1024 * 4;

    private TencentCosConfig config;

    public TencentCosStorage(TencentCosConfig config) {
        this.config = config;
    }

    @Override
    public <K, V> void putAll(List<DataItemModel<K, V>> list, Map<String, Object> args) throws Exception {
        cosPutAll(list, args);
    }

    <K, V> void cosPutAll(List<DataItemModel<K, V>> list, Map<String, Object> args) {
        LOG.info("cosPutAll args: " + args.toString());
        // cos storage args
        String dstNamespace = args.get("fc_namespace").toString();
        String dstName = args.get("fc_name").toString();
        Integer partitions = Integer.parseInt(args.get("fc_partitions").toString());
        // 1 初始化用户身份信息（secretId, secretKey）。
        COSCredentials cred = new BasicCOSCredentials(config.accessKeyId, config.accessKeySecret);
        // 2 设置 bucket 的地域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region(config.region);
        ClientConfig clientConfig = new ClientConfig(region);
        // 这里建议设置使用 https 协议
        // 从 5.6.54 版本开始，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);

        LOG.info(" accessKeyId: " + config.accessKeyId + " accessKeySecret: " + config.accessKeySecret);
        cosPutAll(list, config.bucketName, dstNamespace, dstName, partitions, cosClient);
    }


    /**
     * put all data into COS
     */
    private <K, V> void cosPutAll(List<DataItemModel<K, V>> list, String bucketName
            , String namespace, String name, Integer partitions, COSClient cosClient) {

        // store data per partition
        Map<Integer, IntermediateDataOuterClass.IntermediateData.Builder> builderMap = new HashMap<>(50);
        // store byte list per partition
        Map<Integer, Integer> rowCountMap = new HashMap<>(50);
        // store byte size per partition
        Map<Integer, Integer> byteSizeMap = new HashMap<>(50);
        Pickler pickler = new Pickler();
        try {
            // take the first piece of data and estimate how much data is stored in each file
            DataItemModel<K, V> dataItemModel = list.get(0);
            byte[] firstKey = dataItemModel.getK() instanceof byte[] ? (byte[]) dataItemModel.getK() : pickler.dumps(dataItemModel.getK());
            byte[] firstValue = dataItemModel.getV() instanceof byte[] ? (byte[]) dataItemModel.getV() : pickler.dumps(dataItemModel.getV());
            byte[] firstKeyValueByte = toKeyValueByte(firstKey, firstValue);

            // record rows
            int uploadLinesCount = OBJECT_FILE_MAX_SIZE / firstKeyValueByte.length;
            if (uploadLinesCount <= OBJECT_MIN_DATA_COUNT) {
                uploadLinesCount = OBJECT_MIN_DATA_COUNT;
            } else if (uploadLinesCount >= OBJECT_MAX_DATA_COUNT) {
                uploadLinesCount = OBJECT_MAX_DATA_COUNT;
            }
            int cupCores = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = new ThreadPoolExecutor(cupCores, cupCores * 2,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(1024),
                    new ThreadPoolExecutor.CallerRunsPolicy());

            List<Future> futures = new ArrayList<>();
            for (DataItemModel<K, V> item : list) {
                byte[] key = item.getK() instanceof byte[] ? (byte[]) item.getK() : pickler.dumps(item.getK());
                byte[] value = item.getV() instanceof byte[] ? (byte[]) item.getV() : pickler.dumps(item.getV());
                int partition = hashKeyToPartition(key, partitions);

                builderMap.putIfAbsent(partition, IntermediateDataOuterClass.IntermediateData.newBuilder().setDataFlag(IntermediateDataFlag.ITEM_SERIALIZATION));
                IntermediateDataOuterClass.IntermediateData.Builder dataItemList = builderMap.get(partition);

                // add one row data
                IntermediateDataOuterClass.IntermediateDataItem.Builder dataItem = IntermediateDataOuterClass.IntermediateDataItem.newBuilder();
                dataItem.setKey(ByteString.copyFrom(key)).setValue(ByteString.copyFrom(value));
                dataItemList.addIntermediateData(dataItem);
                byteSizeMap.putIfAbsent(partition, 0);
                int partitionNewSize = byteSizeMap.get(partition) + key.length + value.length;
                // count rows
                rowCountMap.putIfAbsent(partition, 0);
                int rowCount = rowCountMap.get(partition) + 1;
                // determine whether the upload conditions are met: Less than or equal to 4M，at the same time, 500 < rows count or rows count > 5000
                if ((partitionNewSize >= OBJECT_FILE_MAX_SIZE && rowCount >= OBJECT_MIN_DATA_COUNT) || rowCount >= OBJECT_MAX_DATA_COUNT) {
                    String getCosFileName = getCosFileName(namespace, name, partition, rowCount);
                    LOG.info("start to upload cos data: " + getCosFileName);
                    Future future = executor.submit(() -> {
                        ObjectMetadata objectMetadata = new ObjectMetadata();
                        objectMetadata.setContentLength(dataItemList.build().toByteArray().length);
                        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, getCosFileName, new ByteArrayInputStream(dataItemList.build().toByteArray()), objectMetadata);
                        cosClient.putObject(putObjectRequest);
                        LOG.info("data upload succeed：" + getCosFileName);
                    });
                    futures.add(future);
                    rowCountMap.put(partition, 0);
                    // reset byte_map
                    builderMap.put(partition, IntermediateDataOuterClass.IntermediateData.newBuilder().setDataFlag(IntermediateDataFlag.ITEM_SERIALIZATION));
                    // reset byteSizeMap
                    byteSizeMap.put(partition, 0);
                } else {
                    rowCountMap.put(partition, rowCount);
                    // add byte size of current partition
                    byteSizeMap.put(partition, partitionNewSize);
                    builderMap.put(partition, dataItemList);
                }
            }
            // upload last data
            for (Map.Entry<Integer, IntermediateDataOuterClass.IntermediateData.Builder> byteEntry : builderMap.entrySet()) {
                int partition = byteEntry.getKey();
                IntermediateDataOuterClass.IntermediateData.Builder keyValue = byteEntry.getValue();
                int rowCount = rowCountMap.get(partition);
                String getCosFileName = getCosFileName(namespace, name, partition, rowCount);
                Future future = executor.submit(() -> {
                    ObjectMetadata objectMetadata = new ObjectMetadata();
                    objectMetadata.setContentLength(keyValue.build().toByteArray().length);
                    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, getCosFileName, new ByteArrayInputStream(keyValue.build().toByteArray()), objectMetadata);

                    cosClient.putObject(putObjectRequest);
                    LOG.info("data upload succeed：" + getCosFileName);
                });
                futures.add(future);
                rowCountMap.put(partition, 0);
                // reset byte_map
                builderMap.put(partition, IntermediateDataOuterClass.IntermediateData.newBuilder().setDataFlag(IntermediateDataFlag.ITEM_SERIALIZATION));
                // reset byte_size
                byteSizeMap.put(partition, 0);
            }
            // set maps to null
            builderMap = null;
            rowCountMap = null;
            byteSizeMap = null;
            for (Future future : futures) {
                try {
                    future.get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            executor.shutdown();
            cosClient.shutdown();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

    }


    /**
     * generate COS file name
     *
     * @param fileCount
     */
    static String getCosFileName(String namespace, String name, Integer partition, int fileCount) {
        return namespace + "/" + name + "/" + partition + "/" + UUID.randomUUID() + "_cnt" + fileCount;
    }


    /**
     * merge key and value into one byte array
     */
    public static byte[] toKeyValueByte(byte[] key, byte[] value) {
        byte[] bt3 = new byte[key.length + value.length + 2];
        System.arraycopy(key, 0, bt3, 0, key.length);
        System.arraycopy("\n".getBytes(StandardCharsets.UTF_8), 0, bt3, key.length, 1);
        System.arraycopy(value, 0, bt3, key.length + 1, value.length);
        System.arraycopy("\n".getBytes(StandardCharsets.UTF_8), 0, bt3, key.length + 1 + value.length, 1);
        return bt3;
    }


    static int hashKeyToPartition(byte[] key, int partitions) throws Exception {
        MessageDigest sha1Digest = DigestUtils.getSha1Digest();
        byte[] tempB = sha1Digest.digest(key);
        BigInteger hashKey = byteArrayToInt(tempB);
        if (partitions < 1) {
            throw new Exception("partitions must be a positive number");
        }
        BigDecimal b = new BigDecimal("-1"), j = new BigDecimal("0");
        BigDecimal tempPartitions = new BigDecimal(String.valueOf(partitions));
        while (j.compareTo(tempPartitions) < 0) {
            b = new BigDecimal(String.valueOf(j.intValue()));
            hashKey = new BigInteger("2862933555777941757").multiply(hashKey).add(new BigInteger("1")).and(new BigInteger("18446744073709551615"));
            BigDecimal left = new BigDecimal(new BigInteger("1").shiftLeft(31).toString());
            BigDecimal right = new BigDecimal(hashKey.shiftRight(33).add(new BigInteger("1")).toString());
            BigDecimal divide = left.divide(right, 10, 5);
            j = b.add(new BigDecimal("1")).multiply(divide);
        }
        return b.intValue();
    }


    public static BigInteger byteArrayToInt(byte[] array) {
        byte[] lowArray = Arrays.copyOfRange(array, 0, array.length);
        //The default byte order in java is big_endian, use the tool class to change the byte array to little_endian
        ArrayUtils.reverse(lowArray);
        return new BigInteger(1, lowArray);
    }
}
