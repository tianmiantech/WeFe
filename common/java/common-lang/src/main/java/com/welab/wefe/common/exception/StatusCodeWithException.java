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

package com.welab.wefe.common.exception;


import com.welab.wefe.common.StatusCode;

/**
 * @author Zane
 */
public class StatusCodeWithException extends Exception {
    public static StatusCodeWithException of(StatusCode statusCode, String message) {
        return new StatusCodeWithException(statusCode, message);
    }

    /**
     * 意料之外的枚举项
     */
    public static StatusCodeWithException ofUnexpectedEnumCase(Enum aEnum) {
        StatusCode code = StatusCode.UNEXPECTED_ENUM_CASE;
        String message = code.getMessage(aEnum.name());
        return new StatusCodeWithException(code, message);
    }


    private StatusCode statusCode;

    public StatusCodeWithException(StatusCode statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
    }

    public StatusCodeWithException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "code: " + this.statusCode + ", message:" + this.getMessage();
    }
}
