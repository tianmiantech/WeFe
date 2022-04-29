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
package com.welab.wefe.serving.service.database.serving.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author ivenn.zheng
 * @date 2022/4/27
 */
@Entity(name = "service_order")
public class ServiceOrderMysqlModel extends AbstractBaseMySqlModel {

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "order_type")
    private Integer orderType;

    @Column(name = "status")
    private String status;

    @Column(name = "request_partner_id")
    private String requestPartnerId;

    @Column(name = "request_partner_name")
    private String requestPartnerName;

    @Column(name = "response_partner_id")
    private String responsePartnerId;

    @Column(name = "response_partner_name")
    private String responsePartnerName;


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

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
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
