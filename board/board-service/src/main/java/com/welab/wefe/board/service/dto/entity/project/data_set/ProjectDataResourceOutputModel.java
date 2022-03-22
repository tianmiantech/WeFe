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

package com.welab.wefe.board.service.dto.entity.project.data_set;

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.DataResourceOutputModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

import java.util.Date;

/**
 * @author zane.luo
 */
public class ProjectDataResourceOutputModel extends AbstractOutputModel {

    @Check(name = "项目 Id 项目主键")
    private String projectId;
    @Check(name = "成员id")
    private String memberId;
    @Check(name = "成员角色", desc = "由于存在自己和自己建模的情况，所以需要用角色区分数据集归属。")
    private JobMemberRole memberRole;
    @Check(name = "数据集 Id")
    private String dataSetId;
    @Check(name = "状态")
    private AuditStatus auditStatus;
    @Check(name = "审核意见")
    private String auditComment;
    @Check(name = "状态更新时间")
    private Date statusUpdatedTime;
    @Check(name = "数据集类型")
    private DataResourceType dataResourceType;
    @Check(name = "数据集详情")
    private DataResourceOutputModel dataResource;

    //region getter/setter


    public JobMemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(JobMemberRole memberRole) {
        this.memberRole = memberRole;
    }

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

    public DataResourceOutputModel getDataResource() {
        return dataResource;
    }

    public void setDataResource(DataResourceOutputModel dataResource) {
        this.dataResource = dataResource;
    }

    public DataResourceType getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(DataResourceType dataResourceType) {
        this.dataResourceType = dataResourceType;
    }
    //endregion

}
