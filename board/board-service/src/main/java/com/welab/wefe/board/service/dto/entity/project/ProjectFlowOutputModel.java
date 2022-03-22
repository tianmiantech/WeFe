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

package com.welab.wefe.board.service.dto.entity.project;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.DeepLearningJobType;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.ProjectFlowStatus;

import java.util.Date;

/**
 * @author zane.luo
 */
public class ProjectFlowOutputModel extends AbstractOutputModel {
    @Check(name = "是否已被删除")
    private Boolean deleted;
    @Check(name = "联邦任务类型（横向/纵向）")
    private FederatedLearningType federatedLearningType;
    @Check(name = "深度学习任务类型（目标检测、图像分类）")
    private DeepLearningJobType deepLearningJobType;
    @Check(name = "项目ID")
    private String projectId;
    @Check(name = "流程ID")
    private String flowId;
    @Check(name = "流程名称")
    private String flowName;
    @Check(name = "流程描述")
    private String flowDesc;
    @Check(name = "画布中编辑的图")
    private JSONObject graph;
    @Check(name = "创建此流程的成员的ID")
    private String creatorMemberId;

    @Check(name = "流程的状态")
    private ProjectFlowStatus flowStatus;
    private Date statusUpdatedTime;
    private String message;
    @Check(name = "我方角色")
    private JobMemberRole myRole;
    @Check(name = "是否置顶")
    private boolean top;

    private ProjectModelingOutputModel projectModelingOutputModel;

    public void setGraph(String graph) {
        // 将字符串序列化为 json 后再输出
        if (graph != null) {
            this.graph = JSON.parseObject(graph);
        }
    }

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

    public DeepLearningJobType getDeepLearningJobType() {
        return deepLearningJobType;
    }

    public void setDeepLearningJobType(DeepLearningJobType deepLearningJobType) {
        this.deepLearningJobType = deepLearningJobType;
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

    public JSONObject getGraph() {
        return graph;
    }

    public ProjectFlowStatus getFlowStatus() {
        return flowStatus;
    }

    public void setFlowStatus(ProjectFlowStatus flowStatus) {
        this.flowStatus = flowStatus;
    }

    public Date getStatusUpdatedTime() {
        return statusUpdatedTime;
    }

    public void setStatusUpdatedTime(Date statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
    }

    public ProjectModelingOutputModel getProjectModelingOutputModel() {
        return projectModelingOutputModel;
    }

    public void setProjectModelingOutputModel(ProjectModelingOutputModel projectModelingOutputModel) {
        this.projectModelingOutputModel = projectModelingOutputModel;
    }

    public String getCreatorMemberId() {
        return creatorMemberId;
    }

    public void setCreatorMemberId(String creatorMemberId) {
        this.creatorMemberId = creatorMemberId;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }
    //endregion
}
