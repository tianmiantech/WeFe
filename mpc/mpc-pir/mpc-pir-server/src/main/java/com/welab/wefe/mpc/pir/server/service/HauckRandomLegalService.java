
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

package com.welab.wefe.mpc.pir.server.service;

import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.pir.request.QueryRandomLegalRequest;
import com.welab.wefe.mpc.pir.request.QueryRandomLegalResponse;

import java.util.List;

/**
 * @Author eval
 * @Date 2021/12/14
 **/
public class HauckRandomLegalService {

    public QueryRandomLegalResponse handle(QueryRandomLegalRequest request) {
        CacheOperation<String> mCacheOperation = CacheOperationFactory.getCacheOperation();
        String uuid = request.getUuid();
        int attemptCount = request.getAttemptCount();
        boolean sLegal = request.getsLegal();
        String name = Constants.PIR.RANDOM_LEGAL + "_" + attemptCount;
        mCacheOperation.save(uuid, name, Boolean.toString(sLegal));
        if (!request.getR().isEmpty()) {
            String r = request.getR();
            mCacheOperation.save(uuid, Constants.PIR.R, r);
        }

        List<String> results = GetEncryptResultService.getEncryptResult(uuid);

        QueryRandomLegalResponse response = new QueryRandomLegalResponse();
        response.setUuid(uuid);
        if (results != null && !results.isEmpty()) {
            response.setResults(results);
        }
        return response;
    }

}
