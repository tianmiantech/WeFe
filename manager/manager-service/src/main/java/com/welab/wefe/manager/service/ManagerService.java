/*
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

package com.welab.wefe.manager.service;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author yuxin.zhang
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DruidDataSourceAutoConfigure.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        TransactionAutoConfiguration.class
})
@EnableScheduling
@ComponentScan(
        basePackages = {"com.welab.wefe.common.data.mongodb"},
        nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {
                Launcher.class,
                ManagerService.class
        }
)
public class ManagerService implements ApplicationContextAware {

    /**
     * spring上下文环境
     */
    public static ApplicationContext CONTEXT = null;


    public static void main(String[] args) {

        Launcher.instance()
                .apiPackageClass(ManagerService.class)
                .checkSessionTokenFunction((api, annotation, token) -> CurrentAccount.get() != null)
                .launch(ManagerService.class, args);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }
}
