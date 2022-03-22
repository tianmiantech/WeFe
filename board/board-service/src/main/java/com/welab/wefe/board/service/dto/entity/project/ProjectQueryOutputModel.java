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
public class ProjectQueryOutputModel extends AbstractOutputModel {
    @Check(name = "项目ID")
    private String projectId;

    @Check(name = "名称")
    private String name;

    @Check(name = "项目描述")
    private String projectDesc;

    private AuditStatus auditStatus;

    @Check(name = "我方身份;枚举（promoter/provider）")
    private JobMemberRole myRole;

    @Check(name = "我方成员ID")
    private String memberId;

    @Check(name = "状态更新时间")
    private Date statusUpdatedTime;

    @Check(name = "开始时间")
    private Date startTime;
    @Check(name = "结束时间")
    private Date finishTime;

    @Check(name = "进度")
    private Integer progress;
    @Check(name = "进度更新时间")
    private Date progressUpdatedTime;

    @Check(name = "消息备注 失败原因/备注")
    private String message;

    private List<ProjectMemberOutputModel> memberList;
    @Check(name = "发起方ID")
    private String promoter;

    @Check(name = "发起方name")
    private String promoterName;

    @Check(name = "退出项目的操作者")
    private String exitedBy;
    @Check(name = "退出时间")
    private Date exitedTime;
    @Check(name = "是否已关闭")
    private boolean closed = false;
    @Check(name = "关闭项目的操作者")
    private String closedBy;
    @Check(name = "关闭时间")
    private Date closedTime;

    @Check(name = "各流程状态的统计")
    private JObject flowStatusStatistics;

    @Check(name = "待审核数据集数量")
    private int needMeAuditDataSetCount;
    @Check(name = "项目类型")
    private ProjectType projectType;


    public String getExitOperatorNickname() {
        return CacheObjects.getNickname(exitedBy);
    }

    public String getCloseOperatorNickname() {
        return CacheObjects.getNickname(closedBy);
    }

    //region getter/setter


    public List<ProjectMemberOutputModel> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<ProjectMemberOutputModel> memberList) {
        this.memberList = memberList;
    }

    public String getPromoter() {
        return promoter;
    }

    public void setPromoter(String promoter) {
        this.promoter = promoter;
    }

    public String getPromoterName() {
        return promoterName;
    }

    public void setPromoterName(String promoterName) {
        this.promoterName = promoterName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Date getStatusUpdatedTime() {
        return statusUpdatedTime;
    }

    public void setStatusUpdatedTime(Date statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Date getProgressUpdatedTime() {
        return progressUpdatedTime;
    }

    public void setProgressUpdatedTime(Date progressUpdatedTime) {
        this.progressUpdatedTime = progressUpdatedTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public int getNeedMeAuditDataSetCount() {
        return needMeAuditDataSetCount;
    }

    public void setNeedMeAuditDataSetCount(int needMeAuditDataSetCount) {
        this.needMeAuditDataSetCount = needMeAuditDataSetCount;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    //endregion

}
