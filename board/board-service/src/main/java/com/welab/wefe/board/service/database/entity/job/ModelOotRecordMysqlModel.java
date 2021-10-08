/**
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

package com.welab.wefe.board.service.database.entity.job;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;

import javax.persistence.Entity;

/**
 * 模型打分验证记录
 *
 * @author aaron.li
 **/
@Entity(name = "model_oot_record")
public class ModelOotRecordMysqlModel extends AbstractBaseMySqlModel {
    /**
     * 流程ID
     */
    private String flowId;

    /**
     * 被oot的作业ID
     */
    private String ootJobId;
    /**
     * 被oot的模型id
     */
    private String ootModelFlowNodeId;

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getOotJobId() {
        return ootJobId;
    }

    public void setOotJobId(String ootJobId) {
        this.ootJobId = ootJobId;
    }

    public String getOotModelFlowNodeId() {
        return ootModelFlowNodeId;
    }

    public void setOotModelFlowNodeId(String ootModelFlowNodeId) {
        this.ootModelFlowNodeId = ootModelFlowNodeId;
    }
}
