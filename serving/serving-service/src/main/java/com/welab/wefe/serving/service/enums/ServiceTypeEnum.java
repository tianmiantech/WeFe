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
 * @date 2022/1/12
 */
public enum ServiceTypeEnum {

    PIR(1, "两方匿踪查询"), PSI(2, "两方交集查询"),

    SA(3, "多方安全统计(被查询方)"), MULTI_SA(4, "多方安全统计(查询方)"),

    MULTI_PSI(5, "多方交集查询"), MULTI_PIR(6, "多方匿踪查询"),

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
                result = ServiceTypeEnum.PSI.value;
                break;
            case 3:
                result = ServiceTypeEnum.SA.value;
                break;
            case 4:
                result = ServiceTypeEnum.MULTI_SA.value;
                break;
            case 5:
                result = ServiceTypeEnum.MULTI_PSI.value;
                break;
            case 6:
                result = ServiceTypeEnum.MULTI_PIR.value;
                break;
            default:
                break;
        }
        return result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static boolean checkServiceType(int serviceType) {
        for (ServiceTypeEnum enu : ServiceTypeEnum.values()) {
            if (enu.code == serviceType) {
                return true;
            }
        }
        return false;
    }

    public static boolean needDataSource(int serviceType) {
        return serviceType == PIR.code || serviceType == PSI.code || serviceType == SA.code;
    }

    public static boolean needServiceConfig(int serviceType) {
        return serviceType == MULTI_SA.code || serviceType == MULTI_PSI.code || serviceType == MULTI_PIR.code;
    }

}
