
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

import com.welab.wefe.mpc.cache.CacheOperation;
import com.welab.wefe.mpc.cache.impl.LocalCache;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyRequest;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyResponse;

import java.math.BigInteger;
import java.util.Random;

/**
 * @Author eval
 * @Date 2021/12/17
 **/
public class QueryDiffieHellmanKeyService {

    CacheOperation mCacheOperation = new LocalCache();

    public QueryDiffieHellmanKeyResponse handle(QueryDiffieHellmanKeyRequest request) {
        Random rand = new Random();
        BigInteger random = new BigInteger(1024, rand);
        mCacheOperation.put(request.getUuid(), "sa_key", random.toString(16));
        mCacheOperation.put(request.getUuid(), "sa_mod", request.getP());
        mCacheOperation.put(request.getUuid(), "sa_index", request.getIndex()+"");
        BigInteger p = new BigInteger(request.getP(), 16);
        BigInteger g = new BigInteger(request.getG(), 16);
        BigInteger result = g.modPow(random, p);
        QueryDiffieHellmanKeyResponse response = new QueryDiffieHellmanKeyResponse();
        response.setDiffieHellmanValue(result.toString(16));
        response.setUuid(request.getUuid());
        return response;
    }
}
