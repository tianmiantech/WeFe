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

package com.welab.wefe.board.service.database.entity.job;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.ProjectFlowStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * @author zane.luo
 */
@Entity(name = "project_flow")
public class ProjectFlowMySqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = -2604406277609318163L;

    /**
     * 是否已被删除
     */
    private boolean deleted = false;
    /**
     * 联邦任务类型（横向/纵向）
     */
    @Enumerated(EnumType.STRING)
    private FederatedLearningType federatedLearningType;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 流程ID
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
     * 画布中编辑的图
     */
    private String graph;
    /**
     * 创建此流程的成员的ID
     */
    private String creatorMemberId;

    /**
     * 流程的状态
     */
    @Enumerated(EnumType.STRING)
    private ProjectFlowStatus flowStatus;
    private Date statusUpdatedTime;
    private String message;
    /**
     * 我方角色
     */
    @Enumerated(EnumType.STRING)
    private JobMemberRole myRole;


    //region getter/setter


    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public FederatedLearningType getFederatedLearningType() {
        return federatedLearningType;
    }

    public void setFederatedLearningType(FederatedLearningType federatedLearningType) {
        this.federatedLearningType = federatedLearningType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public ProjectFlowStatus getFlowStatus() {
        return flowStatus;
    }

    public void setFlowStatus(ProjectFlowStatus flowStatus) {
        this.flowStatus = flowStatus;
    }

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
    }

    public Date getStatusUpdatedTime() {
        return statusUpdatedTime;
    }

    public void setStatusUpdatedTime(Date statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
    }

    public String getCreatorMemberId() {
        return creatorMemberId;
    }

    public void setCreatorMemberId(String creatorMemberId) {
        this.creatorMemberId = creatorMemberId;
    }

    //endregion

}
