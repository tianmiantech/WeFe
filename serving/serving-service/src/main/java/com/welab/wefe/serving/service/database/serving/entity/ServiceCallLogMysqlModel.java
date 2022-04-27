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
@Entity(name = "service_call_log")
public class ServiceCallLogMysqlModel extends AbstractBaseMySqlModel {

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "call_by_me")
    private Integer callByMe;

    @Column(name = "request_partner_id")
    private String requestPartnerId;

    @Column(name = "request_partner_name")
    private String requestPartnerName;

    @Column(name = "response_partner_id")
    private String responsePartnerId;

    @Column(name = "response_partner_name")
    private String responsePartnerName;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "response_id")
    private String responseId;

    @Column(name = "request_data")
    private String requestData;

    @Column(name = "response_data")
    private String responseData;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_status")
    private String responseStatus;

    @Column(name = "request_ip")
    private String requestIp;

    @Column(name = "spend_time")
    private Integer spendTime;


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public Integer getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(Integer spendTime) {
        this.spendTime = spendTime;
    }
}
