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

package com.welab.wefe.mpc.pir.server.service;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.commom.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetEncryptResultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetEncryptResultService.class);

    public static List<String> getEncryptResult(String uuid) {
        return getEncryptResult(uuid, false);
    }

    public static List<String> getEncryptResult(String uuid, long timeout) {
        return getEncryptResult(uuid, timeout, false);
    }

    public static List<String> getEncryptResult(String uuid, boolean needData) {
        return getEncryptResult(uuid, 60, needData);
    }

    /**
     * 获取查询加密结果数据
     *
     * @param uuid     匿踪查询请求id
     * @param timeout  超时时间，当needData为false时生效
     * @param needData 是否必须有数据返回
     * @return 加密结果数据
     */
    public static List<String> getEncryptResult(String uuid, long timeout, boolean needData) {
        CacheOperation<String> operation = CacheOperationFactory.getCacheOperation();
        long startTime = System.currentTimeMillis() / 1000;
        String result = operation.get(uuid, Constants.ENCRYPT_RESULT);
        while (result == null || result.isEmpty()) {
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
            result = operation.get(uuid, Constants.ENCRYPT_RESULT);
            long cost = System.currentTimeMillis() / 1000 - startTime;
            if (!needData && cost > timeout) {
                break;
            }
        }
        if (result == null || result.isEmpty()) {
            return null;
        }
        return JSON.parseArray(result, String.class);
    }
}
