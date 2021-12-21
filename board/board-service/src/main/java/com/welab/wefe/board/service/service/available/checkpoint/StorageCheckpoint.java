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

package com.welab.wefe.board.service.service.available.checkpoint;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.common.data.storage.config.JdbcParamConfig;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.repo.Storage;
import com.welab.wefe.common.data.storage.service.StorageService;
import com.welab.wefe.common.web.Launcher;
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
    protected Config config;

    @Override
    public ServiceType service() {
        return ServiceType.StorageService;
    }

    @Override
    public String desc() {
        return "检查 board 对 storage 服务的访问是否正常";
    }

    @Override
    public String value() {
        JdbcParamConfig storageConfig = Launcher.getBean(JdbcParamConfig.class);
        return storageConfig.getUrl();
    }

    @Override
    protected void doCheck() throws Exception {

        StorageService service = Launcher.getBean(StorageService.class);
        Storage storage = service.getStorage();
        String name = RandomStringUtils.randomAlphabetic(6);
        try {
            storage.put(DATABASE_NAME, name, new DataItemModel<>(name, "test"));
        } catch (Exception e) {
            super.log(e);
            throw new Exception(config.getDbType().name() + " put异常，请检查相关配置是否正确。");
        }

        try {
            storage.dropTB(DATABASE_NAME, name);
        } catch (Exception e) {
            super.log(e);
            throw new Exception(config.getDbType().name() + " drop异常，请检查相关配置是否正确。");
        }

    }
}
