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

package com.welab.wefe.board.service.listener;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.service.EncryptPhoneNumberService;
import com.welab.wefe.common.util.CommentedProperties;
import com.welab.wefe.common.util.StringUtil;
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
public class AutoEncryptPhoneNumberListener implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(AutoEncryptPhoneNumberListener.class);

    @Autowired
    private ConfigurableEnvironment configurableEnvironment;
    @Autowired
    private EncryptPhoneNumberService encryptPhoneNumberService;

    @Autowired
    private Config config;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        String configPath = configurableEnvironment.getProperty("config.path");
        if (StringUtil.isEmpty(configPath) || !config.isEncryptPhoneNumberOpen()) {
            return;
        }
        String key = "has.auto.encrypt.phone.number";
        try {
            CommentedProperties properties = new CommentedProperties();
            properties.load(configPath);
            if (properties.containsKey(key)) {
                return;
            }
            LOG.info("Start auto encrypt phone number........");
            encryptPhoneNumberService.encrypt();
            properties.append(key, "true", "Whether the mobile phone number has been automatically encrypted. The presence of this field indicates that it has been encrypted");
            properties.store(configPath);
            LOG.info("End auto encrypt phone number!!!");
        } catch (Exception e) {
            LOG.error("Auto encrypt phone number exception: ", e);
        }
    }
}
