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

package com.welab.wefe.union.service.constant;

public enum CertStatusEnums {

    // -1认证失败 /0未认证 /1认证中 /2已认证
    INVALID(0, "无效"), WAIT_VERIFY(1, "认证中"), VALID(2, "有效");

    private CertStatusEnums(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private int code;
    private String name;

    public static CertStatusEnums getStatus(int code) {
        for (CertStatusEnums e : CertStatusEnums.values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
