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

package com.welab.wefe.board.service.dto.entity;

import com.welab.wefe.common.enums.AuditStatus;
import com.welab.wefe.common.util.Masker;
import com.welab.wefe.common.web.CurrentAccount;

/**
 * @author Zane
 */
public class AccountOutputModel extends AbstractOutputModel {

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 昵称
     */
    private String nickname;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 是否是超级管理员;超级管理员通常是第一个创建并初始化系统的那个人
     */
    private Boolean superAdminRole;
    /**
     * 是否是管理员;管理员有更多权限，比如设置 member 是否对外可见。
     */
    private Boolean adminRole;
    /**
     * 审核状态
     */
    private AuditStatus auditStatus;
    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 是否可用
     */
    private Boolean enable;

    public String getEmail() {
        if (!CurrentAccount.isAdmin()) {
            return "";
        } else {
            return Masker.maskEmail(email);
        }
    }

    public String getPhoneNumber() {
        if (!CurrentAccount.isAdmin()) {
            return "";
        } else {
            return Masker.maskPhoneNumber(phoneNumber);
        }
    }

    //region getter/setter


    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getSuperAdminRole() {
        return superAdminRole;
    }

    public void setSuperAdminRole(Boolean superAdminRole) {
        this.superAdminRole = superAdminRole;
    }

    public Boolean getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(Boolean adminRole) {
        this.adminRole = adminRole;
    }

    public AuditStatus getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(AuditStatus auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    //endregion

}
