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
package com.welab.wefe.gateway.service.processors.available.checkpoint;

import com.welab.wefe.common.data.storage.common.Constant;
import com.welab.wefe.common.data.storage.config.JdbcParamConfig;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.repo.Storage;
import com.welab.wefe.common.data.storage.service.StorageService;
import com.welab.wefe.common.wefe.checkpoint.AbstractCheckpoint;
import com.welab.wefe.common.wefe.enums.ServiceType;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane
 * @date 2021/12/20
 */
@Service
public class StorageCheckpoint extends AbstractCheckpoint {

    @Autowired
    private StorageService storageService;
    @Autowired
    private JdbcParamConfig jdbcParamConfig;

    @Override
    protected ServiceType service() {
        return ServiceType.StorageService;
    }

    @Override
    protected String desc() {
        return "检查 gateway 对 storage 服务的访问是否正常";
    }

    @Override
    protected String getConfigValue() {
        return jdbcParamConfig.getUrl();
    }

    @Override
    protected String messageWhenConfigValueEmpty() {
        return null;
    }

    @Override
    protected void doCheck(String value) throws Exception {
        String name = RandomStringUtils.randomAlphabetic(6);
        Storage storage = storageService.getStorage();
        storage.put(Constant.DBName.WEFE_DATA, name, new DataItemModel<>(name, "test"));
        storage.dropTB(Constant.DBName.WEFE_DATA, name);
    }
}
