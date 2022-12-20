/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import com.welab.wefe.common.jdbc.base.DatabaseType;

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

    private static final Map<String, DruidDataSource> DRUID_DATA_SOURCE = new HashMap<>();

    private static final Map<String, Object> PROPERTIES = new HashMap<>();

    static {
        PROPERTIES.put("connectionTimeout", 15000);
        PROPERTIES.put("idleTimeout", 60000);
        PROPERTIES.put("validationTimeout", 3000);
        PROPERTIES.put("maxLifetime", 60000);
        PROPERTIES.put("maximumPoolSize", 10);
        PROPERTIES.put("maxWait", "1000");
    }

    public AbstractDruidTemplate(
            DatabaseType databaseType,
            String host,
            int port,
            String database,
            String username,
            String password) {
        super(databaseType, host, port, database, username, password);
    }

    /**
     * database.driver
     *
     * @return driverName
     */
    protected abstract String driver();


    protected abstract String url();

    @Override
    protected Map<String, Object> execute(String sql) throws StatusCodeWithException {
        //Get connection pool
        DruidPooledConnection connection = getConnection(url(), username, password, driver());
        ResultSet resultSet = null;
        try {

            PreparedStatement statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

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
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private static DruidPooledConnection getConnection(String url, String username, String password, String driver) throws StatusCodeWithException {
        DruidDataSource dataSource = DRUID_DATA_SOURCE.get(url);

        try {
            if (dataSource != null) {
                return dataSource.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DruidPooledConnection connection = null;

        synchronized (PROPERTIES) {
            PROPERTIES.put("url", url);
            PROPERTIES.put("username", username);
            PROPERTIES.put("password", password);
            PROPERTIES.put("driverClassName", driver);

            try {

                DruidDataSource druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(PROPERTIES);
                druidDataSource.setBreakAfterAcquireFailure(true);
                druidDataSource.setConnectionErrorRetryAttempts(0);

                connection = druidDataSource.getConnection();

                DRUID_DATA_SOURCE.put(url, druidDataSource);
            } catch (Exception e) {
                e.printStackTrace();

                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }

                throw new StatusCodeWithException("connection error: " + url, StatusCode.PARAMETER_VALUE_INVALID);
            }
        }

        return connection;
    }
}
