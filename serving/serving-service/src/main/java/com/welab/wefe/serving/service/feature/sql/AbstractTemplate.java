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

package com.welab.wefe.serving.service.feature.sql;

import com.welab.wefe.common.exception.StatusCodeWithException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author hunter.zhao
 */
public abstract class AbstractTemplate {
    private Logger logger = LoggerFactory.getLogger(getClass());

    protected final String placeholder = "?";

    public String url;

    public String username;

    public String password;

    public String sql;

    public String userId;

    public AbstractTemplate(String url, String username, String password, String sql, String userId) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.sql = sql;
        this.userId = userId;
    }

    /**
     * sql execute
     * @return featureDataMap
     * @throws StatusCodeWithException
     */
    protected abstract Map<String, Object> execute() throws StatusCodeWithException;

    public Map<String, Object> handle() throws StatusCodeWithException {

        long start = System.currentTimeMillis();

        Map<String, Object> featureData = execute();

        logger.debug(getClass().getSimpleName() + ":" + (System.currentTimeMillis() - start) + " ms");

        return featureData;
    }
}
