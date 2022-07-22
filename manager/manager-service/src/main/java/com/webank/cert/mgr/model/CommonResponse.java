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
package com.webank.cert.mgr.model;

import com.webank.cert.mgr.exception.CertMgrException;

/**
 * @author aaronchu
 */
public class CommonResponse<TBody> {

    private static final int SUCCESS_CODE = 0;

    private int code;

    private String message;

    private TBody data;

    public CommonResponse(int code, String message, TBody data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse success(T data) {
        return new CommonResponse(SUCCESS_CODE, "", data);
    }

    public static CommonResponse fail(Exception error) {
        if (error instanceof CertMgrException) {
            CertMgrException certException = (CertMgrException) error;
            return new CommonResponse(certException.getCodeMessageEnums().getExceptionCode(),
                    certException.getCodeMessageEnums().getExceptionMessage(), null);
        }
        return new CommonResponse(-1, error.getMessage(), null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TBody getData() {
        return data;
    }

    public void setData(TBody data) {
        this.data = data;
    }

}
