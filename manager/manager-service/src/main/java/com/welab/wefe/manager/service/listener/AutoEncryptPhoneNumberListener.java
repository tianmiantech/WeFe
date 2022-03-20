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

package com.welab.wefe.manager.service.listener;

import com.welab.wefe.manager.service.service.EncryptPhoneNumberService;
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

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        if (configurableEnvironment.containsProperty("has.auto.encrypt.phone.number")) {
            return;
        }
        try {
            LOG.info("Start auto encrypt phone number........");
            encryptPhoneNumberService.encrypt();
            LOG.info("End auto encrypt phone number!!!");
        } catch (Exception e) {
            LOG.error("Auto encrypt phone number exception: ", e);
        }
    }
}
