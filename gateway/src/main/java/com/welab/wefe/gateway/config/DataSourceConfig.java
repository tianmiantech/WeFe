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

package com.welab.wefe.gateway.config;

import com.welab.wefe.common.data.mysql.config.AbstractJpaConfig;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.entity.FlagEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @author aaron.li
 **/
@Configuration
@EntityScan("com.welab.gateway")
@EnableJpaRepositories(basePackageClasses = GatewayServer.class,
        entityManagerFactoryRef = "entityManagerFactoryRefWefeGateway",
        transactionManagerRef = "transactionManagerRefWefeGateway")
public class DataSourceConfig extends AbstractJpaConfig {

    @Bean
    @ConfigurationProperties(prefix = "db.mysql")
    @Primary
    public DataSource wefeGatewayDS() {
        return createDatasource();
    }

    @Bean
    @Primary
    LocalContainerEntityManagerFactoryBean entityManagerFactoryRefWefeGateway(
            EntityManagerFactoryBuilder builder, @Qualifier("wefeGatewayDS") DataSource dataSource) {

        return entityManagerFactoryRef(builder, dataSource, mProperties, FlagEntity.class);
    }

    @Bean
    @Primary
    PlatformTransactionManager transactionManagerRefWefeGateway(
            @Qualifier("entityManagerFactoryRefWefeGateway") LocalContainerEntityManagerFactoryBean factoryBean) {

        return new JpaTransactionManager(factoryBean.getObject());
    }

}
