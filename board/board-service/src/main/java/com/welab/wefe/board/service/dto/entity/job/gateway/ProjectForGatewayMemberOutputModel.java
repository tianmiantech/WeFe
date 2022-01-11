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

package com.welab.wefe.board.service.dto.entity.job.gateway;


import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane.luo
 */
public class ProjectForGatewayMemberOutputModel {

    @Check(name = "成员 Id")
    private String memberId;
    @Check(name = "成员名称")
    private String memberName;

    /**
     * 在任务中的角色;枚举（promoter/provider/arbiter）
     */
    @Enumerated(EnumType.STRING)
    private JobMemberRole memberRole;

    /**
     * 审核结果;枚举值（adopt/disagree）
     */
    @Enumerated(EnumType.STRING)
    private AuditStatus auditResult;
    @Check(name = "审核意见")
    private String auditComment;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public JobMemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(JobMemberRole memberRole) {
        this.memberRole = memberRole;
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
