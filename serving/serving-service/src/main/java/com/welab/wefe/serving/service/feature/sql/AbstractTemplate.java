/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.feature.sql;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.jdbc.base.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author hunter.zhao
 */
public abstract class AbstractTemplate {
    private Logger logger = LoggerFactory.getLogger(getClass());

    protected final String placeholder = "?";

    public DatabaseType databaseType;

    public String host;

    public int port;

    public String database;

    public String username;

    public String password;

    public AbstractTemplate(DatabaseType databaseType,
                            String host,
                            int port,
                            String database,
                            String username,
                            String password) {
        this.databaseType = databaseType;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    /**
     * sql execute
     *
     * @return featureDataMap
     * @throws StatusCodeWithException
     */
    protected abstract Map<String, Object> execute(String sql) throws StatusCodeWithException;

    public Map<String, Object> handle(String sql) throws StatusCodeWithException {

        long start = System.currentTimeMillis();

        Map<String, Object> featureData = execute(sql);

        logger.debug(getClass().getSimpleName() + ":" + (System.currentTimeMillis() - start) + " ms");

        return featureData;
    }
}
