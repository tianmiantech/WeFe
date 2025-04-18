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
package com.welab.wefe.serving.service.dto;

import java.util.UUID;

/**
 * @author hunter.zhao
 */
public class ServiceResultOutput {

    private String requestId;

    private String responseId;

    private Object data;


    public static String buildId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static ServiceResultOutput of(String requestId, String responseId, Object data) {
        ServiceResultOutput output = new ServiceResultOutput();
        output.requestId = requestId;
        output.responseId = responseId;
        output.data = data;

        return output;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
