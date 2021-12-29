
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

package com.welab.wefe.mpc.sa.server.service;

import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.cache.result.QueryDataResult;
import com.welab.wefe.mpc.cache.result.QueryDataResultFactory;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.commom.Operator;
import com.welab.wefe.mpc.sa.request.QuerySAResultRequest;
import com.welab.wefe.mpc.sa.request.QuerySAResultResponse;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

/**
 * @Author eval
 * @Date 2021/12/17
 **/
public class QueryResultService {

    public QuerySAResultResponse handle(QuerySAResultRequest request) {
        return handle(request, 1000);
    }

    public QuerySAResultResponse handle(QuerySAResultRequest request, int factor) {
        CacheOperation<BigInteger> mCacheOperation = CacheOperationFactory.getCacheOperation();
        BigInteger key = mCacheOperation.get(request.getUuid(), Constants.SA.SA_KEY);
        BigInteger p = new BigInteger(request.getP(), 16);

        QueryDataResult<Double> queryDataResult = QueryDataResultFactory.getQueryDataResult();
        Double dataResult = queryDataResult.query(request.getUuid());
        List<String> diffieHellmanValues = request.getDiffieHellmanValues();
        int index = request.getIndex();
        Double result = dataResult;
        if (request.getOperator() == Operator.SUB) {
            result *= -1.0;
        }
        for (int i = 0; i < diffieHellmanValues.size(); i++) {
            if (index == i) {
                continue;
            }
            BigInteger randSeed = DiffieHellmanUtil.encrypt(diffieHellmanValues.get(i), key, p, false);
            Random random = new Random(randSeed.longValue());
            float randomValue = random.nextFloat() * factor;
            if (i < index) {
                result += randomValue;
            } else {
                result -= randomValue;
            }

        }
        QuerySAResultResponse response = new QuerySAResultResponse();
        response.setUuid(request.getUuid());
        response.setResult(result);
        return response;
    }
}
