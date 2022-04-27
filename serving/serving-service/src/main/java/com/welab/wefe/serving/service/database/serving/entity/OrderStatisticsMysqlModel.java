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
@Entity(name = "order_statistics")
public class OrderStatisticsMysqlModel extends AbstractBaseMySqlModel {

    @Column(name = "call_times")
    private Integer callTimes;

    @Column(name = "success_times")
    private Integer successTimes;

    @Column(name = "failed_times")
    private Integer failedTimes;

    @Column(name = "minute")
    private String minute;

    @Column(name = "hour")
    private String hour;

    @Column(name = "day")
    private String day;

    @Column(name = "month")
    private String month;

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

    public Integer getCallTimes() {
        return callTimes;
    }

    public void setCallTimes(Integer callTimes) {
        this.callTimes = callTimes;
    }

    public Integer getSuccessTimes() {
        return successTimes;
    }

    public void setSuccessTimes(Integer successTimes) {
        this.successTimes = successTimes;
    }

    public Integer getFailedTimes() {
        return failedTimes;
    }

    public void setFailedTimes(Integer failedTimes) {
        this.failedTimes = failedTimes;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
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
}
