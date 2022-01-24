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

package com.welab.wefe.serving.service.database.serving.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @author ivenn.zheng
 */
@Entity(name = "fee_detail")
public class FeeDetailMysqlModel extends AbstractBaseMySqlModel {

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "service_type")
    private Integer serviceType;

    @Column(name = "client_name")
    private String clientName;

    /**
     * total fee
     */
    @Column(name = "total_fee")
    private BigDecimal totalFee;

    @Column(name = "total_request_times")
    private Long totalRequestTimes;

    /**
     * unit price
     */
    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "fee_config_id")
    private String feeConfigId;


    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getFeeConfigId() {
        return feeConfigId;
    }

    public void setFeeConfigId(String feeConfigId) {
        this.feeConfigId = feeConfigId;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public Long getTotalRequestTimes() {
        return totalRequestTimes;
    }

    public void setTotalRequestTimes(Long totalRequestTimes) {
        this.totalRequestTimes = totalRequestTimes;
    }
}
