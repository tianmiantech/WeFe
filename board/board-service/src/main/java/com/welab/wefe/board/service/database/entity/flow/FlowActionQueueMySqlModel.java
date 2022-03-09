/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.database.entity.flow;

import com.welab.wefe.board.service.database.entity.base.AbstractMySqlModel;
import com.welab.wefe.common.wefe.enums.FlowActionType;
import com.welab.wefe.common.wefe.enums.ProducerType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author Seven
 */
@Entity(name = "flow_action_queue")
public class FlowActionQueueMySqlModel extends AbstractMySqlModel {
    /**
     * 消息生产者;枚举（board/gateway）
     */
    @Enumerated(EnumType.STRING)
    private ProducerType producer;
    /**
     * 优先级;优先级大的会被先消费
     */
    private Integer priority;
    /**
     * 动作名称
     */
    @Enumerated(EnumType.STRING)
    private FlowActionType action;
    /**
     * 动作参数
     */
    private String params;


    //region getter/setter

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

    public FlowActionType getAction() {
        return action;
    }

    public void setAction(FlowActionType action) {
        this.action = action;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }


    //endregion
}
