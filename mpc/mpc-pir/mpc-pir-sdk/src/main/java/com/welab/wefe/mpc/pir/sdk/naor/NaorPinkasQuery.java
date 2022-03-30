/*
 * Copyright 2022 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.mpc.pir.sdk.naor;

import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.pir.protocol.ro.hf.HashFunction;
import com.welab.wefe.mpc.pir.protocol.ro.hf.Sha256;
import com.welab.wefe.mpc.pir.request.QueryKeysRequest;
import com.welab.wefe.mpc.pir.request.naor.QueryNaorPinkasRandomResponse;
import com.welab.wefe.mpc.pir.request.naor.QueryNaorPinkasResultRequest;
import com.welab.wefe.mpc.pir.request.naor.QueryNaorPinkasResultResponse;
import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;
import com.welab.wefe.mpc.pir.sdk.crypt.CryptUtil;
import com.welab.wefe.mpc.pir.sdk.trasfer.NaorPinkasTransferVariable;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;

import java.math.BigInteger;

public class NaorPinkasQuery {

    public String query(PrivateInformationRetrievalConfig config, NaorPinkasTransferVariable transferVariable) throws Exception {
        return query(config, transferVariable, 1024);
    }

    public String query(PrivateInformationRetrievalConfig config, NaorPinkasTransferVariable transferVariable, int keySize) throws Exception {
        int targetIndex = config.getTargetIndex();
        BigInteger k = DiffieHellmanUtil.generateRandomKey(keySize);
        QueryKeysRequest randomRequest = new QueryKeysRequest();
        randomRequest.setIds(config.getPrimaryKeys());
        randomRequest.setOtMethod(Constants.PIR.NAORPINKAS_OT);
        QueryNaorPinkasRandomResponse randomResponse = transferVariable.queryNaorPinkasRandom(randomRequest);
        if(randomResponse.getCode() != 0) {
            throw new Exception(randomResponse.getMessage());
        }
        String uuid = randomResponse.getUuid();
        BigInteger g = DiffieHellmanUtil.hexStringToBigInteger(randomResponse.getG());
        BigInteger p = DiffieHellmanUtil.hexStringToBigInteger(randomResponse.getP());
        BigInteger secret = DiffieHellmanUtil.hexStringToBigInteger(randomResponse.getSecret());

        BigInteger pk = DiffieHellmanUtil.encrypt(g, k, p);
        if (targetIndex != 0) {
            BigInteger c = DiffieHellmanUtil.hexStringToBigInteger(randomResponse.getRandoms().get(targetIndex - 1));
            pk = DiffieHellmanUtil.modDivide(c, pk, p);
        }
        QueryNaorPinkasResultRequest request = new QueryNaorPinkasResultRequest();
        request.setUuid(uuid);
        request.setPk(DiffieHellmanUtil.bigIntegerToHexString(pk));
        QueryNaorPinkasResultResponse response = transferVariable.queryNaorPinkasResult(request);

        HashFunction hash = new Sha256();
        byte[] key = hash.digest(DiffieHellmanUtil.encrypt(secret, k, p).toByteArray());
        return CryptUtil.decrypt(response.getEncryptResults().get(targetIndex), key);
    }
}
