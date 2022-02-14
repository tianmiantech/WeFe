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
package com.welab.wefe.common.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;

/**
 * 当对象序列化后输出到日志中时，使用此自定义序列化可以避免输出过长的日志。
 * <p>
 * 使用方法：
 * JSON.toJSONString(result, LoggerSerializeConfig.instance());
 *
 * @author zane
 * @date 2021/11/30
 */
public class LoggerSerializeConfig {
    /**
     * 输出日志时的自定义 json 序列化配置
     */
    private static SerializeConfig LOG_SERIALIZE_CONFIG = new SerializeConfig();

    private LoggerSerializeConfig() {
        LOG_SERIALIZE_CONFIG.put(String.class, new LogCharSequenceSerializer());
    }

    public static SerializeConfig instance() {
        return LOG_SERIALIZE_CONFIG;
    }
}
