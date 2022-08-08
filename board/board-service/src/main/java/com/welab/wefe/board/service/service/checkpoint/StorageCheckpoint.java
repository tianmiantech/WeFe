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

package com.welab.wefe.board.service.service.checkpoint;

import com.welab.wefe.board.service.dto.globalconfig.storage.StorageBaseConfigModel;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.service.persistent.PersistentStorage;
import com.welab.wefe.common.wefe.checkpoint.AbstractCheckpoint;
import com.welab.wefe.common.wefe.enums.ServiceType;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.welab.wefe.board.service.service.DataSetStorageService.DATABASE_NAME;

/**
 * @author zane
 */
@Service
public class StorageCheckpoint extends AbstractCheckpoint {
    @Autowired
    protected GlobalConfigService configService;

    @Override
    public ServiceType service() {
        return ServiceType.StorageService;
    }

    @Override
    public String desc() {
        return "检查 board 对 storage 服务的访问是否正常";
    }

    @Override
    public String getConfigValue() {
        return null;
    }

    @Override
    protected String messageWhenConfigValueEmpty() {
        return null;
    }

    @Override
    protected void doCheck(String value) throws Exception {
        if (!PersistentStorage.inited()) {
            throw new Exception("数据集存储不可用，请在[全局设置][系统设置]中检查数据集存储配置是否正确。");
        }

        StorageBaseConfigModel config = configService.getModel(StorageBaseConfigModel.class);

        String name = RandomStringUtils.randomAlphabetic(6);
        try {
            PersistentStorage.getInstance().put(DATABASE_NAME, name, new DataItemModel<>(name, "test"));
        } catch (Exception e) {
            super.log(e);
            throw new Exception(config.storageType.name() + " put 异常，请检查相关配置是否正确：" + e.getMessage());
        }

        try {
            PersistentStorage.getInstance().dropTB(DATABASE_NAME, name);
        } catch (Exception e) {
            super.log(e);
            throw new Exception(config.storageType.name() + " drop 异常，请检查相关配置是否正确：" + e.getMessage());
        }

    }
}
