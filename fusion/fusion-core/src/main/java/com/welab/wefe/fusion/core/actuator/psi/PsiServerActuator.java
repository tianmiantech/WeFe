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

package com.welab.wefe.fusion.core.actuator.psi;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;
import com.welab.wefe.fusion.core.utils.CryptoUtils;
import com.welab.wefe.fusion.core.utils.PSIUtils;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hunter.zhao
 */
public abstract class PsiServerActuator extends AbstractPsiActuator {

    protected BigInteger N;
    protected BigInteger d;
    protected BigInteger e;

    public PsiServerActuator(String businessId, BloomFilters bloomFilters, BigInteger N, BigInteger e, BigInteger d) {
        super(businessId);
        this.N = N;
        this.e = e;
        this.d = d;
        this.bf = bloomFilters;
    }

    public PsiActuatorMeta getActuatorParam() {
        return PsiActuatorMeta.of(e, N, bf);
    }

    public byte[][] compute(List<byte[]> bs) {
        LOG.info("align start...");

//        //String 转为二进制
//        byte[][] bs = new byte[value.size()][];
//
//        //加密
//        for (int i = 0; i < value.size(); i++) {
//            byte[] b = Base64Util.base64ToByteArray(value.get(i));
//            bs[i] = b;
//        }

        long start = System.currentTimeMillis();

        try {

            //Encrypted again
            return CryptoUtils.sign(N, d, bs);

//            List<String> resultStr = Lists.newArrayList();
//            for (int i = 0; i < result.length; i++) {
//                resultStr.add(Base64Util.encode(bs[i]));
//            }
//
//            return resultStr;
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
    public void receiveResult(List<byte[]> rs) {

        List<JObject> fruit = new ArrayList<>();
        for (int i = 0; i < rs.size(); i++) {
            fruit.add(JObject.create(new String(rs.get(i))));
        }

        dump(fruit);
    }

    @Override
    public void init() throws StatusCodeWithException {
    }

    @Override
    public void fusion() throws StatusCodeWithException {
    }
}
