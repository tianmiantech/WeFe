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
package com.welab.wefe.board.service.dto.globalconfig.fc;

/**
 * @author zane
 * @date 2021/10/29
 */
public class FunctionComputeConfigModel {

    /**
     * 函数计算的提供商：aliyun/tencentcloud
     */
    public String cloudProvider = "aliyun";
    /**
     * 日费用上限
     */
    public int maxCostInDay = 500;
    /**
     * 月费用上限
     */
    public int maxCostInMonth = 1000;
}
