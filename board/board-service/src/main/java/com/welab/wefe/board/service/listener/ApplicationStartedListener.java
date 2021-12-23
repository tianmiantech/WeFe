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

package com.welab.wefe.board.service.listener;

import cn.hutool.core.thread.ThreadUtil;
import com.welab.wefe.board.service.database.entity.chat.MessageQueueMySqlModel;
import com.welab.wefe.board.service.database.repository.ChatUnreadMessageRepository;
import com.welab.wefe.board.service.service.MemberChatService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.exception.StatusCodeWithException;
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
    MemberChatService memberChatService;

    @Autowired
    ChatUnreadMessageRepository statUnreadMessageRepository;
    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {
            globalConfigService.init();
        } catch (StatusCodeWithException e) {
            LOG.error(e.getMessage(), e);
        }
        startChatListener();
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

                    switch (message.getAction()) {
                        case create_chat_msg: {
                            memberChatService.handleChatMessage(message);
                            break;
                        }
                        default:
                            LOG.info("Illegal type[" + message.getAction() + "]");
                    }
                } catch (Exception e) {
                    ThreadUtil.sleep(5, TimeUnit.SECONDS);
                    LOG.error("Listening chat message queue exception", e);
                }
            }
        });
    }
}
