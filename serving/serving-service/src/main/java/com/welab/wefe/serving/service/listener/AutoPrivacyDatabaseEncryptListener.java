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

package com.welab.wefe.serving.service.listener;

import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.service.PrivacyDatabaseEncryptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

/**
 * Auto encrypt mobile phone number
 */
@Component
public class AutoPrivacyDatabaseEncryptListener implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(AutoPrivacyDatabaseEncryptListener.class);

    @Autowired
    private ConfigurableEnvironment configurableEnvironment;

    @Autowired
    private PrivacyDatabaseEncryptService privacyDatabaseEncryptService;

    @Autowired
    private Config config;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        if (configurableEnvironment.containsProperty("database.encrypt.completed") || !config.isDatabaseEncryptEnable()) {
            return;
        }
        try {
            LOG.info("Start auto database data encrypt........");
            privacyDatabaseEncryptService.encrypt();
            LOG.info("End auto database data encrypt!!!");
        } catch (Exception e) {
            LOG.error("Auto database data encrypt exception: ", e);
        }
    }
}
