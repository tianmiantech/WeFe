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
import com.welab.wefe.common.enums.AuditStatus;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.JobMemberRole;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * @author zane.luo
 */
@Entity(name = "project_data_set")
public class ProjectDataSetMySqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = 7360644396326460699L;

    /**
     * 项目 Id 项目主键
     */
    private String projectId;
    /**
     * 成员id
     */
    private String memberId;
    /**
     * 成员角色
     * <p>
     * 由于存在自己和自己建模的情况，所以需要用角色区分数据集归属。
     */
    @Enumerated(EnumType.STRING)
    private JobMemberRole memberRole;
    /**
     * 数据集 Id
     */
    private String dataSetId;
    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    private AuditStatus auditStatus;
    /**
     * 审核意见
     */
    private String auditComment;
    /**
     * 状态更新时间
     */
    private Date statusUpdatedTime;
    /**
     * 来源组件类型，为空表示原始数据。
     */
    @Enumerated(EnumType.STRING)
    private ComponentType sourceType;
    /**
     * 来源任务id
     */
    private String sourceJobId;
    /**
     * 来源子任务id
     */
    private String sourceTaskId;


    //region getter/setter


    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public JobMemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(JobMemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
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

    public ComponentType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ComponentType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceJobId() {
        return sourceJobId;
    }

    public void setSourceJobId(String sourceJobId) {
        this.sourceJobId = sourceJobId;
    }

    public String getSourceTaskId() {
        return sourceTaskId;
    }

    public void setSourceTaskId(String sourceTaskId) {
        this.sourceTaskId = sourceTaskId;
    }

    //endregion

}
