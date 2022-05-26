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

import com.welab.wefe.common.data.storage.service.fc.FcStorage;
import com.welab.wefe.common.data.storage.service.fc.aliyun.AliyunOssConfig;
import com.welab.wefe.common.data.storage.service.persistent.PersistentStorage;
import com.welab.wefe.common.data.storage.service.persistent.clickhouse.ClickhouseConfig;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.dto.AliyunFunctionComputeConfigModel;
import com.welab.wefe.gateway.dto.ClickhouseStorageConfigModel;
import com.welab.wefe.gateway.service.GlobalConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initialize persistent storage services and FC storage services
 */
public class InitStorageManager {
    private final static Logger LOG = LoggerFactory.getLogger(InitStorageManager.class);
    public static AtomicBoolean PERSISTENT_INIT = new AtomicBoolean(false);
    public static AtomicBoolean FC_INIT = new AtomicBoolean(false);

    /**
     * Initialize
     */
    public static void init() {
        initPersistent(false);
        initFC(false);
    }

    /**
     * Initialize persistent storage services
     *
     * @param force If true, force reinitialization
     */
    public static boolean initPersistent(boolean force) {
        LOG.info("Start init persistent storage.....");
        if (force) {
            PERSISTENT_INIT.set(initPersistentStorage());
        } else if (!PERSISTENT_INIT.get()) {
            PERSISTENT_INIT.set(initPersistentStorage());
        }
        if (!PERSISTENT_INIT.get()) {
            LOG.error("Init persistent storage fail, Please check whether the configuration is correct!!!!!!!!!!!!(＞﹏＜)(＞﹏＜)(＞﹏＜)(＞﹏＜)(＞﹏＜)(＞﹏＜)(＞﹏＜)(＞﹏＜)!!!!!!!!!!!");
        } else {
            LOG.error("Init persistent storage success.");
        }
        return PERSISTENT_INIT.get();
    }

    /**
     * Initialize FC storage services
     *
     * @param force If true, force reinitialization
     */
    public static boolean initFC(boolean force) {
        LOG.info("Start init FC storage.....");
        if (force) {
            FC_INIT.set(initFcStorage());
        } else if (!FC_INIT.get()) {
            FC_INIT.set(initFcStorage());
        }
        if (!FC_INIT.get()) {
            LOG.error("Init FC storage fail, Please check whether the configuration is correct!!!!!!!!!!!!(＞﹏＜)(＞﹏＜)(＞﹏＜)(＞﹏＜)(＞﹏＜)(＞﹏＜)(＞﹏＜)(＞﹏＜)");
        } else {
            LOG.error("Init FC storage success.");
        }
        return FC_INIT.get();
    }

    private static boolean initPersistentStorage() {
        try {
            GlobalConfigService globalConfigService = GatewayServer.CONTEXT.getBean(GlobalConfigService.class);
            ClickhouseStorageConfigModel configModel = globalConfigService.getClickhouseStorageConfig();
            if (null == configModel) {
                return false;
            }
            PersistentStorage.init(new ClickhouseConfig(configModel.getHost(), configModel.getHttpPort(), configModel.getUsername(), configModel.getPassword()));
            return true;
        } catch (Exception e) {
            LOG.error("Init persistent storage fail, exception: ", e);
        }
        return false;
    }

    private static boolean initFcStorage() {
        try {
            GlobalConfigService globalConfigService = GatewayServer.CONTEXT.getBean(GlobalConfigService.class);
            AliyunFunctionComputeConfigModel configModel = globalConfigService.getAliyunFunctionComputeConfig();
            if (null == configModel) {
                return false;
            }
            AliyunOssConfig aliyunOssConfig = new AliyunOssConfig(configModel.getAccessKeyId(),
                    configModel.getAccessKeySecret(), configModel.getOssBucketName(), "wefe-fc", configModel.getRegion());
            FcStorage.initWithAliyun(aliyunOssConfig);
            return true;
        } catch (Exception e) {
            LOG.error("Init FC storage fail, exception: ", e);
        }
        return false;
    }
}
