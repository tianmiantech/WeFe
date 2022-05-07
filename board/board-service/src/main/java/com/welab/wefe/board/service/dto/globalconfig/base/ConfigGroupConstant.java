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
package com.welab.wefe.board.service.dto.globalconfig.base;

/**
 * @author zane
 * @date 2022/5/7
 */
public class ConfigGroupConstant {
    /****************************** 系统设置 *************************************/
    public static final String MEMBER_INFO = "member_info";
    public static final String MAIL_SERVER = "mail_server";
    public static final String ALERT_CONFIG = "alert_config";

    /****************************** 子系统 *************************************/
    public static final String WEFE_GATEWAY = "wefe_gateway";
    public static final String WEFE_BOARD = "wefe_board";
    public static final String WEFE_FLOW = "wefe_flow";
    public static final String WEFE_SERVING = "wefe_serving";

    /****************************** 依赖服务 *************************************/
    public static final String STORAGE = "storage";
    public static final String CLICKHOUSE_STORAGE = "clickhouse_storage";

    /****************************** 函数计算 *************************************/
    public static final String FC_CONFIG = "function_compute_config";
    public static final String ALIYUN_FC_CONFIG = "aliyun_function_compute_config";

    /****************************** 深度学习 *************************************/
    public static final String DEEP_LEARNING_CONFIG = "deep_learning_config";

    /****************************** 计算引擎 *************************************/
    public static final String CALCULATION_ENGINE_CONFIG = "calculation_engine_config";
    public static final String SPARK_STANDALONE_CONFIG = "spark_standalone_config";
}
