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

package com.welab.wefe.serving.service.enums;

/**
 * @author ivenn.zheng
 */

public enum ServiceStatusEnum {

    USED(1, "已启用"),

    UNUSED(0, "未启用");

    private int code;
    private String message;

    ServiceStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static String getValueByCode(int code) {
        String result = null;
        switch (code) {
            case 0:
                result = ServiceStatusEnum.UNUSED.message;
                break;
            case 1:
                result = ServiceStatusEnum.USED.message;
                break;
            default:
                break;
        }
        return result;
    }

    public static int getCodeByValue(String value) {
        int result = 0;
        switch (value) {
            case "已启用":
                result = ServiceStatusEnum.USED.code;
                break;
            case "未启用":
                result = ServiceStatusEnum.UNUSED.code;
                break;
            default:
                break;
        }
        return result;
    }

}
