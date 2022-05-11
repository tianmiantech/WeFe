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
package com.welab.wefe.board.service.dto.kernel.machine_learning;

import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.board.service.dto.globalconfig.storage.ClickHouseStorageConfigModel;
import com.welab.wefe.board.service.dto.globalconfig.storage.StorageBaseConfigModel;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.data.storage.common.DBType;
import com.welab.wefe.common.web.Launcher;

/**
 * @author zane
 * @date 2022/5/11
 */
public class StorageConfig {
    private static GlobalConfigService CONFIG_SERVICE = Launcher.getBean(GlobalConfigService.class);

    public DBType dbType;
    public ClickHouseStorageConfigModel clickHouseStorageConfigModel;

    @JSONField(serialize = false)
    public static StorageConfig get() {
        StorageBaseConfigModel baseConfig = CONFIG_SERVICE.getModel(StorageBaseConfigModel.class);
        if (baseConfig.storageType == null) {
            throw new RuntimeException("数据集存储环境未选择，请在[全局设置][系统设置]中指定数据集存储类型。");
        }

        StorageConfig config = new StorageConfig();
        config.dbType = baseConfig.storageType;
        config.clickHouseStorageConfigModel = CONFIG_SERVICE.getModel(ClickHouseStorageConfigModel.class);

        return config;
    }
}
