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

import cn.hutool.core.thread.ThreadUtil;
import com.welab.wefe.board.service.cache.CaCertificateCache;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.chat.MessageQueueMySqlModel;
import com.welab.wefe.board.service.database.repository.ChatUnreadMessageRepository;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.service.MemberChatService;
import com.welab.wefe.board.service.service.PrivacyDatabaseEncryptService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.wefe.dto.global_config.PrivacyConfigModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Monitor the message queue and process chat messages
 *
 * @author Johnny.lin
 */
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationStartedListener.class);

    @Autowired
    private Config config;

    @Autowired
    MemberChatService memberChatService;

    @Autowired
    ChatUnreadMessageRepository statUnreadMessageRepository;
    @Autowired
    private GlobalConfigService globalConfigService;
    @Autowired
    private DataSetStorageService dataSetStorageService;
    @Autowired
    private PrivacyDatabaseEncryptService privacyDatabaseEncryptService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {
            globalConfigService.init();

            privacyDatabaseEncrypt();

            dataSetStorageService.initStorage();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        startChatListener();

        loadCaCertificate();
    }

    private void startChatListener() {
        LOG.info("MemberChatListener start");
        // Process received chat messages
        ThreadUtil.execAsync(() -> {
            while (true) {
                try {
                    MessageQueueMySqlModel message = memberChatService.getOneMessage();
                    if (message == null) {
                        ThreadUtil.sleep(2, TimeUnit.SECONDS);
                        continue;
                    }

                    memberChatService.handleChatMessage(message);
                } catch (Exception e) {
                    ThreadUtil.sleep(5, TimeUnit.SECONDS);
                    LOG.error("Listening chat message queue exception", e);
                }
            }
        });
    }

    private void loadCaCertificate() {
        LOG.info("Start refresh certificate cache..............");
        CaCertificateCache.getInstance().refreshCache();
        LOG.info("End refresh certificate cache.");
    }

    private void privacyDatabaseEncrypt() {
        try {
            PrivacyConfigModel configModel = globalConfigService.getModel(PrivacyConfigModel.class);
            configModel = (null == configModel ? new PrivacyConfigModel() : configModel);
            if (configModel.databaseEncryptCompleted && !config.isDatabaseEncryptEnable()) {
                LOG.error("The data has been encrypted. Please change the value of the configuration item [privacy.database.encrypt.enable] to true to start the system in an encrypted way!!!");
                System.exit(0);
                return;
            }
            if (!config.isDatabaseEncryptEnable() || configModel.databaseEncryptCompleted) {
                return;
            }
            LOG.info("Start auto encrypt privacy database........");
            privacyDatabaseEncryptService.encrypt();
            configModel.databaseEncryptCompleted = true;
            globalConfigService.put(configModel);

            LOG.info("End auto encrypt privacy database!!!");
        } catch (Exception e) {
            LOG.error("Auto encrypt privacy database exception: ", e);
            System.exit(-1);
        }

    }
}
