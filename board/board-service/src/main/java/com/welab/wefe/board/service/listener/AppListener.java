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

import com.welab.wefe.common.data.storage.StorageManager;
import com.welab.wefe.common.data.storage.common.DBType;
import com.welab.wefe.common.data.storage.config.JdbcConfig;
import com.welab.wefe.common.data.storage.config.StorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author zane.luo
 */
@Component
public class AppListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(AppListener.class);


    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent applicationEnvironmentPreparedEvent) {
        LOG.info("start ApplicationEnvironmentPreparedEvent");
        try {

            JdbcConfig jdbcConfig = new JdbcConfig(
                    "host",
                    8123,
                    "user",
                    "pwd",
                    DBType.CLICKHOUSE
            );
            StorageConfig storageConfig = new StorageConfig(DBType.CLICKHOUSE, jdbcConfig);
            StorageManager.getInstance().init(storageConfig);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info("end ApplicationEnvironmentPreparedEvent");
    }

}
