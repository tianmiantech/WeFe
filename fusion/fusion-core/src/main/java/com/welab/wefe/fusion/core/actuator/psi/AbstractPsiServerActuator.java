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
import java.util.ArrayList;
import java.util.List;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Base64Util;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;
import com.welab.wefe.fusion.core.utils.CryptoUtils;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;

/**
 * @author hunter.zhao
 */
public abstract class AbstractPsiServerActuator extends AbstractPsiActuator implements PsiServerInterface {

    protected BigInteger n;
    protected BigInteger d;
    protected BigInteger e;
    protected BigInteger p;
    protected BigInteger q;
    protected BigInteger cp;
    protected BigInteger cq;
    protected BloomFilters bf;

    public AbstractPsiServerActuator(String businessId, BloomFilters bloomFilters, BigInteger n, BigInteger e, BigInteger d, BigInteger p, BigInteger q, Long dataCount) {
        super(businessId);
        this.n = n;
        this.e = e;
        this.d = d;
        this.p = p;
        this.q = q;
        this.cp = q.modInverse(p).multiply(q);
        this.cq = p.modInverse(q).multiply(p);
        this.bf = bloomFilters;
        this.dataCount = dataCount;
    }

    @Override
    public PsiActuatorMeta getActuatorParam() {
        return PsiActuatorMeta.of(e, n, bf);
    }

    @Override
    public byte[][] dataTransform(List<String> bsList) {

        LOG.info("psi log, align start...bsList size = " + bsList.size());

        byte[][] bs = new byte[bsList.size()][];

        //加密
        for (int i = 0; i < bsList.size(); i++) {
            bs[i] = Base64Util.base64ToByteArray(bsList.get(i));
        }
        // 由于gateway的重试机制，可能会导致相同的请求会收到两次，会影响这个值的计算
        processedCount.add(bsList.size());
        if (processedCount.longValue() > dataCount) {
            processedCount.reset();
            processedCount.add(dataCount);
            LOG.info("psi log, processedCount bigger than dataCount, reset");
        }
        try {
            //Encrypted again
            return CryptoUtils.sign(n, d, p, q, cp, cq, bs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 接收结果
     *
     * @param rs
     */
    @Override
    public void receiveResult(List<String> rs) {
        LOG.info("psi log, receiveResult size = " + rs.size());
        fusionCount.add(rs.size());
        // 由于gateway的重试机制，可能会导致相同的请求会收到两次，会影响这个值的计算
        if (fusionCount.longValue() > dataCount) {
            fusionCount.reset();
            fusionCount.add(dataCount);
            LOG.info("psi log, fusionCount bigger than dataCount, reset");
        }
        List<JObject> fruit = new ArrayList<>();
        for (int i = 0; i < rs.size(); i++) {
            fruit.add(JObject.create(new String(Base64Util.base64ToByteArray(rs.get(i)))));
        }

        dump(fruit);
    }

    @Override
    public void fusion() throws StatusCodeWithException {
    }
}
