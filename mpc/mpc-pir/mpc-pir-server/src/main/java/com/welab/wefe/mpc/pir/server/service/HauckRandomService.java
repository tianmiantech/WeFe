
/*
 * *
 *  * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.welab.wefe.mpc.pir.server.service;

import com.welab.wefe.mpc.cache.CacheOperation;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.pir.request.QueryRandomRequest;
import com.welab.wefe.mpc.pir.request.QueryRandomResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author eval
 * @Date 2021/12/14
 **/
@Service
public class HauckRandomService {

    @Autowired
    CacheOperation mCacheOperation;

    public QueryRandomResponse handle(QueryRandomRequest request) {
        QueryRandomResponse response = new QueryRandomResponse();
        String uuid = request.getUuid();
        int attemptCount = request.getAttemptCount();
        String name = Constants.PIR.RANDOM + "_" + attemptCount;
        String result = mCacheOperation.get(uuid, name);
        while (result == null || result.isEmpty()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = mCacheOperation.get(uuid, name);
        }
        response.setUuid(uuid);
        response.setS(result);
        return response;
    }

}
