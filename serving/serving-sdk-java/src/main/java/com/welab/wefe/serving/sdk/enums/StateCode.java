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

package com.welab.wefe.serving.sdk.enums;

/**
 * @author hunter.zhao
 */
public enum StateCode {

    /**
     * success
     */
    SUCCESS(0, "success"),

    /**
     * System level error codes
     */
    SYSTEM_NOT_BEEN_INITIALIZED(10000, "The system has not been initialized, please perform initialization first."),
    SYSTEM_ERROR(10001, "System error"),

    /**
     * Data error code
     */
    FEATURE_ERROR(20001, "Sample characteristic error");

    private int code;
    private String description;

    StateCode(int code, String message) {
        this.code = code;
        this.description = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return description;
    }

    @Override
    public String toString() {
        return code + "";
    }
}
