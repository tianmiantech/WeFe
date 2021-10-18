package com.welab.wefe.manager.service.config;

import com.welab.wefe.common.data.mysql.config.AbstractJpaConfig;
import com.welab.wefe.manager.service.ManagerService;
import com.welab.wefe.manager.service.entity.DataSet;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * @Author Jervis
 * @Date 2020-05-19
 **/
@Configuration
@EnableJpaRepositories(basePackageClasses = ManagerService.class,
        entityManagerFactoryRef = "entityManagerFactoryRefWefeMgr",
        transactionManagerRef = "transactionManagerRefWefeMgr")
public class DataSourceConfig extends AbstractJpaConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.wefe")
    @Primary
    DataSource wefeDS() {
        return createDatasource();
    }

    @Bean
    @Primary
    LocalContainerEntityManagerFactoryBean entityManagerFactoryRefWefeMgr(
            EntityManagerFactoryBuilder builder, @Qualifier("wefeDS") DataSource dataSource) {

        return entityManagerFactoryRef(builder, dataSource, mProperties, DataSet.class);
    }

    @Bean
    @Primary
    PlatformTransactionManager transactionManagerRefWefeMgr(
            @Qualifier("entityManagerFactoryRefWefeMgr") LocalContainerEntityManagerFactoryBean factoryBean) {

        return new JpaTransactionManager(factoryBean.getObject());
    }

}
