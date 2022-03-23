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

package com.welab.wefe.common.data.mysql.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.welab.wefe.common.data.mysql.sql_monitor.SqlMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * @author Jervis
 **/
public class AbstractJpaConfig {

    @Autowired
    protected JpaProperties mProperties;

    protected LocalContainerEntityManagerFactoryBean entityManagerFactoryRef(
            EntityManagerFactoryBuilder builder
            , DataSource ds
            , JpaProperties props
            , Class<?>... basePackageClasses) {

        return builder.dataSource(ds)
                .properties(props.getProperties())
                .packages(basePackageClasses)
                .persistenceUnit("pu1")
                .build();
    }

    protected DataSource createDatasource() {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        // 在 DruidDataSource 中添加自定义 Filter
        dataSource.setProxyFilters(Collections.singletonList(new SqlMonitor()));

        return dataSource;
    }
}
