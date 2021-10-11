/**
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

package com.welab.wefe.common.data.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


/**
 * @author yuxin.zhang
 */
@Component
@PropertySource(value = {"file:${config.path}"}, encoding = "utf-8")
public class JdbcParamConfig {
    @Value("${db.storage.clickhouse.username:null}")
    private String username;
    @Value("${db.storage.clickhouse.password:null}")
    private String password;
    @Value("${db.storage.clickhouse.driverClassName:null}")
    private String driverClassName;
    @Value("${db.storage.clickhouse.url:null}")
    private String url;
    @Value("${db.storage.clickhouse.initialSize:1}")
    private Integer initialSize;
    @Value("${db.storage.clickhouse.maxActive:50}")
    private Integer maxActive;
    @Value("${db.storage.clickhouse.minIdle:1}")
    private Integer minIdle;
    @Value("${db.storage.clickhouse.maxWait:60000}")
    private Integer maxWait;
    @Value("${db.storage.clickhouse.testWhileIdle:false}")
    private boolean testWhileIdle;
    @Value("${db.storage.clickhouse.validationQuery:null}")
    private String validationQuery;


    @Value("${db.storage.clickhouse.timeBetweenEvictionRunsMillis:15000}")
    private Integer timeBetweenEvictionRunsMillis;
    @Value("${db.storage.clickhouse.minEvictableIdleTimeMillis:60000}")
    private Integer minEvictableIdleTimeMillis;
    @Value("${db.storage.clickhouse.removeAbandoned:true}")
    private boolean removeAbandoned;
    @Value("${db.storage.clickhouse.removeAbandonedTimeout:60}")
    private Integer removeAbandonedTimeout;
    @Value("${db.storage.clickhouse.logAbandoned:true}")
    private boolean logAbandoned;
    /**
     * The optimal batch insertion byte size of clickhouse, unit: M (support decimal)
     */
    @Value("${db.storage.clickhouse.optimal.insert.byte.size:1}")
    private double optimalInsertByteSize;


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
