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
 * @author hunter.zhao
 * @date 2022/6/24
 */
public enum CallByMeEnum {
    YES(1, "是"),

    NO(0, "否");

    private int code;
    private String value;

    public static CallByMeEnum getByCode(int code) {
        CallByMeEnum result = null;
        switch (code) {
            case 0:
                result = CallByMeEnum.NO;
                break;
            case 1:
                result = CallByMeEnum.YES;
                break;
            default:
                break;
        }
        return result;
    }

    CallByMeEnum(int code, String value) {
        this.value = value;
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
