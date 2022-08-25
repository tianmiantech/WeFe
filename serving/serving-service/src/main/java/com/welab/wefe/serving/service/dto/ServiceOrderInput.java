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
 */
public class ServiceOrderInput {

    private String serviceId;

    private String serviceName;

    private String status;

    private Integer orderType;

    private String requestPartnerId;

    private String requestPartnerName;

    private String responsePartnerId;

    private String responsePartnerName;

    private Date createdStartTime;

    private Date createdEndTime;

    private Date updatedStartTime;

    private Date updatedEndTime;

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Date getCreatedStartTime() {
        return createdStartTime;
    }

    public void setCreatedStartTime(Date createdStartTime) {
        this.createdStartTime = createdStartTime;
    }

    public Date getCreatedEndTime() {
        return createdEndTime;
    }

    public void setCreatedEndTime(Date createdEndTime) {
        this.createdEndTime = createdEndTime;
    }

    public Date getUpdatedStartTime() {
        return updatedStartTime;
    }

    public void setUpdatedStartTime(Date updatedStartTime) {
        this.updatedStartTime = updatedStartTime;
    }

    public Date getUpdatedEndTime() {
        return updatedEndTime;
    }

    public void setUpdatedEndTime(Date updatedEndTime) {
        this.updatedEndTime = updatedEndTime;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestPartnerId() {
        return requestPartnerId;
    }

    public void setRequestPartnerId(String requestPartnerId) {
        this.requestPartnerId = requestPartnerId;
    }

    public String getRequestPartnerName() {
        return requestPartnerName;
    }

    public void setRequestPartnerName(String requestPartnerName) {
        this.requestPartnerName = requestPartnerName;
    }

    public String getResponsePartnerId() {
        return responsePartnerId;
    }

    public void setResponsePartnerId(String responsePartnerId) {
        this.responsePartnerId = responsePartnerId;
    }

    public String getResponsePartnerName() {
        return responsePartnerName;
    }

    public void setResponsePartnerName(String responsePartnerName) {
        this.responsePartnerName = responsePartnerName;
    }
}
