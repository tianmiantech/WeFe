/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.data.storage.repo.impl;

import com.alicloud.openservices.tablestore.*;
import com.alicloud.openservices.tablestore.model.*;
import com.alicloud.openservices.tablestore.writer.WriterConfig;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.google.protobuf.ByteString;
import com.welab.wefe.common.data.storage.common.DBType;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.repo.MiddleStorage;
import com.welab.wefe.common.proto.IntermediateDataOuterClass;
import net.razorvine.pickle.Pickler;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ivenn.zheng
 */
@Component
public class FcStorage extends MiddleStorage {
    private static Logger log = LoggerFactory.getLogger(LmdbStorage.class);

    @Value(value = "${fc.ots.instance_name}")
    private String instanceName;

    @Value(value = "${fc.access_key_id}")
    private String accessKeyId;

    @Value(value = "${fc.access_key_secret}")
    private String accessKeySecret;


    /**
     * OTS internal end point
     */
    @Value(value = "${fc.ots.internal_end_point}")
    private String otsInternalEndPoint;

    /**
     * OSS internal end point
     */
    @Value(value = "${fc.oss.internal_end_point:https://oss-cn-shenzhen-internal.aliyuncs.com}")
    private String ossInternalEndPoint;

    @Value(value = "${fc.oss.bucket_name:wefe-fc}")
    private String bucketName;

    private final static String SPLIT_MAX_FREFIX = "MAX_";

    /**
     * byte, split size
     */
    private final static int SPLIT_EACH_SIZE = 1024 * 1024;

    /**
     * max row count per OSS file
     */
    private final static int OBJECT_MAX_DATA_COUNT = 1000;

    /**
     * min row count per OSS file
     */
    private final static int OBJECT_MIN_DATA_COUNT = 500;

    /**
     * max size per OSS file
     */
    private final static int OBJECT_FILE_MAX_SIZE = 1024 * 1024 * 4;

    @Override
    public <K, V> void putAll(List<DataItemModel<K, V>> list, Map<String, Object> args) throws Exception {
        String storageType = args.get("storage_type").toString();
        switch (DBType.valueOf(storageType.toUpperCase())) {
            case OTS:
                otsPutAll(list, args);
                break;
            case OSS:
                ossPutAll(list, args);
                break;
            default:
                break;
        }
    }

    <K, V> void ossPutAll(List<DataItemModel<K, V>> list, Map<String, Object> args) {
        log.info("ossPutAll args: " + args.toString());
        // oss storage args
        String dstNamespace = args.get("fc_namespace").toString();
        String dstName = args.get("fc_name").toString();
        Integer partitions = Integer.parseInt(args.get("fc_partitions").toString());
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        // retry request when error occur, default 3 times
        conf.setMaxErrorRetry(5);
        log.info("ossInternalEndPoint: " + ossInternalEndPoint + " accessKeyId: " + accessKeyId + " accessKeySecret: " + accessKeySecret);
        OSS ossClient = new OSSClientBuilder().build(ossInternalEndPoint, accessKeyId, accessKeySecret, conf);
        ossPutAll(list, bucketName, dstNamespace, dstName, partitions, ossClient);
    }


    <K, V> void otsPutAll(List<DataItemModel<K, V>> list, Map<String, Object> args) {
        // ots storage args
        String dstNamespace = args.get("fc_namespace").toString();
        String dstName = args.get("fc_name").toString();
        Integer partitions = Integer.parseInt(args.get("fc_partitions").toString());
        ClientConfiguration cc = new ClientConfiguration();
        // set retry strategy
        cc.setRetryStrategy(new DefaultRetryStrategy());
        cc.setConnectionTimeoutInMillisecond(60 * 1000);
        AsyncClient asyncClient = new AsyncClient(otsInternalEndPoint, accessKeyId, accessKeySecret, instanceName, cc);

        // init writer config
        WriterConfig config = new WriterConfig();
        // default 4MB
        config.setMaxBatchSize(4 * 1024 * 1024);
        // default 1024 row，must be multiple of 2
        config.setBufferSize(1024);
        // default 100
        config.setMaxBatchRowsCount(200);
        // default 10, cpu * 2
        config.setConcurrency(Runtime.getRuntime().availableProcessors() * 2);
        // default 2MB
        config.setMaxAttrColumnSize(2 * 1024 * 1024);
        // default 1KB
        config.setMaxPKColumnSize(1024);
        // default 10s
        config.setFlushInterval(2000);
        AtomicLong succeedCount = new AtomicLong();
        AtomicLong failedCount = new AtomicLong();
        TableStoreCallback<RowChange, ConsumedCapacity> callback = new SampleCallback(succeedCount, failedCount);
        ExecutorService executor = new ThreadPoolExecutor(8, 8,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),
                new ThreadPoolExecutor.CallerRunsPolicy());
        TableStoreWriter tablestoreWriter = new DefaultTableStoreWriter(asyncClient, dstNamespace, config, callback, executor);
        otsPutAll(list, dstNamespace, dstName, partitions, tablestoreWriter);
    }

    /**
     * split value by SPLIT_EACH_SIZE
     *
     * @param vBytes
     */
    public static List<byte[]> splitValue(byte[] vBytes) {
        int vLen = vBytes.length;
        int listSize = vLen % SPLIT_EACH_SIZE == 0 ? vLen / SPLIT_EACH_SIZE : (vLen / SPLIT_EACH_SIZE) + 1;
        List<byte[]> result = new ArrayList<>(listSize);
        if (vLen >= SPLIT_EACH_SIZE) {
            byte[] b = null;
            for (int i = 0; ; i += SPLIT_EACH_SIZE) {
                if (i + SPLIT_EACH_SIZE > vLen) {
                    b = new byte[vLen - i];
                    System.arraycopy(vBytes, i, b, 0, vLen - i);
                    result.add(b);
                    break;
                } else {
                    b = new byte[SPLIT_EACH_SIZE];
                    System.arraycopy(vBytes, i, b, 0, SPLIT_EACH_SIZE);
                    result.add(b);
                }
            }
        } else {
            byte[] b = new byte[vLen];
            System.arraycopy(vBytes, 0, b, 0, vLen);
            result.add(b);
        }
        return result;
    }


    /**
     * put all data into OSS
     */
    private static <K, V> void ossPutAll(List<DataItemModel<K, V>> list, String bucketName
            , String namespace, String name, Integer partitions, OSS ossClient) {

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

                builderMap.putIfAbsent(partition, IntermediateDataOuterClass.IntermediateData.newBuilder().setDataFlag(1));
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
                    String getOssFileName = getOssFileName(namespace, name, partition, rowCount);
                    log.info("start to upload oss data: " + getOssFileName);
                    Future future = executor.submit(() -> {
                        ossClient.putObject(bucketName, getOssFileName, new ByteArrayInputStream(dataItemList.build().toByteArray()));
                        log.info("data upload succeed：" + getOssFileName);
                    });
                    futures.add(future);
                    rowCountMap.put(partition, 0);
                    // reset byte_map
                    builderMap.put(partition, IntermediateDataOuterClass.IntermediateData.newBuilder().setDataFlag(1));
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
                String getOssFileName = getOssFileName(namespace, name, partition, rowCount);
                Future future = executor.submit(() -> {
                    ossClient.putObject(bucketName, getOssFileName, new ByteArrayInputStream(keyValue.build().toByteArray()));
                    log.info("data upload succeed：" + getOssFileName);
                });
                futures.add(future);
                rowCountMap.put(partition, 0);
                // reset byte_map
                builderMap.put(partition, IntermediateDataOuterClass.IntermediateData.newBuilder().setDataFlag(1));
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
            ossClient.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


//    private static <K, V> void ossPutAll2(List<DataItemModel<K, V>> list, String bucketName
//            , String namespace, String name, Integer partitions, OSS ossClient) {
//
//        // store data per partition
//        Map<Integer, IntermediateDataOuterClass.BatchSerializationData.Builder> builderMap = new HashMap<>(50);
//        // store byte list per partition
//        Map<Integer, Integer> rowCountMap = new HashMap<>(50);
//        // store byte size per partition
//        Map<Integer, Integer> byteSizeMap = new HashMap<>(50);
//        Pickler pickler = new Pickler();
//        try {
//            // take the first piece of data and estimate how much data is stored in each file
//            DataItemModel<K, V> dataItemModel = list.get(0);
//            byte[] firstKey = dataItemModel.getK() instanceof byte[] ? (byte[]) dataItemModel.getK() : pickler.dumps(dataItemModel.getK());
//            byte[] firstValue = dataItemModel.getV() instanceof byte[] ? (byte[]) dataItemModel.getV() : pickler.dumps(dataItemModel.getV());
//            byte[] firstKeyValueByte = toKeyValueByte(firstKey, firstValue);
//
//            // record rows
////            int uploadLinesCount = OBJECT_FILE_MAX_SIZE / firstKeyValueByte.length;
////            if (uploadLinesCount <= OBJECT_MIN_DATA_COUNT) {
////                uploadLinesCount = OBJECT_MIN_DATA_COUNT;
////            } else if (uploadLinesCount >= OBJECT_MAX_DATA_COUNT) {
////                uploadLinesCount = OBJECT_MAX_DATA_COUNT;
////            }
//            int cupCores = Runtime.getRuntime().availableProcessors();
//            ExecutorService executor = new ThreadPoolExecutor(cupCores, cupCores * 2,
//                    0L, TimeUnit.MILLISECONDS,
//                    new LinkedBlockingQueue<Runnable>(1024),
//                    new ThreadPoolExecutor.CallerRunsPolicy());
//
//            List<Future> futures = new ArrayList<>();
//            for (DataItemModel<K, V> item : list) {
//                byte[] key = item.getK() instanceof byte[] ? (byte[]) item.getK() : pickler.dumps(item.getK());
//                byte[] value = item.getV() instanceof byte[] ? (byte[]) item.getV() : pickler.dumps(item.getV());
//                int partition = hashKeyToPartition(key, partitions);
//
//                builderMap.putIfAbsent(partition, IntermediateDataOuterClass.BatchSerializationData.newBuilder());
//                IntermediateDataOuterClass.BatchSerializationData.Builder batchData = builderMap.get(partition);
//
//                // add one row data
////                IntermediateDataOuterClass.IntermediateDataItem.Builder dataItem = IntermediateDataOuterClass.IntermediateDataItem.newBuilder();
//                List<ByteString> byte_k_v = new ArrayList<>();
//                byte_k_v.add(ByteString.copyFrom(key));
//                byte_k_v.add(ByteString.copyFrom(value));
//                batchData.setValue(ByteString.copyFrom(byte_k_v));
//                byteSizeMap.putIfAbsent(partition, 0);
//                int partitionNewSize = byteSizeMap.get(partition) + key.length + value.length;
//                // count rows
//                rowCountMap.putIfAbsent(partition, 0);
//                int rowCount = rowCountMap.get(partition) + 1;
//                // determine whether the upload conditions are met: Less than or equal to 4M，at the same time, 500 < rows count or rows count > 1000
//                if ((partitionNewSize >= OBJECT_FILE_MAX_SIZE && rowCount >= OBJECT_MIN_DATA_COUNT) || rowCount >= OBJECT_MAX_DATA_COUNT) {
//                    String getOssFileName = getOssFileName(namespace, name, partition, rowCount);
//                    log.info("start to upload oss data: " + getOssFileName);
//                    Future future = executor.submit(() -> {
//                        ossClient.putObject(bucketName, getOssFileName, new ByteArrayInputStream(dataItemList.build().toByteArray()));
//                        log.info("data upload succeed：" + getOssFileName);
//                    });
//                    futures.add(future);
//                    rowCountMap.put(partition, 0);
//                    // reset byte_map
//                    builderMap.put(partition, IntermediateDataOuterClass.IntermediateData.newBuilder());
//                    // reset byteSizeMap
//                    byteSizeMap.put(partition, 0);
//                } else {
//                    rowCountMap.put(partition, rowCount);
//                    // add byte size of current partition
//                    byteSizeMap.put(partition, partitionNewSize);
//                    builderMap.put(partition, dataItemList);
//                }
//            }
//            // upload last data
//            for (Map.Entry<Integer, IntermediateDataOuterClass.IntermediateData.Builder> byteEntry : builderMap.entrySet()) {
//                int partition = byteEntry.getKey();
//                IntermediateDataOuterClass.IntermediateData.Builder keyValue = byteEntry.getValue();
//                int rowCount = rowCountMap.get(partition);
//                String getOssFileName = getOssFileName(namespace, name, partition, rowCount);
//                Future future = executor.submit(() -> {
//                    ossClient.putObject(bucketName, getOssFileName, new ByteArrayInputStream(keyValue.build().toByteArray()));
//                    log.info("data upload succeed：" + getOssFileName);
//                });
//                futures.add(future);
//                rowCountMap.put(partition, 0);
//                // reset byte_map
//                builderMap.put(partition, IntermediateDataOuterClass.IntermediateData.newBuilder());
//                // reset byte_size
//                byteSizeMap.put(partition, 0);
//            }
//            // set maps to null
//            builderMap = null;
//            rowCountMap = null;
//            byteSizeMap = null;
//            for (Future future : futures) {
//                try {
//                    future.get();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//            executor.shutdown();
//            ossClient.shutdown();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    /**
     * generate OSS file name
     *
     * @param fileCount
     */
    static String getOssFileName(String namespace, String name, Integer partition, int fileCount) {
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

    /**
     * put all data into OTS table
     */
    protected static <K, V> void otsPutAll(List<DataItemModel<K, V>> list, String namespace, String name, Integer partitions, TableStoreWriter tablestoreWriter) {
        Pickler pickler = new Pickler();
        for (DataItemModel<K, V> item : list) {
            try {
                byte[] key = item.getK() instanceof byte[] ? (byte[]) item.getK() : pickler.dumps(item.getK());
                byte[] value = item.getV() instanceof byte[] ? (byte[]) item.getV() : pickler.dumps(item.getV());
                int partition = hashKeyToPartition(key, partitions);
                List<byte[]> splitValueList = splitValue(value);
                int lengthSplitSize = splitValueList.size();
                for (int i = 0; i < lengthSplitSize; i++) {
                    String splitIndex = "";
                    if (lengthSplitSize > 1) {
                        splitIndex = (i == (lengthSplitSize - 1) ? SPLIT_MAX_FREFIX + i : i + "");
                    }

                    String ossName = (Math.abs((partition + name).hashCode()) % 10007) + "_" + name;

                    PrimaryKey primaryKey = PrimaryKeyBuilder.createPrimaryKeyBuilder()
                            .addPrimaryKeyColumn("name", PrimaryKeyValue.fromString(ossName))
                            .addPrimaryKeyColumn("partition", PrimaryKeyValue.fromLong(partition))
                            .addPrimaryKeyColumn("k", PrimaryKeyValue.fromBinary(key))
                            .addPrimaryKeyColumn("split_index", PrimaryKeyValue.fromString(splitIndex))
                            .build();
                    RowPutChange rowChange = new RowPutChange(namespace);
                    rowChange.setPrimaryKey(primaryKey);
                    rowChange.addColumn("v", ColumnValue.fromBinary(splitValueList.get(i)));
                    tablestoreWriter.addRowChange(rowChange);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tablestoreWriter.flush();
        tablestoreWriter.close();
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


    /**
     * callback
     */
    private static class SampleCallback implements TableStoreCallback<RowChange, ConsumedCapacity> {
        private AtomicLong succeedCount;
        private AtomicLong failedCount;

        public SampleCallback(AtomicLong succeedCount, AtomicLong failedCount) {
            this.succeedCount = succeedCount;
            this.failedCount = failedCount;
        }

        @Override
        public void onCompleted(RowChange req, ConsumedCapacity res) {
            succeedCount.incrementAndGet();
        }

        @Override
        public void onFailed(RowChange req, Exception ex) {
            ex.printStackTrace();
            failedCount.incrementAndGet();
        }

    }


}
