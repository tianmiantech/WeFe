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

package com.welab.wefe.gateway.entity;

import com.welab.wefe.common.data.mysql.entity.AbstractUniqueIDEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Process action queue entity
 *
 * @author aaron.li
 **/
@Table(name = "flow_action_queue")
@Entity
public class FlowActionQueueEntity extends AbstractUniqueIDEntity {
    /**
     * Creator
     */
    @Column(name = "created_by")
    private String createdBy;
    /**
     * Updater
     */
    @Column(name = "updated_by")
    private String updatedBy;
    /**
     * Message producer
     */
    @Column(name = "producer")
    private String producer;
    /**
     * Those with higher priority will be consumed first. The default value is 0
     */
    @Column(name = "priority")
    private Integer priority;
    /**
     * denomination of dive
     */
    @Column(name = "action")
    private String action;
    /**
     * Action parameters
     */
    @Column(name = "params")
    private String params;

    @Column(name = "channel")
    private String channel;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
