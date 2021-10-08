/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * If manual creation of druidDataSource fails, it will cause dead loop and program jam，View details :https://blog.csdn.net/q283614346/article/details/103668748
 * resolvent: https://blog.csdn.net/qq_36669407/article/details/104744899
 *
 * @author hunter.zhao
 */
public abstract class AbstractDruidTemplate extends AbstractTemplate {


    private static final Map<String, DruidPooledConnection> DRUID_POOL_CONNECTION = new HashMap<>();

    private static final Map<String, Object> PROPERTIES = new HashMap<>();

    static {
        PROPERTIES.put("connectionTimeout", 15000);
        PROPERTIES.put("idleTimeout", 60000);
        PROPERTIES.put("validationTimeout", 3000);
        PROPERTIES.put("maxLifetime", 60000);
        PROPERTIES.put("maximumPoolSize", 10);
        PROPERTIES.put("maxWait", "1000");
    }

    public AbstractDruidTemplate(String url, String username, String password, String sql, String userId) {
        super(url, username, password, sql, userId);
    }

    /**
     * database.driver
     *
     * @return driverName
     */
    protected abstract String driver();

    @Override
    protected Map<String, Object> execute() throws StatusCodeWithException {
        //Get connection pool
        DruidPooledConnection connection = getConnection(url, username, password, driver());

        try {

            sql = StringUtil.replace(sql, placeholder, userId);

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                int count = resultSet.getMetaData().getColumnCount();

                Map<String, Object> featureData = new HashMap<>(16);
                for (int i = 0; i < count; i++) {
                    /**
                     * The index of resultSet starts with 1 and needs to be added with 1
                     */
                    featureData.put(resultSet.getMetaData().getColumnLabel(i + 1), resultSet.getObject(i + 1));
                }

                return featureData;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StatusCodeWithException(e.getMessage(), StatusCode.PARAMETER_VALUE_INVALID);
        }

        return null;
    }


    private static DruidPooledConnection getConnection(String url, String username, String password, String driver) throws StatusCodeWithException {

        DruidPooledConnection pool = DRUID_POOL_CONNECTION.get(url);

        if (pool != null) {
            return pool;
        }

        synchronized (PROPERTIES) {
            PROPERTIES.put("url", url);
            PROPERTIES.put("username", username);
            PROPERTIES.put("password", password);
            PROPERTIES.put("driverClassName", driver);

            try {

                DruidDataSource druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(PROPERTIES);
                druidDataSource.setBreakAfterAcquireFailure(true);
                druidDataSource.setConnectionErrorRetryAttempts(0);
                DRUID_POOL_CONNECTION.put(url, druidDataSource.getConnection());

            } catch (Exception e) {
                e.printStackTrace();
                throw new StatusCodeWithException("connection error: " + url, StatusCode.PARAMETER_VALUE_INVALID);
            }
        }

        return DRUID_POOL_CONNECTION.get(url);
    }
}
