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
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

/**
 * @author zane.luo
 */
public class ProjectMemberOutputModel extends AbstractOutputModel {

    @Check(name = "邀请方成员Id")
    private String inviterId;
    @Check(name = "邀请方成员名称")
    private String inviterName;
    @Check(name = "是否是初始化项目时添加进来的（关系到审核流程不同）")
    private boolean fromCreateProject;
    @Check(name = "所属项目 Id 项目主键")
    private String projectId;

    @Check(name = "成员 Id")
    private String memberId;

    @Check(name = "在任务中的角色;枚举（promoter/provider/arbiter）")
    private JobMemberRole memberRole;

    @Check(name = "综合的审核结果")
    private AuditStatus auditStatus;
    @Check(name = "自己是否同意")
    private AuditStatus auditStatusFromMyself;
    @Check(name = "其他人是否同意")
    private AuditStatus auditStatusFromOthers;

    @Check(name = "审核意见")
    private String auditComment;

    @Check(name = "是否已退出")
    private boolean exited = false;


    public void setInviterId(String inviterId) throws StatusCodeWithException {
        this.inviterId = inviterId;

        // 设置成员名称
        this.inviterName = CacheObjects.getMemberName(inviterId);
    }

    public String getMemberName() {
        return CacheObjects.getMemberName(memberId);
    }

    //region getter/setter


    public boolean isFromCreateProject() {
        return fromCreateProject;
    }

    public void setFromCreateProject(boolean fromCreateProject) {
        this.fromCreateProject = fromCreateProject;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
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

    public String getInviterName() {
        return inviterName;
    }

    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }

    public String getInviterId() {
        return inviterId;
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

    public JobMemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(JobMemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public boolean isExited() {
        return exited;
    }

    public void setExited(boolean exited) {
        this.exited = exited;
    }

    //endregion

}
