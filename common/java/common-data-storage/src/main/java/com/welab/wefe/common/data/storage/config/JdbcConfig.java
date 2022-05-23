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

package com.welab.wefe.common.data.storage.config;

import com.welab.wefe.common.data.storage.common.Constant;
import com.welab.wefe.common.data.storage.common.DBType;
import org.springframework.util.Assert;


/**
 * @author yuxin.zhang
 */
public class JdbcConfig {
    private String host = "127.0.0.1";
    private Integer port = 8123;
    private DBType dbType = DBType.CLICKHOUSE;
    private String username;
    private String password;
    private String driverClassName = Constant.DataBaseDriverClassName.CLICKHOUSE;
    private String url = "jdbc:clickhouse//127.0.0.1:8123";
    private Integer initialSize = 1;
    private Integer maxActive = 50;
    private Integer minIdle = 1;
    private Integer maxWait = 60000;
    private boolean testWhileIdle = false;
    private String validationQuery = "SELECT 1";


    private Integer timeBetweenEvictionRunsMillis = 15000;
    private Integer minEvictableIdleTimeMillis = 60000;
    private boolean removeAbandoned = true;
    private Integer removeAbandonedTimeout = 60;
    private boolean logAbandoned = true;
    /**
     * The optimal batch insertion byte size of clickhouse, unit: M (support decimal)
     */
    private double optimalInsertByteSize = 1;

    public JdbcConfig(DBType dbType,String host, Integer port, String username, String password) throws Exception {
        Assert.notNull(host, "host == null");
        Assert.notNull(port, "port == null");
        Assert.notNull(username, "username == null");
        Assert.notNull(password, "password == null");
        Assert.notNull(dbType, "dbType == null");
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.dbType = dbType;
        if (dbType == DBType.CLICKHOUSE) {
            this.driverClassName = Constant.DataBaseDriverClassName.CLICKHOUSE;
        } else {
            throw new Exception("Invalid storage type");
        }

        url = String.format("jdbc:%s://%s:%s", dbType.name().toLowerCase(), host, port);
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public DBType getDbType() {
        return dbType;
    }

    public void setDbType(DBType dbType) {
        this.dbType = dbType;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }


    public Integer getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(Integer timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public Integer getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(Integer minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public boolean isRemoveAbandoned() {
        return removeAbandoned;
    }

    public void setRemoveAbandoned(boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public Integer getRemoveAbandonedTimeout() {
        return removeAbandonedTimeout;
    }

    public void setRemoveAbandonedTimeout(Integer removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }

    public boolean isLogAbandoned() {
        return logAbandoned;
    }

    public void setLogAbandoned(boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    public double getOptimalInsertByteSize() {
        return optimalInsertByteSize;
    }

    public void setOptimalInsertByteSize(double optimalInsertByteSize) {
        this.optimalInsertByteSize = optimalInsertByteSize;
    }
}
