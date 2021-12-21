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
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.ProjectType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * @author zane.luo
 */
@Entity(name = "project")
public class ProjectMySqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = -2632889286058354328L;

    /**
     * 是否已删除
     */
    private boolean deleted;
    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String projectDesc;

    /**
     * 综合的审核结果
     */
    @Enumerated(EnumType.STRING)
    private AuditStatus auditStatus;
    /**
     * 自己是否同意
     */
    @Enumerated(EnumType.STRING)
    private AuditStatus auditStatusFromMyself;
    /**
     * 其他人是否同意
     */
    @Enumerated(EnumType.STRING)
    private AuditStatus auditStatusFromOthers;
    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 我方身份;枚举（promoter/provider）
     */
    @Enumerated(EnumType.STRING)
    private JobMemberRole myRole;

    /**
     * 该项目的创建者ID
     */
    private String memberId;

    /**
     * 状态更新时间
     */
    private Date statusUpdatedTime;

    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date finishTime;

    /**
     * 进度
     */
    private Integer progress;
    /**
     * 进度更新时间
     */
    private Date progressUpdatedTime;

    /**
     * 消息备注 失败原因/备注
     */
    private String message;
    /**
     * 是否已退出
     */
    private boolean exited = false;
    /**
     * 退出项目的操作者
     */
    private String exitedBy;
    /**
     * 退出时间
     */
    private Date exitedTime;
    /**
     * 是否已关闭
     */
    private boolean closed = false;
    /**
     * 关闭项目的操作者
     */
    private String closedBy;
    /**
     * 关闭时间
     */
    private Date closedTime;

    /**
     * 流程状态统计字段
     */
    private String flowStatusStatistics;

    /**
     * 项目类型
     */
    @Enumerated(EnumType.STRING)
    private ProjectType projectType;

    //region getter/setter

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
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

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public boolean isExited() {
        return exited;
    }

    public void setExited(boolean exited) {
        this.exited = exited;
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

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getFlowStatusStatistics() {
        return flowStatusStatistics;
    }

    public void setFlowStatusStatistics(String flowStatusStatistics) {
        this.flowStatusStatistics = flowStatusStatistics;
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

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    //endregion
}
