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

import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.key.DiffieHellmanKey;
import com.welab.wefe.mpc.pir.request.QueryKeysRequest;
import com.welab.wefe.mpc.pir.request.naor.QueryNaorPinkasRandomResponse;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class NaorPinkasRandomService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NaorPinkasRandomService.class);

    public QueryNaorPinkasRandomResponse handle(QueryKeysRequest request) {
        List<Object> queryConditions = request.getIds();
        if (queryConditions == null || queryConditions.isEmpty()) {
            throw new IllegalArgumentException("query condition is empty");
        }
        int keySize = 1024;
        DiffieHellmanKey diffieHellmanKey = DiffieHellmanUtil.generateKey(keySize);
        QueryNaorPinkasRandomResponse response = new QueryNaorPinkasRandomResponse();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        response.setUuid(uuid);
        response.setG(DiffieHellmanUtil.bigIntegerToHexString(diffieHellmanKey.getG()));
        response.setP(DiffieHellmanUtil.bigIntegerToHexString(diffieHellmanKey.getP()));

        BigInteger key = DiffieHellmanUtil.generateRandomKey(keySize);
        BigInteger secret = DiffieHellmanUtil.encrypt(key, diffieHellmanKey);
        response.setSecret(DiffieHellmanUtil.bigIntegerToHexString(secret));
        List<String> randoms = new ArrayList<>(queryConditions.size());
        List<BigInteger> bigIntegers = new ArrayList<>(queryConditions.size());
        for (int i = 0; i < queryConditions.size(); i++) {
            BigInteger random = DiffieHellmanUtil.generateRandomKey(keySize);
            bigIntegers.add(random);
            randoms.add(DiffieHellmanUtil.bigIntegerToHexString(random));
        }
        response.setRandoms(randoms);

        new Thread(() -> process(uuid, diffieHellmanKey, key, bigIntegers, queryConditions)).start();

        return response;
    }

    public void process(String uuid, DiffieHellmanKey key, BigInteger a, List<BigInteger> randoms, List<Object> queryConditions) {
        CacheOperation<String> mCacheOperation = CacheOperationFactory.getCacheOperation();
        mCacheOperation.save(uuid, Constants.PIR.NAORPINKAS_P, DiffieHellmanUtil.bigIntegerToHexString(key.getP()));
        mCacheOperation.save(uuid, Constants.PIR.NAORPINKAS_A, DiffieHellmanUtil.bigIntegerToHexString(a));

        CacheOperation<List<String>> cacheOperation = CacheOperationFactory.getCacheOperation();
        List<String> randomExponential = randoms.stream()
                .map(value -> DiffieHellmanUtil.encrypt(value, a, key.getP()))
                .map(DiffieHellmanUtil::bigIntegerToHexString)
                .collect(Collectors.toList());
        List<String> conditions = queryConditions.stream().map(Object::toString).collect(Collectors.toList());

        cacheOperation.save(uuid, Constants.PIR.NAORPINKAS_RANDOM, randomExponential);
        cacheOperation.save(uuid, Constants.PIR.NAORPINKAS_CONDITION, conditions);
        LOGGER.info("{} save params finish", uuid);
    }

}
