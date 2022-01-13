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
 * @date 2022/1/12
 */
public enum ServiceTypeEnum {

    PIR(1, "匿踪查询"),

    MULTI_PIR(2, "多方匿踪查询"),

    PSI(3, "安全求交"),

    MULTI_PSI(4, "多方安全求交"),

    MULTI_SA(5, "多方安全聚合"),


    ;

    private int code;

    private String value;


    ServiceTypeEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }


    public static String getValue(int code) {

        String result = null;
        switch (code) {
            case 1:
                result = ServiceTypeEnum.PIR.value;
                break;
            case 2:
                result = ServiceTypeEnum.MULTI_PIR.value;
                break;
            case 3:
                result = ServiceTypeEnum.PSI.value;
                break;
            case 4:
                result = ServiceTypeEnum.MULTI_PSI.value;
                break;
            case 5:
                result = ServiceTypeEnum.MULTI_SA.value;
                break;
            default:
                break;
        }
        return result;
    }

}
