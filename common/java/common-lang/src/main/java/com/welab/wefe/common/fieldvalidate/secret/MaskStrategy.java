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
package com.welab.wefe.common.fieldvalidate.secret;

import com.welab.wefe.common.util.Masker;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zane
 * @date 2022/7/7
 */
public enum MaskStrategy {
    /**
     * 阻止字段输出任何字节，输出 null。
     */
    BLOCK,
    /**
     * 密码，固定长度的星号。
     */
    PASSWORD,
    PHONE_NUMBER,
    EMAIL;

    private static final Map<MaskStrategy, Function<String, String>> FUNCTION_MAP = new HashMap<>();

    static {
        FUNCTION_MAP.put(BLOCK, x -> null);
        FUNCTION_MAP.put(PASSWORD, x -> "***************");
        FUNCTION_MAP.put(PHONE_NUMBER, Masker::maskPhoneNumber);
        FUNCTION_MAP.put(EMAIL, Masker::maskEmail);
    }

    public String get(Object value) {
        if (value == null) {
            return null;
        }

        Function<String, String> function = FUNCTION_MAP.get(this);
        if (function == null) {
            throw new RuntimeException("意料之外的枚举：" + this);
        }

        String str = String.valueOf(value);
        return function.apply(str);


    }
}
