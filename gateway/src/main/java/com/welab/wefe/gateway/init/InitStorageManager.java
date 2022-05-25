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

import com.welab.wefe.common.data.storage.StorageManager;
import com.welab.wefe.common.data.storage.common.DBType;
import com.welab.wefe.common.data.storage.common.FunctionComputeType;
import com.welab.wefe.common.data.storage.config.FcStorageConfig;
import com.welab.wefe.common.data.storage.config.JdbcConfig;
import com.welab.wefe.common.data.storage.config.StorageConfig;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.ThreadUtil;
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

    public static void init() throws Exception {
        LOG.info("Start init storage manager.....");
        StorageConfigModel storageConfigModel = waitForLoadStorageConfig();
        ClickhouseStorageConfigModel clickhouseStorageConfigModel = waitForLoadClickhouseStorageConfig();
        AliyunFunctionComputeConfigModel aliyunFunctionComputeConfigModel = waitForLoadAliyunFunctionComputeConfig(storageConfigModel.getStorageType());
        StorageConfig storageConfig = new StorageConfig(buildJdbcConfig(clickhouseStorageConfigModel), buildFcStorageConfig(storageConfigModel.getStorageType(), aliyunFunctionComputeConfigModel));
        StorageManager.getInstance().init(storageConfig);
        LOG.info("Storage manager init success.....");
    }

    private static JdbcConfig buildJdbcConfig(ClickhouseStorageConfigModel clickhouseStorageConfigModel) throws Exception {
        return new JdbcConfig(DBType.CLICKHOUSE, clickhouseStorageConfigModel.getHost(), clickhouseStorageConfigModel.getHttpPort(),
                clickhouseStorageConfigModel.getUsername(), clickhouseStorageConfigModel.getPassword());
    }

    private static FcStorageConfig buildFcStorageConfig(String storageType, AliyunFunctionComputeConfigModel fcModel) {
        FcStorageConfig fcStorageConfig = new FcStorageConfig();
        if (DBType.OSS.name().equals(storageType) || DBType.OTS.name().equals(storageType)) {
            fcStorageConfig.setAccessKeyId(fcModel.getAccessKeyId());
            fcStorageConfig.setAccessKeySecret(fcModel.getAccessKeySecret());
            fcStorageConfig.setBucketName(fcModel.getOssBucketName());
            fcStorageConfig.setRegion(fcModel.getRegion());
            fcStorageConfig.setOssInternalEndPoint("https://oss-" + fcModel.getRegion() + "-internal.aliyuncs.com");
            fcStorageConfig.setFunctionComputeType(FunctionComputeType.Aliyun);
        }
        return fcStorageConfig;
    }


    private static StorageConfigModel waitForLoadStorageConfig() {
        GlobalConfigService globalConfigService = GatewayServer.CONTEXT.getBean(GlobalConfigService.class);
        while (true) {
            StorageConfigModel storageConfigModel = globalConfigService.getStorageConfig();
            if (null == storageConfigModel || StringUtil.isEmpty(storageConfigModel.getStorageType())) {
                LOG.info("Please set storage type configuration.......................");
                ThreadUtil.sleepSeconds(3);
                continue;
            }
            return storageConfigModel;
        }
    }

    private static ClickhouseStorageConfigModel waitForLoadClickhouseStorageConfig() {
        GlobalConfigService globalConfigService = GatewayServer.CONTEXT.getBean(GlobalConfigService.class);
        while (true) {
            ClickhouseStorageConfigModel clickhouseStorageConfigModel = globalConfigService.getClickhouseStorageConfig();
            if (null == clickhouseStorageConfigModel) {
                LOG.info("Please set clickhouse configuration.......................");
                ThreadUtil.sleepSeconds(3);
                continue;
            }
            return clickhouseStorageConfigModel;
        }
    }

    private static AliyunFunctionComputeConfigModel waitForLoadAliyunFunctionComputeConfig(String storageType) {
        if (!DBType.OSS.name().equals(storageType) && !DBType.OTS.name().equals(storageType)) {
            return new AliyunFunctionComputeConfigModel();
        }
        GlobalConfigService globalConfigService = GatewayServer.CONTEXT.getBean(GlobalConfigService.class);
        while (true) {
            AliyunFunctionComputeConfigModel aliyunFunctionComputeConfigModel = globalConfigService.getAliyunFunctionComputeConfig();
            if (null == aliyunFunctionComputeConfigModel) {
                LOG.info("Please set aliyun function compute configuration.......................");
                ThreadUtil.sleepSeconds(3);
                continue;
            }
            return aliyunFunctionComputeConfigModel;
        }
    }
}
