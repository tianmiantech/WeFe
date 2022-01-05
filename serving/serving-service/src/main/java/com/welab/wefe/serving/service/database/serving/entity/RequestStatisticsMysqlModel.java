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

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

/**
 * @author ivenn.zheng
 */
@Entity
public class RequestStatisticsMysqlModel{

    @Id
    private String id = UUID.randomUUID().toString().replaceAll("-", "");
    /**
     * 总调用次数
     */
    private Long totalRequestTimes;

    /**
     * 总失败次数
     */
    private Long totalFailTimes;

    /**
     * 总成功次数
     */
    private Long totalSuccessTimes;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 客户名称
     */
    private String clientName;

    /**
     * 服务类型
     */
    private Integer serviceType;

    /**
     * 总耗时
     */
    private long totalSpend;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTotalRequestTimes() {
        return totalRequestTimes;
    }

    public void setTotalRequestTimes(Long totalRequestTimes) {
        this.totalRequestTimes = totalRequestTimes;
    }

    public Long getTotalFailTimes() {
        return totalFailTimes;
    }

    public void setTotalFailTimes(Long totalFailTimes) {
        this.totalFailTimes = totalFailTimes;
    }

    public Long getTotalSuccessTimes() {
        return totalSuccessTimes;
    }

    public void setTotalSuccessTimes(Long totalSuccessTimes) {
        this.totalSuccessTimes = totalSuccessTimes;
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

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }

    public long getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(long totalSpend) {
        this.totalSpend = totalSpend;
    }
}
