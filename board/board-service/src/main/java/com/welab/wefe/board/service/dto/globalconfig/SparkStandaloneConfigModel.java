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
package com.welab.wefe.board.service.dto.globalconfig;

import com.welab.wefe.board.service.dto.globalconfig.base.ConfigGroupConstant;
import com.welab.wefe.board.service.dto.globalconfig.base.ConfigModel;

/**
 * 单机版配置
 *
 * @author zane
 * @date 2022/04/27
 */
@ConfigModel(group = ConfigGroupConstant.SPARK_STANDALONE_CONFIG)
public class SparkStandaloneConfigModel {
    /**
     * Driver内存，默认1024m：
     * 单位：m、g
     */
    public String driverMemory = "1g";
    /**
     * 结果集的最大大小，默认1G
     * 单位：m、g
     */
    public String driverMaxResultSize = "1g";
    /**
     * 每个executor的内存
     * 单位：m、g
     */
    public String executorMemory = "1g";

}
