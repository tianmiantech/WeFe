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

import com.alibaba.druid.pool.DruidDataSource;
import com.welab.wefe.common.data.storage.common.DBType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author yuxin.zhang
 */
@Configuration
@ComponentScan("com.welab.wefe.common.data.storage")
public class DruidConfig {

    @Autowired
    private JdbcParamConfig jdbcParamConfig;

    @Value(value = "${db.storage.type}")
    private DBType dbType;

    @Bean("storageDataSource")
    public DataSource dataSource() {
        DruidDataSource datasource = new DruidDataSource();
        if (dbType == DBType.CLICKHOUSE) {
            datasource.setUrl(jdbcParamConfig.getUrl());
            datasource.setDriverClassName(jdbcParamConfig.getDriverClassName());
            datasource.setInitialSize(jdbcParamConfig.getInitialSize());
            datasource.setMinIdle(jdbcParamConfig.getMinIdle());
            datasource.setMaxActive(jdbcParamConfig.getMaxActive());
            datasource.setMaxWait(jdbcParamConfig.getMaxWait());
            datasource.setPassword(jdbcParamConfig.getPassword());
            datasource.setUsername(jdbcParamConfig.getUsername());
            datasource.setTestWhileIdle(jdbcParamConfig.isTestWhileIdle());
            datasource.setValidationQuery(jdbcParamConfig.getValidationQuery());


            datasource.setTimeBetweenEvictionRunsMillis(jdbcParamConfig.getTimeBetweenEvictionRunsMillis());
            datasource.setMinEvictableIdleTimeMillis(jdbcParamConfig.getMinEvictableIdleTimeMillis());
            datasource.setRemoveAbandoned(jdbcParamConfig.isRemoveAbandoned());
            datasource.setRemoveAbandonedTimeout(jdbcParamConfig.getRemoveAbandonedTimeout());
            datasource.setLogAbandoned(jdbcParamConfig.isLogAbandoned());
        }
        return datasource;
    }

}
