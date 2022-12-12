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
import com.welab.wefe.common.data.storage.service.fc.tencent.TencentCosConfig;
import com.welab.wefe.common.data.storage.service.persistent.PersistentStorage;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.dto.global_config.calculation_engine.fc.AliyunFunctionComputeConfigModel;
import com.welab.wefe.common.wefe.dto.global_config.calculation_engine.fc.FunctionComputeBaseConfigModel;
import com.welab.wefe.common.wefe.dto.global_config.calculation_engine.fc.TencentServerlessCloudFunctionConfigModel;
import com.welab.wefe.common.wefe.dto.global_config.storage.ClickHouseStorageConfigModel;
import com.welab.wefe.common.wefe.dto.storage.ClickhouseConfig;
import com.welab.wefe.common.wefe.enums.FcCloudProvider;
import com.welab.wefe.gateway.GatewayServer;
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
        if (force || !FC_INIT.get()) {
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
            ClickHouseStorageConfigModel configModel = globalConfigService.getModel(ClickHouseStorageConfigModel.class);
            if (null == configModel) {
                return false;
            }
            ClickhouseConfig clickhouseConfig = new ClickhouseConfig(configModel.host, configModel.http_port, configModel.username, configModel.password);
            clickhouseConfig.setRemoveAbandoned(false);
            PersistentStorage.init(clickhouseConfig);
            return true;
        } catch (Exception e) {
            LOG.error("Init persistent storage fail, exception: ", e);
        }
        return false;
    }

    private static boolean initFcStorage() {
        try {
            GlobalConfigService globalConfigService = GatewayServer.CONTEXT.getBean(GlobalConfigService.class);
            FunctionComputeBaseConfigModel functionComputeBaseConfigModel = globalConfigService.getModel(FunctionComputeBaseConfigModel.class);
            if ((functionComputeBaseConfigModel.cloudProvider).equals(FcCloudProvider.aliyun)) {
                AliyunFunctionComputeConfigModel configModel = globalConfigService.getModel(AliyunFunctionComputeConfigModel.class);
                if (null == configModel || StringUtil.isEmpty(configModel.accessKeyId) || StringUtil.isEmpty(configModel.accessKeySecret)) {
                    return false;
                }
                AliyunOssConfig aliyunOssConfig = new AliyunOssConfig(configModel.accessKeyId,
                        configModel.accessKeySecret, configModel.ossBucketName, "wefe-fc", configModel.region);
                FcStorage.initWithAliyun(aliyunOssConfig);
            } else if((functionComputeBaseConfigModel.cloudProvider).equals(FcCloudProvider.tencentcloud)){
                TencentServerlessCloudFunctionConfigModel configModel = globalConfigService.getModel(TencentServerlessCloudFunctionConfigModel.class);
                if (null == configModel || StringUtil.isEmpty(configModel.accessKeyId) || StringUtil.isEmpty(configModel.accessKeySecret)){
                    return false;
                }
                TencentCosConfig tencentCosConfig = new TencentCosConfig(configModel.accessKeyId,configModel.accessKeySecret,configModel.cosBucketName,configModel.region);
                FcStorage.initWithTencent(tencentCosConfig);

            }
            return true;
        } catch (Exception e) {
            LOG.error("Init FC storage fail, exception: ", e);
        }
        return false;
    }
}
