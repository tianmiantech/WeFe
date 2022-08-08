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

import com.welab.wefe.common.wefe.checkpoint.AbstractCheckpoint;
import com.welab.wefe.common.wefe.enums.JobBackendType;
import com.welab.wefe.common.wefe.enums.ServiceType;
import com.welab.wefe.gateway.dto.CalculationEngineBaseConfigModel;
import com.welab.wefe.gateway.init.InitStorageManager;
import com.welab.wefe.gateway.service.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane
 * @date 2021/12/20
 */
@Service
public class FcCheckpoint extends AbstractCheckpoint {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ServiceType service() {
        return ServiceType.FcService;
    }

    @Override
    protected String desc() {
        return "检查函数计算环境是否可用";
    }

    @Override
    protected String getConfigValue() {
        CalculationEngineBaseConfigModel config = globalConfigService.getModel(
                GlobalConfigService.Group.CALCULATION_ENGINE_CONFIG,
                CalculationEngineBaseConfigModel.class
        );

        if (config == null) {
            return null;
        }
        return config.backend.name();

    }

    @Override
    protected String messageWhenConfigValueEmpty() {
        return null;
    }

    @Override
    protected void doCheck(String value) throws Exception {
        CalculationEngineBaseConfigModel config = globalConfigService.getModel(
                GlobalConfigService.Group.CALCULATION_ENGINE_CONFIG,
                CalculationEngineBaseConfigModel.class
        );
        if (config == null || config.backend != JobBackendType.FC) {
            return;
        }

        if (!InitStorageManager.FC_INIT.get()) {
            throw new Exception("函数计算存储未成功初始化，请在[全局设置][计算环境设置]中检查函数计算相关配置是否正确。");
        }
    }
}
