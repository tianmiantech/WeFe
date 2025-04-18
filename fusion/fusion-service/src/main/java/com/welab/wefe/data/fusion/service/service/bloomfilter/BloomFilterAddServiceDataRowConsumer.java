/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.data.fusion.service.service.bloomfilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.BatchConsumer;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.database.entity.BloomFilterMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.BloomFilterRepository;
import com.welab.wefe.data.fusion.service.enums.Progress;
import com.welab.wefe.data.fusion.service.service.FieldInfoService;
import com.welab.wefe.data.fusion.service.utils.bf.BloomFilters;
import com.welab.wefe.data.fusion.service.utils.primarykey.FieldInfo;
import com.welab.wefe.data.fusion.service.utils.primarykey.PrimaryKeyUtils;
import com.welab.wefe.fusion.core.utils.CryptoUtils;
import com.welab.wefe.fusion.core.utils.PSIUtils;

/**
 * @author jacky.jiang
 */
public class BloomFilterAddServiceDataRowConsumer implements Consumer<Map<String, Object>> {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BloomFilterRepository bloomFilterRepository;

    private String src;

    private AsymmetricCipherKeyPair keyPair;

    private BloomFilters<BigInteger> bf;

    private RSAKeyParameters pk;

    private BigInteger e;

    private BigInteger N;

    private RSAPrivateCrtKeyParameters sk;

    private BigInteger d;

    public List<FieldInfo> fieldInfoList;

    private Integer processCount = 0;

    private AtomicInteger checkCount = new AtomicInteger(0);

    private Progress process;

    private List<Object> CheckData = new CopyOnWriteArrayList<>();

    private BloomFilterMySqlModel model;

    /**
     * To speed up writing, use batch processing.
     */
    private BatchConsumer<Map<String, Object>> batchConsumer;


    public BloomFilters<BigInteger> getBf() {
        return bf;
    }

    public void setBf(BloomFilters<BigInteger> bf) {
        this.bf = bf;
    }

    public BigInteger getE() {
        return e;
    }

    public void setE(BigInteger e) {
        this.e = e;
    }

    public BigInteger getN() {
        return N;
    }

    public void setN(BigInteger n) {
        N = n;
    }

    public BigInteger getD() {
        return d;
    }

    public void setD(BigInteger d) {
        this.d = d;
    }

    public Integer getProcessCount() {
        return processCount;
    }

    public void setProcessCount(Integer processCount) {
        this.processCount = processCount;
    }

    public Progress getProcess() {
        return process;
    }

    public void setProcess(Progress process) {
        this.process = process;
    }

    public List<Object> getCheckData() {
        return CheckData;
    }

    public BloomFilterMySqlModel getModel() {
        return model;
    }


    public BloomFilterAddServiceDataRowConsumer(BloomFilterMySqlModel model, File file) {
        if (model.getProcessCount() != 0) {
            this.processCount = model.getProcessCount();
        }
        this.process = Progress.Ready;
        this.src = model.getSrc();
        this.keyPair = CryptoUtils.generateKeys(1024);
        this.bf = new BloomFilters<BigInteger>(0.00001, model.getRowCount());
        this.pk = (RSAKeyParameters) keyPair.getPublic();
        this.e = pk.getExponent();
        this.N = pk.getModulus();
        this.sk = (RSAPrivateCrtKeyParameters) keyPair.getPrivate();
        this.d = sk.getExponent();
        FieldInfoService service = Launcher.CONTEXT.getBean(FieldInfoService.class);
        this.fieldInfoList = service.fieldInfoList(model.getId());
        this.checkCount = new AtomicInteger(0);;
        this.CheckData.clear();

        model.setD(getD().toString());
        model.setN(getN().toString());
        model.setE(getE().toString());
        model.setSrc(this.src);
        model.setProcess(this.process);
        this.bloomFilterRepository = Launcher.CONTEXT.getBean(BloomFilterRepository.class);
        this.bloomFilterRepository.save(model);

        batchConsumer = new BatchConsumer<>(5000000, 10_000, rows -> {
            this.process = Progress.Running;
            bloomFilterRepository.updateById(model.getId(), "process", this.process, BloomFilterMySqlModel.class);
            try {
                generateFilter(model, rows);
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }
        });
    }


    @Override
    public void accept(Map<String, Object> data) {
        batchConsumer.add(data);
    }
    
    /**
     * Generating filter
     */
    public void generateFilter(BloomFilterMySqlModel model, List<Map<String, Object>> rows) throws IOException {
        long start = System.currentTimeMillis();
        LOG.info("generateFilter begin , size = " + rows.size() + ", id = " + model.getId());
        // 创建定长线程池
        int poolSize = 120;
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(poolSize);
        final BloomFilterAddServiceDataRowConsumer consumer = this;
        for (Map<String, Object> data : rows) {
            try {
                // 创建任务
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        String key = PrimaryKeyUtils.create(JObject.create(data), fieldInfoList);
                        BigInteger h = PSIUtils.stringToBigInteger(key);
                        BigInteger z = h.modPow(d, N);
                        consumer.addToBf(z);
                        if (consumer.getCheckCount().get() <= 10) {
                            consumer.getCheckData().add(data);
                            consumer.getCheckCount().incrementAndGet();
                        }
                    }
                };
                // 将任务交给线程池管理
                newFixedThreadPool.execute(runnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        newFixedThreadPool.shutdown();
        try {
            while (!newFixedThreadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                // pass
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        newFixedThreadPool = null;
        long duration = System.currentTimeMillis() - start;
        LOG.info("generateFilter duration = " + duration + ", size = " + rows.size() + ", id = " + model.getId());
        this.processCount = this.processCount + rows.size();
        LOG.info("processCount = " + this.processCount + ", rowCount = " + model.getRowCount());
        bloomFilterRepository.updateById(model.getId(), "processCount", this.processCount, BloomFilterMySqlModel.class);
        if (processCount >= model.getRowCount()) {
            bloomFilterRepository.updateById(model.getId(), "processCount", this.processCount,
                    BloomFilterMySqlModel.class);
            bloomFilterRepository.updateById(model.getId(), "process", Progress.Success, BloomFilterMySqlModel.class);
            LOG.info("save bloomfilter in file, this.src = " + this.src + ", bf size = " + this.bf.size());
            FileOutputStream outputStream = new FileOutputStream(this.src);
            this.bf.writeTo(outputStream);
            outputStream.close();
        }
        long duration2 = System.currentTimeMillis() - start;
        LOG.info("generateFilter end duration = " + duration2 + ", id = " + model.getId());
    }


    /**
     * Generating filter
     */
    public void generateFilterSlown(BloomFilterMySqlModel model, List<Map<String, Object>> rows) throws IOException {
        long start = System.currentTimeMillis();
        LOG.info("generateFilter begin , size = " + rows.size());
        for (Map<String, Object> data : rows) {
            try {
                String key = PrimaryKeyUtils.create(JObject.create(data), fieldInfoList);
                BigInteger h = PSIUtils.stringToBigInteger(key);
                BigInteger z = h.modPow(d, N);
                this.bf.add(z);
                if (this.getCheckCount().get() <= 10) {
                    this.getCheckData().add(data);
                    this.getCheckCount().incrementAndGet();
                }
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }
        }
        long duration1 = System.currentTimeMillis() - start;
        LOG.info("generateFilter duration1 = " + duration1); // generateFilter duration1 = 165382ms 3min
        this.processCount = this.processCount + rows.size();

        BloomFilterMySqlModel bloomFilterMySqlModel = bloomFilterRepository.findOne("id", model.getId(), BloomFilterMySqlModel.class);
        int count = bloomFilterMySqlModel.getProcessCount(); //当前consumer处理量
        if (processCount >= count) {
            bloomFilterRepository.updateById(model.getId(), "processCount", this.processCount, BloomFilterMySqlModel.class);
            bloomFilterRepository.updateById(model.getId(), "process", Progress.Success, BloomFilterMySqlModel.class);

            FileOutputStream outputStream = new FileOutputStream(this.src);
            this.bf.writeTo(outputStream);
            outputStream.close();
        }

    }

    /**
     * Wait for the consumption queue to complete
     */
    public void waitForFinishAndClose() {
        batchConsumer.waitForFinishAndClose();
    }
    
    public AtomicInteger getCheckCount() {
        return checkCount;
    }

    public void setCheckCount(AtomicInteger checkCount) {
        this.checkCount = checkCount;
    }

    public void setCheckData(List<Object> checkData) {
        CheckData = checkData;
    }
    
    public synchronized void addToBf(BigInteger z) {
        this.bf.add(z);
    }

}
