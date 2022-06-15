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

package com.welab.wefe.common.web.dto;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.fastjson.LoggerValueFilter;

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


    public static <T> ApiResult<T> ofErrorWithStatusCode(StatusCode statusCode) {
        return ofErrorWithStatusCode(statusCode, statusCode.getMessage());
    }

    public static <T> ApiResult<T> ofErrorWithStatusCode(StatusCode statusCode, String message) {
        ApiResult<T> response = new ApiResult<>();
        response.code = statusCode.getCode();
        response.message = message;
        return response;
    }

    public static <T> ApiResult<T> ofSuccess(T data) {
        ApiResult<T> response = new ApiResult<>();
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

    /**
     * 获取 ApiResult 用于输出日志文件的内容
     * <p>
     * 警告 ⚠️:
     * 当响应内容为 ResponseEntity<FileSystemResource> 时
     * JSON.toJSONString(result) 序列化时会导致文件被置空
     * 所以这里写日志时务必使用 LoggerValueFilter，序列化时对 value 进行自定义处理。
     *
     * @param omitLog 是否要省略此次日志打印，以减少磁盘使用。
     */
    public String toLogString(boolean omitLog) {
        if (omitLog) {
            ApiResult<Object> copy = ApiResult.ofSuccess(null);
            copy.spend = this.spend;
            copy.code = this.code;
            copy.message = this.message;
            copy.httpCode = this.httpCode;
            return JSON.toJSONString(copy);
        } else {
            return JSON.toJSONString(this, LoggerValueFilter.DEFAULT);
        }
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
