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
package com.welab.wefe.common.wefe.dto.global_config.calculation_engine.fc;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.dto.global_config.base.AbstractConfigModel;
import com.welab.wefe.common.wefe.dto.global_config.base.ConfigGroupConstant;
import com.welab.wefe.common.wefe.dto.global_config.base.ConfigModel;
import com.welab.wefe.common.wefe.enums.FcCloudProvider;

/**
 * 函数计算基础配置项
 *
 * @author zane
 * @date 2021/10/29
 */
@ConfigModel(group = ConfigGroupConstant.FC_CONFIG)
public class FunctionComputeBaseConfigModel extends AbstractConfigModel {

    @Check(
            name = "函数计算的提供商",
            require = true,
            desc = "aliyun/tencentcloud"
    )
    public FcCloudProvider cloudProvider = FcCloudProvider.aliyun;
    @Check(name = "日费用上限", require = true)
    public int maxCostInDay = 500;

    @Check(name = "月费用上限", require = true)
    public int maxCostInMonth = 1000;
}
