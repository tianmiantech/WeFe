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
import com.welab.wefe.board.service.dto.globalconfig.calculation_engine.CalculationEngineBaseConfigModel;
import com.welab.wefe.board.service.dto.globalconfig.calculation_engine.fc.AliyunFunctionComputeConfigModel;
import com.welab.wefe.board.service.dto.globalconfig.calculation_engine.fc.FunctionComputeBaseConfigModel;
import com.welab.wefe.board.service.dto.globalconfig.calculation_engine.spark.SparkStandaloneConfigModel;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.JobBackendType;

/**
 * @author zane
 * @date 2022/5/11
 */
public class CalculationEngineConfig {
    private static GlobalConfigService CONFIG_SERVICE = Launcher.getBean(GlobalConfigService.class);

    public JobBackendType backend;
    public FunctionComputeBaseConfigModel functionComputeBaseConfig;
    public AliyunFunctionComputeConfigModel aliyunFunctionComputeConfig;
    public SparkStandaloneConfigModel sparkStandaloneConfig;

    @JSONField(serialize = false)
    public static CalculationEngineConfig get() {
        CalculationEngineBaseConfigModel baseConfig = CONFIG_SERVICE.getModel(CalculationEngineBaseConfigModel.class);
        if (baseConfig.backend == null) {
            throw new RuntimeException("计算环境未选择，请在[全局设置][计算引擎设置]中指定计算环境。");
        }

        CalculationEngineConfig config = new CalculationEngineConfig();
        config.backend = baseConfig.backend;
        config.functionComputeBaseConfig = CONFIG_SERVICE.getModel(FunctionComputeBaseConfigModel.class);
        config.aliyunFunctionComputeConfig = CONFIG_SERVICE.getModel(AliyunFunctionComputeConfigModel.class);
        config.sparkStandaloneConfig = CONFIG_SERVICE.getModel(SparkStandaloneConfigModel.class);

        return config;
    }
}
