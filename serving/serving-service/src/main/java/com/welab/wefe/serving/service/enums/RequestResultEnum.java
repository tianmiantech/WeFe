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
 * @date 2022/1/24
 */
public enum RequestResultEnum {

    SUCCESS(1, "成功"),

    FAILED(0, "失败");

    private int code;

    private String value;

    RequestResultEnum(int code, String value) {
        this.value = value;
        this.code = code;
    }

    public String getValue() {
        return this.value;
    }

    public int getCode() {
        return this.code;
    }

    public static String getValueByCode(int code) {
        String result = null;
        switch (code) {
            case 1:
                result = RequestResultEnum.SUCCESS.value;
                break;
            case 0:
                result = RequestResultEnum.FAILED.value;
                break;
            default:
                break;
        }
        return result;
    }
}
