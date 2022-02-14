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

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.ProjectType;

import java.util.Date;
import java.util.List;

/**
 * @author zane.luo
 */
public class ProjectOutputModel extends AbstractOutputModel {

    @Check(name = "项目ID")
    private String projectId;

    @Check(name = "名称")
    private String name;

    @Check(name = "项目描述")
    private String projectDesc;

    private AuditStatus auditStatus;
    @Check(name = "自己是否同意")
    private AuditStatus auditStatusFromMyself;
    @Check(name = "其他人是否同意")
    private AuditStatus auditStatusFromOthers;
    @Check(name = "审核意见")
    private String auditComment;

    @Check(name = "我方身份;枚举（promoter/provider）")
    private JobMemberRole myRole;

    @Check(name = "是否是创建者")
    private boolean isCreator;

    @Check(name = "我方成员ID")
    private String memberId;

    @Check(name = "消息备注 失败原因/备注")
    private String message;

    private ProjectDetailMemberOutputModel promoter;
    private List<ProjectDetailMemberOutputModel> providerList;
    private List<ProjectDetailMemberOutputModel> promoterList;

    @Check(name = "退出项目的操作者")
    private String exitedBy;
    @Check(name = "退出时间")
    private Date exitedTime;

    @Check(name = "当前成员是否退出了项目")
    private boolean isExited;
    @Check(name = "是否已关闭")
    private boolean closed = false;
    @Check(name = "关闭项目的操作者")
    private String closedBy;
    @Check(name = "关闭时间")
    private Date closedTime;
    private JObject flowStatusStatistics;
    @Check(name = "项目类型")
    private ProjectType projectType;

    public String getExitOperatorNickname() {
        return CacheObjects.getNickname(exitedBy);
    }

    public String getCloseOperatorNickname() {
        return CacheObjects.getNickname(closedBy);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }

    public AuditStatus getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(AuditStatus auditStatus) {
        this.auditStatus = auditStatus;
    }

    public AuditStatus getAuditStatusFromMyself() {
        return auditStatusFromMyself;
    }

    public void setAuditStatusFromMyself(AuditStatus auditStatusFromMyself) {
        this.auditStatusFromMyself = auditStatusFromMyself;
    }

    public AuditStatus getAuditStatusFromOthers() {
        return auditStatusFromOthers;
    }

    public void setAuditStatusFromOthers(AuditStatus auditStatusFromOthers) {
        this.auditStatusFromOthers = auditStatusFromOthers;
    }

    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ProjectDetailMemberOutputModel getPromoter() {
        return promoter;
    }

    public void setPromoter(ProjectDetailMemberOutputModel promoter) {
        this.promoter = promoter;
    }

    public List<ProjectDetailMemberOutputModel> getProviderList() {
        return providerList;
    }

    public void setProviderList(List<ProjectDetailMemberOutputModel> providerList) {
        this.providerList = providerList;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public JObject getFlowStatusStatistics() {
        return flowStatusStatistics;
    }

    public void setFlowStatusStatistics(String flowStatusStatistics) {
        this.flowStatusStatistics = JObject.create(flowStatusStatistics);
    }

    public List<ProjectDetailMemberOutputModel> getPromoterList() {
        return promoterList;
    }

    public void setPromoterList(List<ProjectDetailMemberOutputModel> promoterList) {
        this.promoterList = promoterList;
    }

    public boolean getIsCreator() {
        return isCreator;
    }

    public void setIsCreator(boolean isCreator) {
        this.isCreator = isCreator;
    }

    public String getExitedBy() {
        return exitedBy;
    }

    public void setExitedBy(String exitedBy) {
        this.exitedBy = exitedBy;
    }

    public Date getExitedTime() {
        return exitedTime;
    }

    public void setExitedTime(Date exitedTime) {
        this.exitedTime = exitedTime;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public Date getClosedTime() {
        return closedTime;
    }

    public void setClosedTime(Date closedTime) {
        this.closedTime = closedTime;
    }

    public boolean getIsExited() {
        return isExited;
    }

    public void setIsExited(boolean isExited) {
        this.isExited = isExited;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }
}
