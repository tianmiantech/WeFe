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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane.luo
 */
@Entity(name = "project_member_audit")
public class ProjectMemberAuditMySqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = 8870060097218263816L;

    /**
     * 所属项目 Id 项目主键
     */
    private String projectId;

    /**
     * 成员 Id
     */
    private String memberId;

    /**
     * 审核人
     */
    private String auditorId;

    /**
     * 审核结果;枚举值（adopt/disagree）
     */
    @Enumerated(EnumType.STRING)
    private AuditStatus auditResult;
    /**
     * 审核意见
     */
    private String auditComment;

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

    public String getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(String auditorId) {
        this.auditorId = auditorId;
    }

    public AuditStatus getAuditResult() {
        return auditResult;
    }

    public void setAuditResult(AuditStatus auditResult) {
        this.auditResult = auditResult;
    }

    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }

}
