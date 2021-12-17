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

package com.welab.wefe.mpc.pir.server.trasfer.impl;

import com.welab.wefe.mpc.cache.CacheOperation;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.pir.server.trasfer.PrivateInformationRetrievalTransferVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheTransferVariable implements PrivateInformationRetrievalTransferVariable {
    private static final Logger LOG = LoggerFactory.getLogger(CacheTransferVariable.class);

    @Autowired
    public static CacheOperation mCacheOperation;

    @Override
    public void processHauckRandom(String key, int count, String value) {
        mCacheOperation.put(key, Constants.PIR.RANDOM + "_" + count, value);
    }

    @Override
    public void processResult(String key, String value) {
        mCacheOperation.put(key, Constants.PIR.RESULT, value);
    }

    @Override
    public boolean processHauckRandomLegal(String key, int count) {
        String value = getValue(key, Constants.PIR.RANDOM_LEGAL + "_" + count);
        return Boolean.valueOf(value);
    }

    @Override
    public String processClientRandom(String key) {
        return getValue(key, Constants.PIR.R);
    }

    private String getValue(String key, String name) {
        String value = mCacheOperation.get(key, name);
        long start = System.currentTimeMillis();
        while (value == null || value.isEmpty()) {
            if (System.currentTimeMillis() - start > 120000) {
                return "";
            }
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
            value = mCacheOperation.get(key, name);
        }
        return value;
    }
}
