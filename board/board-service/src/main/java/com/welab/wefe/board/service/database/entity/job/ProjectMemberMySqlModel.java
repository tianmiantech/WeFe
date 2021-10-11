/**
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
import com.welab.wefe.common.enums.AuditStatus;
import com.welab.wefe.common.enums.JobMemberRole;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane.luo
 */
@Entity(name = "project_member")
public class ProjectMemberMySqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = -2632889286058354328L;

    /**
     * 邀请方成员Id
     */
    private String inviterId;
    /**
     * 是否是初始化项目时添加进来的（关系到审核流程不同）
     */
    private boolean fromCreateProject;
    /**
     * 所属项目 Id 项目主键
     */
    private String projectId;

    /**
     * 成员 Id
     */
    private String memberId;
    /**
     * 在任务中的角色;枚举（promoter/provider/arbiter）
     */
    @Enumerated(EnumType.STRING)
    private JobMemberRole memberRole;

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
     * 是否已退出
     */
    private Boolean exited = false;

    /**
     * 用于查询成员列表时，按角色排序
     */
    public int getProjectRoleIndex() {
        switch (memberRole) {
            case promoter:
                return 1;
            default:
                return 3;
        }
    }


    //region getter/setter


    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }

    public boolean isFromCreateProject() {
        return fromCreateProject;
    }

    public void setFromCreateProject(boolean fromCreateProject) {
        this.fromCreateProject = fromCreateProject;
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

    public Boolean isExited() {
        return exited;
    }

    public void setExited(Boolean exited) {
        this.exited = exited;
    }

    //endregion

}
