package com.welab.wefe.common.data.mongodb.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/9
 */
@Configuration
public class MongoManagerConfig extends AbstractConfig{
    @Value("${spring.datasource.mongodb.manager.uri:}")
    private String managerUri;
    @Value("${spring.datasource.mongodb.manager.databaseName:}")
    private String managerDatabaseName;

    @Bean
    public MongoClient mongoManagerClient() {
        if (StringUtils.isEmpty(managerUri)) {
            return null;
        }
        return new MongoClient(new MongoClientURI(managerUri));
    }


    @Bean
    public MongoDbFactory mongoDbManagerFactory(MongoClient mongoManagerClient) {
        if (null == mongoManagerClient) {
            return null;
        }
        return new SimpleMongoDbFactory(mongoManagerClient, managerDatabaseName);
    }


    @Bean
    public MongoTransactionManager transactionManagerManager(MongoDbFactory mongoDbManagerFactory) {
        if (null == mongoDbManagerFactory) {
            return null;
        }
        return new MongoTransactionManager(mongoDbManagerFactory);
    }

    @Bean
    public MongoTemplate mongoManagerTemplate(MongoDbFactory mongoDbManagerFactory) {
        if (null == mongoDbManagerFactory) {
            return null;
        }
        return new MongoTemplate(mongoDbManagerFactory, getConverter(mongoDbManagerFactory));
    }
}
