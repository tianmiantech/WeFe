/*
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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
package com.welab.wefe.serving.service.enums;

/**
 * @author ivenn.zheng
 * @date 2022/5/26
 */
public enum ServiceCallStatusEnum {

    SUCCESS("成功"),
    REQUEST_TIMEOUT("请求超时"),
    RESPONSE_TIMEOUT("响应超时"),
    CONNECTION_ERROR("请求不通"),
    RESPONSE_ERROR("响应错误");

    private String value;

    ServiceCallStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
