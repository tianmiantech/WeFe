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

package com.welab.wefe.gateway.init;

import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.dto.AliyunFunctionComputeConfigModel;
import com.welab.wefe.gateway.dto.ClickhouseStorageConfigModel;
import com.welab.wefe.gateway.dto.StorageConfigModel;
import com.welab.wefe.gateway.service.GlobalConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize the intermediate result storage module of the modeling
 */
public class InitStorageManager {
    private final static Logger LOG = LoggerFactory.getLogger(InitStorageManager.class);

    public static void init() {
        LOG.info("Start init storage manager.....");
        GlobalConfigService globalConfigService = GatewayServer.CONTEXT.getBean(GlobalConfigService.class);
        StorageConfigModel storageConfigModel = globalConfigService.getStorageConfig();
        ClickhouseStorageConfigModel clickhouseStorageConfigModel = globalConfigService.getClickhouseStorageConfig();
        AliyunFunctionComputeConfigModel aliyunFunctionComputeConfigModel = globalConfigService.getAliyunFunctionComputeConfig();

    }
}
