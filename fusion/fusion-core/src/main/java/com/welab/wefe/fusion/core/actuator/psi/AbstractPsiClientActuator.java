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

package com.welab.wefe.fusion.core.actuator.psi;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;
import com.welab.wefe.fusion.core.enums.PSIActuatorStatus;
import com.welab.wefe.fusion.core.utils.PSIUtils;


/**
 * @author hunter.zhao
 */
public abstract class AbstractPsiClientActuator extends AbstractPsiActuator implements PsiClientInterface {

    protected String dataSetId;
    protected Boolean isTrace;
    protected String traceColumn;

    private PsiActuatorMeta psiClientMeta;

    public AbstractPsiClientActuator(String businessId, String dataSetId, Boolean isTrace, String traceColumn, Long dataCount) {
        super(businessId);
        this.dataSetId = dataSetId;
        this.isTrace = isTrace;
        this.traceColumn = traceColumn;
        this.dataCount = dataCount;
    }


    /**
     * Begin to fusion
     */
    @Override
    public void fusion() throws Exception {

        init();

        while (!isServerReady(businessId)) {
            ThreadUtil.sleep(2000);
        }

        psiClientMeta = downloadActuatorMeta();
        int bucketSize = bucketSize();
//        CountDownLatch latch = new CountDownLatch(bucketSize);
        
//        for (int j = 0; j < bucketSize; j++) {
//            int index = j;
//            CommonThreadPool.run(() -> execute(index), latch);
//        }

//        latch.await();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int j = 0; j < bucketSize; j++) {
            final int index = j;
            executorService.submit(() -> {
                execute(index);
            });
        }
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // pass
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        status = PSIActuatorStatus.success;
        notifyServerClose();
    }

    private void execute(int index) {
        List<JObject> data = getBucketByIndex(index);

        List<BigInteger> rInv = new ArrayList<>();
        byte[][] bs = new byte[data.size()][16];

        //数据预处理
        for (int i = 0; i < data.size(); i++) {
            BigInteger blindFactor = this.generateBlindingFactor();
            BigInteger random = this.getRandom(blindFactor);
            rInv.add(blindFactor.modInverse(psiClientMeta.getN()));

            String key = hashValue(data.get(i));
            BigInteger h = PSIUtils.stringToBigInteger(key);
            BigInteger x = h.multiply(random).mod(psiClientMeta.getN());

            bs[i] = PSIUtils.bigIntegerToBytes(x, false);
        }

        byte[][] result = new byte[0][];
        try {
            result = dataTransform(bs);
        } catch (StatusCodeWithException e) {
            e.printStackTrace();
            LOG.error("slice：{} fusion error: {}", e.getMessage());
        }
        LOG.info("psi log, dataTransform result size = " + result.length);

        //matching
        List<JObject> fruit = this.parseAndMatch(result, data, rInv);
        LOG.info("psi log, parseAndMatch result size = " + fruit.size());
        dump(fruit);

        sendFusionDataToServer(fruit);
    }

    private BigInteger getRandom(BigInteger blindFactor) {
        return blindFactor.modPow(psiClientMeta.getE(), psiClientMeta.getN());
    }

    /**
     * Receives encrypted data, parses and matches
     */
    private List<JObject> parseAndMatch(byte[][] ret, List<JObject> cur, List<BigInteger> rInv) {

        LOG.info("psi log, client start parse data...");

        List<JObject> fruit = new ArrayList<>();
        for (int i = 0; i < ret.length; i++) {
            BigInteger y = PSIUtils.bytesToBigInteger(ret[i], 0, ret[i].length);
            BigInteger z = y.multiply(rInv.get(i)).mod(psiClientMeta.getN());

            if (psiClientMeta.getBf().contains(z)) {
                fruit.add(cur.get(i));
                fusionCount.increment();
            }
            processedCount.increment();
        }
        return fruit;
    }

    private BigInteger generateBlindingFactor() {
        BigInteger zero = BigInteger.valueOf(0);
        BigInteger one = BigInteger.valueOf(1);
        int length = psiClientMeta.getN().bitLength() - 1;
        BigInteger gcd;
        BigInteger blindFactor = new BigInteger(length, new SecureRandom());
        do {
            gcd = blindFactor.gcd(psiClientMeta.getN());
        }
        while (blindFactor.equals(zero) || blindFactor.equals(one) || !gcd.equals(one));

        return blindFactor;
    }
}
