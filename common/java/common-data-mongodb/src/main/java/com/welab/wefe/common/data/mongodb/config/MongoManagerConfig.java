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

package com.welab.wefe.common.data.mongodb.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
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
public class MongoManagerConfig extends AbstractConfig implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware, EnvironmentAware {

    private Environment environment;

    public ApplicationContext CONTEXT = null;


    public MongoDbFactory mongoDbManagerFactory(MongoClient mongoManagerClient, String managerDatabaseName) {
        if (null == mongoManagerClient) {
            return null;
        }
        return new SimpleMongoDbFactory(mongoManagerClient, managerDatabaseName);
    }


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        String managerUri = environment.getProperty("spring.datasource.mongodb.manager.uri");
        String managerDatabaseName = environment.getProperty("spring.datasource.mongodb.manager.databaseName");
        if (StringUtils.isNotEmpty(managerUri) && StringUtils.isNotEmpty(managerDatabaseName)) {
            BeanDefinition mongoManagerClientBean = BeanDefinitionBuilder.rootBeanDefinition(MongoClient.class)
                    .addConstructorArgValue(new MongoClientURI(managerUri)).getBeanDefinition();
            beanDefinitionRegistry.registerBeanDefinition("mongoManagerClient", mongoManagerClientBean);


            MongoClient mongoManagerClient = (MongoClient) CONTEXT.getBean("mongoManagerClient");


            MongoDbFactory mongoDbManagerFactory = mongoDbManagerFactory(mongoManagerClient, managerDatabaseName);

            BeanDefinition transactionManagerManagerBean = BeanDefinitionBuilder.rootBeanDefinition(MongoTransactionManager.class)
                    .addConstructorArgValue(mongoDbManagerFactory)
                    .getBeanDefinition();
            beanDefinitionRegistry.registerBeanDefinition("transactionManagerManager", transactionManagerManagerBean);

            BeanDefinition mongoManagerTemplateBean = BeanDefinitionBuilder.rootBeanDefinition(MongoTemplate.class)
                    .addConstructorArgValue(mongoDbManagerFactory)
                    .addConstructorArgValue(getConverter(mongoDbManagerFactory))
                    .getBeanDefinition();
            beanDefinitionRegistry.registerBeanDefinition("mongoManagerTemplate", mongoManagerTemplateBean);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }


    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
}
