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
package com.welab.wefe.serving.service.dto;

import java.util.Date;

/**
 * @author ivenn.zheng
 * @date 2022/4/28
 *
 * 用于根据下面的参数，查询相应的 service call logs
 */
public class ServiceCallLogInput {

    private String orderId;

    private String serviceId;

    private Integer callByMe;

    private String requestPartnerId;

    private String responsePartnerId;

    private String requestId;

    private String responseId;

    private Date startTime;

    private Date endTime;

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getCallByMe() {
        return callByMe;
    }

    public void setCallByMe(Integer callByMe) {
        this.callByMe = callByMe;
    }

    public String getRequestPartnerId() {
        return requestPartnerId;
    }

    public void setRequestPartnerId(String requestPartnerId) {
        this.requestPartnerId = requestPartnerId;
    }

    public String getResponsePartnerId() {
        return responsePartnerId;
    }

    public void setResponsePartnerId(String responsePartnerId) {
        this.responsePartnerId = responsePartnerId;
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
}
