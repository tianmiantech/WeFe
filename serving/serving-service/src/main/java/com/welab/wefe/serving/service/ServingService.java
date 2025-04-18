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

package com.welab.wefe.serving.service;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import com.welab.wefe.serving.sdk.manager.ModelProcessorManager;
import com.welab.wefe.serving.service.feature.CodeFeatureDataHandler;
import com.welab.wefe.serving.service.utils.sign.VerifySignUtil;

/**
 * @author hunter.zhao
 */
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {Launcher.class, ServingService.class})
public class ServingService implements ApplicationContextAware {

    public static void main(String[] args) {
        Launcher
                .instance()
                .apiPackageClass(ServingService.class)
                //.apiLogger(new ServingApiLogger())
                // Login status check method
                //.checkSessionTokenFunction((api, annotation, token) -> CurrentAccount.get() != null || annotation.allowAccessWithSign())
                .apiPermissionPolicy((request, annotation, params) -> {
                    if (!annotation.allowAccessWithSign()) {
                        return;
                    }
                    VerifySignUtil.rsaVerify(annotation.domain(), request, params);
                })
                .launch(ServingService.class, args);

        //Initialize model processor
        ModelProcessorManager.init();

        //Initialize feature processor
        CodeFeatureDataHandler.init();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Launcher.CONTEXT = applicationContext;
    }
}
