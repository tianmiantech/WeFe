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

package com.welab.wefe.board.service.service.data_resource.add;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.repository.data_resource.BloomFilterRepository;
import com.welab.wefe.board.service.service.data_resource.DataResourceUploadTaskService;
import com.welab.wefe.board.service.service.data_resource.bloom_filter.BloomFilterStorageService;
import com.welab.wefe.board.service.service.fusion.FieldInfoService;
import com.welab.wefe.board.service.util.AbstractBloomFilterReader;
import com.welab.wefe.board.service.util.primarykey.FieldInfo;
import com.welab.wefe.board.service.util.primarykey.PrimaryKeyUtils;
import com.welab.wefe.board.service.util.unique.AbstractDataSetUniqueFilter;
import com.welab.wefe.board.service.util.unique.DataSetBloomUniqueFilter;
import com.welab.wefe.board.service.util.unique.DataSetMemoryUniqueFilter;
import com.welab.wefe.common.BatchConsumer;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.fusion.core.utils.CryptoUtils;
import com.welab.wefe.fusion.core.utils.PSIUtils;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

/**
 * @author jacky.jiang
 */
public class BloomFilterAddServiceDataRowConsumer implements Consumer<LinkedHashMap<String, Object>> {
    private final Logger LOG = LoggerFactory.getLogger(BloomFilterAddServiceDataRowConsumer.class);

    protected Config config;
    //region construction parameters
    private BloomFilterRepository bloomFilterRepository;

    /**
     * bloom_filter id
     */
    private String bloomfilterId;
    /**
     * Do you need to de-duplicate
     */
    private boolean deduplication;

    private AsymmetricCipherKeyPair keyPair;

    private BloomFilters bf;

    private RSAKeyParameters rsaPK;

    private BigInteger rsaE;

    private BigInteger rsaN;

    private RSAPrivateCrtKeyParameters rsaSK;

    private BigInteger rsaD;

    private BigInteger rsaP;

    private BigInteger rsaQ;

    private BigInteger cp;

    private BigInteger cq;

    public List<FieldInfo> fieldInfoList;

    private Integer processCount = 0;

    private Integer totalDataCount = 0;

    private String bloomfilterPath;

    //endregion

    /**
     * To increase the writing speed, batch processing is used.
     */
    private BatchConsumer<LinkedHashMap<String, Object>> batchConsumer;
    private int maxBatchSize = 0;
    /**
     * deduplication filter
     */
    private AbstractDataSetUniqueFilter uniqueFilter;
    private BloomFilterStorageService bloomfilterStorageService;
    private DataResourceUploadTaskService dataResourceUploadTaskService;
    private AbstractBloomFilterReader bloomfilterReader;

    /**
     * The number of duplicate data in the primary key
     */
    private final LongAdder repeatDataCount = new LongAdder();

    public BloomFilterAddServiceDataRowConsumer(BloomFilterMysqlModel model, boolean deduplication, AbstractBloomFilterReader bloomfilterReader) throws StatusCodeWithException {
        this.bloomfilterId = model.getId();
        this.deduplication = deduplication;
        this.bloomfilterReader = bloomfilterReader;
        this.keyPair = CryptoUtils.generateKeys(1024);
        this.totalDataCount = (int) bloomfilterReader.getTotalDataRowCount();
        this.bf = new BloomFilters(0.001, totalDataCount);
        this.rsaPK = (RSAKeyParameters) keyPair.getPublic();
        this.rsaE = rsaPK.getExponent();
        this.rsaN = rsaPK.getModulus();
        this.rsaSK = (RSAPrivateCrtKeyParameters) keyPair.getPrivate();
        this.rsaD = rsaSK.getExponent();
        this.rsaP = rsaSK.getP();
        this.rsaQ = rsaSK.getQ();
        this.cp = rsaQ.modInverse(rsaP).multiply(rsaQ);
        this.cq = rsaP.modInverse(rsaQ).multiply(rsaP);

        if (deduplication) {
            this.uniqueFilter = createUniqueFilter(bloomfilterReader.getTotalDataRowCount());
        }
        this.config = Launcher.CONTEXT.getBean(Config.class);
        this.bloomfilterStorageService = Launcher.CONTEXT.getBean(BloomFilterStorageService.class);
        this.dataResourceUploadTaskService = Launcher.CONTEXT.getBean(DataResourceUploadTaskService.class);
        this.bloomFilterRepository = Launcher.CONTEXT.getBean(BloomFilterRepository.class);
        FieldInfoService service = Launcher.CONTEXT.getBean(FieldInfoService.class);
        this.fieldInfoList = service.fieldInfoList(bloomfilterId);
        File outFile = model.file();


        model.setRsaD(this.rsaD.toString());
        model.setRsaN(this.rsaN.toString());
        model.setRsaE(this.rsaE.toString());
        model.setRsaP(this.rsaP.toString());
        model.setRsaQ(this.rsaQ.toString());
        model.setTotalDataCount(this.totalDataCount);
        this.bloomFilterRepository.save(model);

        this.bloomfilterPath = outFile.getPath();
        batchConsumer = new BatchConsumer<>(10, 1_000, rows -> {
            try {
                generateFilter(bloomfilterId, rows);
                // update bloom_filter upload progress
                dataResourceUploadTaskService.updateProgress(
                        bloomfilterId,
                        bloomfilterReader.getTotalDataRowCount(),
                        bloomfilterReader.getReadDataRows(),
                        getRepeatDataCount()
                );
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                dataResourceUploadTaskService.onError(bloomfilterId, e);
            }

        });

    }

    /**
     * Generating filter
     */
    public void generateFilter(String bloomfilterId, List<LinkedHashMap<String, Object>> rows) throws IOException {

        for (LinkedHashMap<String, Object> data : rows) {
            try {
                String key = PrimaryKeyUtils.create(JObject.create(data), fieldInfoList);
                BigInteger h = PSIUtils.stringToBigInteger(key);
                //优化前加密方法
//                BigInteger z = h.modPow(rsaD, rsaN);

                //crt优化后
                BigInteger rp = h.modPow(rsaD.remainder(rsaP.subtract(BigInteger.valueOf(1))), rsaP);
                BigInteger rq = h.modPow(rsaD.remainder(rsaQ.subtract(BigInteger.valueOf(1))), rsaQ);

                BigInteger z = (rp.multiply(cp).add(rq.multiply(cq))).remainder(rsaN);

                this.bf.add(z);
                this.processCount = this.processCount + 1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FileOutputStream outputStream = new FileOutputStream(this.bloomfilterPath);
        this.bf.writeTo(outputStream);
        outputStream.close();

    }


    @Override
    public void accept(LinkedHashMap<String, Object> row) {
        // In order to enable the upload progress bar to start as soon as possible,
        // the initial batch size is set to be smaller.
        if (bloomfilterReader.getReadDataRows() < 100) {
            batchConsumer.setMaxBatchSize(50);
        } else if (bloomfilterReader.getReadDataRows() < 1000) {
            batchConsumer.setMaxBatchSize(100);
        }
        // Later processing according to reasonable batch size
        else {
            // Update the batch size of batch write according to the number of columns
            if (this.maxBatchSize < 1) {
                this.maxBatchSize = bloomfilterStorageService.getAddBatchSize(row.size());
                batchConsumer.setMaxBatchSize(this.maxBatchSize);
            }
        }

        // Save the data row

        batchConsumer.add(row);

    }

    /**
     * Wait for the consumption queue to finish
     */
    public void waitForFinishAndClose() {
        batchConsumer.waitForFinishAndClose();
    }

    /**
     * The count of data duplicated by the primary key
     */
    public long getRepeatDataCount() {
        return repeatDataCount.longValue();
    }


    /**
     * Save data to storage and ensure that the data is not duplicated
     */
    private void saveRowWithDeduplication(List<Object> row) {
//        String id = String.valueOf(row.get(0));
//
//        ContainResult containResult = uniqueFilter.contains(id);
//        while (true) {
//            switch (containResult) {
//                // Already exists: discard duplicate data
//                case In:
//                    repeatDataCount.increment();
//                    return;
//
//                // Does not exist, write
//                case NotIn:
//                    batchConsumer.add(row);
//                    return;
//
//                // Not sure: Wait for the data written in the queue to be written to confirm the query
//                case MaybeIn:
//                    // Waiting for all data in the queue to be written to storage
//                    batchConsumer.waitForClean();
//
//                    // Query in the storage to confirm whether it exists
//                    containResult = bloomfilterStorageService.containsKey(bloomfilterId, id)
//                            ? ContainResult.In
//                            : ContainResult.NotIn;
//                    continue;
//
//                default:
//                    return;
//            }
//        }
    }

    /**
     * Create a deduplication filter
     */
    private AbstractDataSetUniqueFilter createUniqueFilter(long totalDataRowCount) {

        // Use memory filters when the amount of data is small
        if (totalDataRowCount > 100_000) {
            return new DataSetBloomUniqueFilter(totalDataRowCount);
        } else {
            return new DataSetMemoryUniqueFilter();
        }
    }

}
