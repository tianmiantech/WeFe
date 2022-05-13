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

import com.welab.wefe.serving.service.enums.ServiceClientTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceStatusEnum;

@Entity(name = "client_service")
public class ClientServiceMysqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = -2660433592111729597L;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "service_type")
    private Integer serviceType;
    /**
     * status: false means unused, true means used, default = 0
     */
    private Integer status = ServiceStatusEnum.UNUSED.getCode();

    @Column(name = "ip_add")
    private String ipAdd;

    private String url;

    @Column(name = "pay_type")
    private Integer payType;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "public_key")
    private String publicKey;
    
    @Column(name = "private_key")
    private String privateKey;
    
    @Column(name = "code")
    private String code;

    @Column(name = "type")
    private int type = ServiceClientTypeEnum.OPEN.getValue();

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getIpAdd() {
        return ipAdd;
    }

    public void setIpAdd(String ipAdd) {
        this.ipAdd = ipAdd;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
