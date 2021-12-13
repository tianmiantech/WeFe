/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.board.service.listener.online_demo;

import com.welab.wefe.board.service.constant.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * Demo 环境专用逻辑
 *
 * @author zane
 * @date 2021/12/13
 */
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Config config;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!config.isOnlineDemo()) {
            return;
        }
    }
}
