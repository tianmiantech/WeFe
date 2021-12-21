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

package com.welab.wefe.board.service.database.entity.chat;

import com.welab.wefe.board.service.database.entity.base.AbstractMySqlModel;
import com.welab.wefe.common.wefe.enums.GatewayActionType;
import com.welab.wefe.common.wefe.enums.ProducerType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Chat message queue list
 *
 * @author Johnny.lin
 */
@Entity(name = "message_queue")
public class MessageQueueMySqlModel extends AbstractMySqlModel {
    /**
     * Message producer; Enumeration (board / gateway)
     */
    @Enumerated(EnumType.STRING)
    private ProducerType producer;
    /**
     * Priority; Those with higher priority will be consumed first
     */
    private Integer priority;
    /**
     * action name
     */
    @Enumerated(EnumType.STRING)
    private GatewayActionType action;

    /**
     * action params
     */
    private String params;

    public ProducerType getProducer() {
        return producer;
    }

    public void setProducer(ProducerType producer) {
        this.producer = producer;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public GatewayActionType getAction() {
        return action;
    }

    public void setAction(GatewayActionType action) {
        this.action = action;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
}
