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

package com.welab.wefe.data.fusion.service.database;

import com.welab.wefe.common.data.mysql.config.AbstractJpaConfig;
import com.welab.wefe.data.fusion.service.AppService;
import com.welab.wefe.data.fusion.service.database.repository.base.BaseRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author aaron.li
 **/
@Configuration
@EntityScan("com.welab.wefe.data.fusion.service")
@EnableJpaRepositories(basePackageClasses = AppService.class,
        repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class,
        entityManagerFactoryRef = "entityManagerFactoryRefBoard",
        transactionManagerRef = "transactionManagerRefWefeBoard")
public class DataSourceConfig extends AbstractJpaConfig {

    @Bean("dataFusion")
    @ConfigurationProperties(prefix = "db.mysql")
    @Primary
    DataSource dataFusion() {
        return createDatasource();
    }

    @Bean("entityManagerFactoryRefBoard")
    @Primary
    LocalContainerEntityManagerFactoryBean entityManagerFactoryRefWefeBoard(
            EntityManagerFactoryBuilder builder, @Qualifier("dataFusion") DataSource dataSource) {
        // Set entity and table naming rules (since custom data sources override jPA rules)
        Map<String, String> pros = mProperties.getProperties();
        pros.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
        pros.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());

        return entityManagerFactoryRef(builder, dataSource, mProperties, AppService.class);
    }

    @Bean
    @Primary
    PlatformTransactionManager transactionManagerRefWefeBoard(
            @Qualifier("entityManagerFactoryRefBoard") LocalContainerEntityManagerFactoryBean factoryBean) {

        return new JpaTransactionManager(factoryBean.getObject());
    }

}
