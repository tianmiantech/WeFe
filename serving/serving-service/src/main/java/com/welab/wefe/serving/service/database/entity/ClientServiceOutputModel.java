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
package com.welab.wefe.serving.service.database.entity;

import com.welab.wefe.common.constant.SecretKeyType;

import javax.persistence.*;
import java.util.UUID;

/**
 * @author ivenn.zheng
 */
@Entity
public class ClientServiceOutputModel {

    @Id
    private String id = UUID.randomUUID().toString().replaceAll("-", "");

    /**
     * 服务名称
     */
    private String serviceName;

    private String serviceId;

    private String clientId;

    /**
     * 客户名称
     */
    private String clientName;

    /**
     * 单价
     */
    private Double unitPrice;

    /**
     * 付费类型
     */
    private Integer payType;

    /**
     * 启用状态
     */
    private Integer status;

    /**
     * 服务类型
     */
    private Integer serviceType;

    /**
     * ip 地址
     */
    private String ipAdd;
    
    private String publicKey;
    private String privateKey;
    private String code;

    @Column(name = "secret_key_type")
    @Enumerated(EnumType.STRING)
    private SecretKeyType secretKeyType = SecretKeyType.rsa;

    /**
     * 请求地址
     */
    private String url;

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

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
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

    public SecretKeyType getSecretKeyType() {
        return secretKeyType;
    }

    public void setSecretKeyType(SecretKeyType secretKeyType) {
        this.secretKeyType = secretKeyType;
    }
}
