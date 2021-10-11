/**
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

package com.welab.wefe.common.web.dto;


import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.common.StatusCode;

/**
 * @author Zane
 */
public class ApiResult<T> {
    public int code = 0;
    public String message;
    public T data;
    public long spend;
    /**
     * The HTTP response code
     */
    @JSONField(serialize = false)
    public int httpCode = 200;


    public static ApiResult<Object> ofErrorWithStatusCode(StatusCode statusCode) {
        return ofErrorWithStatusCode(statusCode, statusCode.getMessage());
    }

    public static ApiResult<Object> ofErrorWithStatusCode(StatusCode statusCode, String message) {
        ApiResult<Object> response = new ApiResult<>();
        response.code = statusCode.getCode();
        response.message = message;
        return response;
    }

    public static ApiResult<Object> ofSuccess(Object data) {
        ApiResult<Object> response = new ApiResult<>();
        response.data = data;
        return response;
    }


    public ApiResult<T> setHttpCode(int httpCode) {
        this.httpCode = httpCode;
        return this;
    }

    public ApiResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean success() {
        return code == 0;
    }

    //region getter/setter

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getSpend() {
        return spend;
    }

    public void setSpend(long spend) {
        this.spend = spend;
    }


    //endregion

}
