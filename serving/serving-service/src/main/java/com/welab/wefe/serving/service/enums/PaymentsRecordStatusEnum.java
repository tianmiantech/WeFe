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
 * @date 2022/1/17
 */
public enum PaymentsRecordStatusEnum {

    NORMAL(1, "正常"),

    FLUSHES(2, "冲正");

    private int code;

    private String value;

    PaymentsRecordStatusEnum(int code, String value) {
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
                result = PaymentsRecordStatusEnum.NORMAL.value;
                break;
            case 2:
                result = PaymentsRecordStatusEnum.FLUSHES.value;
                break;
            default:
                break;
        }
        return result;
    }
}
