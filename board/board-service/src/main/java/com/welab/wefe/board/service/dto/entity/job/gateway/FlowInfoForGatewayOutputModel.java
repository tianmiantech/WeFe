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

package com.welab.wefe.board.service.dto.entity.job.gateway;

import com.welab.wefe.common.enums.JobMemberRole;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane.luo
 */
public class FlowInfoForGatewayOutputModel {

    /**
     * 项目id 非主键
     */
    private String projectId;

    /**
     * 流程id 非主键
     */
    private String flowId;

    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 流程描述
     */
    private String flowDesc;

    /**
     * 我方身份;枚举（promoter/provider）
     */
    @Enumerated(EnumType.STRING)
    private JobMemberRole myRole;

    /**
     * 流程图默认配置
     */
    private String defaultConfig;

    /**
     * 流程图边
     */
    private String edges;

    /**
     * 分组列表
     */
    private String combos;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowDesc() {
        return flowDesc;
    }

    public void setFlowDesc(String flowDesc) {
        this.flowDesc = flowDesc;
    }

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
    }

    public String getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(String defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public String getEdges() {
        return edges;
    }

    public void setEdges(String edges) {
        this.edges = edges;
    }

    public String getCombos() {
        return combos;
    }

    public void setCombos(String combos) {
        this.combos = combos;
    }

}
