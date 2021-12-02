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

package com.welab.wefe.board.service.dto.entity;

import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.enums.AuditStatus;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * @author zane.luo
 */
public class ProjectMemberAuditOutput extends AbstractOutputModel {
    @Check(name = "所属项目 Id 项目主键")
    private String projectId;

    @Check(name = "成员 Id")
    private String memberId;

    @Check(name = "审核人")
    private String auditorId;

    @Check(name = "审核结果;枚举值（adopt/disagree）")
    private AuditStatus auditResult;
    @Check(name = "审核意见")
    private String auditComment;

    public String getAuditorName() {
        return CacheObjects.getMemberName(auditorId);
    }

    //region getter/setter

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


    //endregion
}
