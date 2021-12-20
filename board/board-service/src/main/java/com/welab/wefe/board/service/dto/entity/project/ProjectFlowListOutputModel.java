/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.dto.entity.project;

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.ProjectFlowStatus;

import java.util.Date;

/**
 * @author zane.luo
 */
public class ProjectFlowListOutputModel extends AbstractOutputModel {
    @Check(name = "是否已被删除")
    private Boolean deleted;
    @Check(name = "联邦任务类型（横向/纵向）")
    private FederatedLearningType federatedLearningType;
    @Check(name = "项目ID")
    private String projectId;
    @Check(name = "流程ID")
    private String flowId;
    @Check(name = "流程名称")
    private String flowName;
    @Check(name = "流程描述")
    private String flowDesc;

    @Check(name = "流程的状态")
    private ProjectFlowStatus flowStatus;
    private Date statusUpdatedTime;
    private String message;
    @Check(name = "我方角色")
    private JobMemberRole myRole;
    @Check(name = "是否是创建者")
    private boolean isCreator;
    @Check(name = "任务进度")
    private Integer jobProgress;
    @Check(name = "创建此流程的成员的ID")
    private String creatorMemberId;

    public String getCreatorMemberName() {
        return CacheObjects.getMemberName(creatorMemberId);
    }

    //region getter/setter


    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
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

    public Integer getJobProgress() {
        return jobProgress;
    }

    public void setJobProgress(Integer jobProgress) {
        this.jobProgress = jobProgress;
    }

    public boolean getIsCreator() {
        return isCreator;
    }

    public void setIsCreator(boolean isCreator) {
        this.isCreator = isCreator;
    }

    public String getCreatorMemberId() {
        return creatorMemberId;
    }

    public void setCreatorMemberId(String creatorMemberId) {
        this.creatorMemberId = creatorMemberId;
    }

    //endregion

}
