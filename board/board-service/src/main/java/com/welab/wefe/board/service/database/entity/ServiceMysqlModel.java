/**
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

package com.welab.wefe.board.service.database.entity;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;

import javax.persistence.Entity;
import java.util.Date;

/**
 * @author lonnie
 */
@Entity(name = "service")
public class ServiceMysqlModel extends AbstractBaseMySqlModel {


    /**
     * 服务id：ip+port(eg：192.168.0.0：8080)
     */
    private String serviceId;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 实例URL
     */
    private String instanceUri;

    /**
     * 可用性检测周期(单位秒)
     */
    private int checkAvailabilityInterval;

    /**
     * 最后心跳时间
     */
    private Date lastHeartbeatTime = new Date();

    /**
     * 最后活动状态
     */
    private String status;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getInstanceUri() {
        return instanceUri;
    }

    public void setInstanceUri(String instanceUri) {
        this.instanceUri = instanceUri;
    }

    public int getCheckAvailabilityInterval() {
        return checkAvailabilityInterval;
    }

    public void setCheckAvailabilityInterval(int checkAvailabilityInterval) {
        this.checkAvailabilityInterval = checkAvailabilityInterval;
    }

    public Date getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public void setLastHeartbeatTime(Date lastHeartbeatTime) {
        this.lastHeartbeatTime = lastHeartbeatTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
