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

import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;
import com.welab.wefe.fusion.core.enums.PSIActuatorStatus;
import com.welab.wefe.fusion.core.utils.PSIUtils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author hunter.zhao
 */
public abstract class AbstractPsiClientActuator extends AbstractPsiActuator {

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
     * Paging data fetching
     *
     * @return
     * @throws StatusCodeWithException
     */
    public abstract List<JObject> next();

    /**
     * Determine whether there is still data
     *
     * @return
     */
    public abstract Boolean hasNext();

    /**
     * Determine whether there is still data
     *
     * @return
     */
    public abstract Integer sliceNumber();

    /**
     * Download the Server Square Bloom filter
     *
     * @return
     */
    public abstract PsiActuatorMeta downloadBloomFilter() throws StatusCodeWithException;

    /**
     * Download the Server Square Bloom filter
     *
     * @param bs
     * @return
     * @throws StatusCodeWithException
     */
    public abstract byte[][] queryFusionData(byte[][] bs) throws StatusCodeWithException;

    /**
     * Download the Server Square Bloom filter
     *
     * @param rs
     */
    public abstract void sendFusionData(List<byte[]> rs);

    /**
     * Primary key hash processing
     *
     * @param value
     * @return
     */
    public abstract String hashValue(JObject value);

    /**
     * Alignment data into the library implementation method
     */
    public abstract void notifyServerClose();

    /**
     * Begin to fusion
     */
    @Override
    public void fusion() throws StatusCodeWithException, InterruptedException {
        //拉取bf
        psiClientMeta = downloadBloomFilter();

        CountDownLatch latch = new CountDownLatch(sliceNumber());

        for (int j = 0; j < sliceNumber(); j++) {
            CommonThreadPool.run(
                    () -> {
                        //取数
                        List<JObject> data = next();

                        List<BigInteger> r = new ArrayList<>();
                        List<BigInteger> rInv = new ArrayList<>();
                        byte[][] bs = new byte[data.size()][16];

                        //加密
                        for (int i = 0; i < data.size(); i++) {

                            String key = hashValue(data.get(i));

                            BigInteger h = PSIUtils.stringToBigInteger(key);
                            BigInteger blindFactor = generateBlindingFactor();
                            r.add(blindFactor.modPow(psiClientMeta.getE(), psiClientMeta.getN()));
                            rInv.add(blindFactor.modInverse(psiClientMeta.getN()));
                            BigInteger x = h.multiply(r.get(i)).mod(psiClientMeta.getN());
                            bs[i] = PSIUtils.bigIntegerToBytes(x, false);
                        }

                        //发送
                        byte[][] result = new byte[0][];
                        try {
                            result = queryFusionData(bs);
                        } catch (StatusCodeWithException e) {
                            e.printStackTrace();
                            LOG.error("slice：{} fusion error: {}", e.getMessage());
                        }

                        //matching
                        List<JObject> fruit = receiveAndParseResult(result, data, rInv);

                        //dump
                        dump(fruit);
                    },
                    latch
            );

        }

        latch.await();

        status = PSIActuatorStatus.success;
    }

    /**
     * Receives encrypted data, parses and matches
     */
    private List<JObject> receiveAndParseResult(byte[][] ret, List<JObject> cur, List<BigInteger> rInv) {

        LOG.info("client start receive data...");

        List<byte[]> rs = new ArrayList<>();
        List<JObject> fruit = new ArrayList<>();
        for (int i = 0; i < ret.length; i++) {

            BigInteger y = PSIUtils.bytesToBigInteger(ret[i], 0, ret[i].length);
            BigInteger z = y.multiply(rInv.get(i)).mod(psiClientMeta.getN());

            if (psiClientMeta.getBf().contains(z)) {
                rs.add(cur.get(i).toString().getBytes());
                fruit.add(cur.get(i));
                fusionCount.increment();
            }
            processedCount.increment();
        }


        LOG.info("fusionCount: " + fusionCount.longValue());

        LOG.info("processedCount: " + processedCount.longValue());

        /**
         * Send alignment data to the server
         */
        sendFusionData(rs);

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
