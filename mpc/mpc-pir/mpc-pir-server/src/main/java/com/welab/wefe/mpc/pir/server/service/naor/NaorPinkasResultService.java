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

package com.welab.wefe.mpc.pir.server.service.naor;

import cn.hutool.core.util.ObjectUtil;
import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.commom.Conversion;
import com.welab.wefe.mpc.pir.protocol.se.SymmetricKey;
import com.welab.wefe.mpc.pir.protocol.se.aes.AESEncryptKey;
import com.welab.wefe.mpc.pir.request.naor.QueryNaorPinkasResultRequest;
import com.welab.wefe.mpc.pir.request.naor.QueryNaorPinkasResultResponse;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NaorPinkasResultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NaorPinkasResultService.class);

    public QueryNaorPinkasResultResponse handle(QueryNaorPinkasResultRequest request) {
        String uuid = request.getUuid();

        QueryNaorPinkasResultResponse response = new QueryNaorPinkasResultResponse();
        response.setUuid(uuid);
        response.setEncryptResults(process(uuid, request.getPk()));
        return response;
    }

    private List<String> process(String uuid, String pkHexString) {
        CompletableFuture<Map<String, String>> queryResult = CompletableFuture.supplyAsync(() -> queryResult(uuid));

        CacheOperation<String> cacheOperation = CacheOperationFactory.getCacheOperation();
        String aString = cacheOperation.get(uuid, Constants.PIR.NAORPINKAS_A);
        String pString = cacheOperation.get(uuid, Constants.PIR.NAORPINKAS_P);
        BigInteger a = DiffieHellmanUtil.hexStringToBigInteger(aString);
        BigInteger p = DiffieHellmanUtil.hexStringToBigInteger(pString);
        BigInteger pk = DiffieHellmanUtil.hexStringToBigInteger(pkHexString);

        BigInteger enPk = DiffieHellmanUtil.encrypt(pk, a, p);

        CacheOperation<List<String>> operation = CacheOperationFactory.getCacheOperation();
        List<String> randoms = operation.get(uuid, Constants.PIR.NAORPINKAS_RANDOM);
        List<String> conditions = operation.get(uuid, Constants.PIR.NAORPINKAS_CONDITION);

        List<SymmetricKey> keys = randoms.stream().map(DiffieHellmanUtil::hexStringToBigInteger)
                .map(value -> DiffieHellmanUtil.modDivide(value, enPk, p))
                .map(BigInteger::toByteArray)
                .map(value -> new AESEncryptKey(value))
                .collect(Collectors.toList());

        Map<String, String> results = null;
        try {
            results = queryResult.get();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        List<String> enResults = new ArrayList<>(conditions.size());
        for (int i = 0; i < conditions.size(); i++) {
            SymmetricKey aesKey = keys.get(i);
            byte[] enResult = aesKey.encrypt(results.getOrDefault(conditions.get(i), "").getBytes());
            String value = Conversion.bytesToHexString(enResult) + "," + Conversion.bytesToHexString(aesKey.getIv());
            enResults.add(value);
        }
        return enResults;
    }

    private Map<String, String> queryResult(String uuid) {
        CacheOperation<Map<String, String>> queryDataResult = CacheOperationFactory.getCacheOperation();
        Map<String, String> result = queryDataResult.get(uuid, Constants.RESULT);
        while (ObjectUtil.isNull(result)) {
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
            result = queryDataResult.get(uuid, Constants.RESULT);
        }
        LOGGER.info("get result finish");
        return result;
    }
}
