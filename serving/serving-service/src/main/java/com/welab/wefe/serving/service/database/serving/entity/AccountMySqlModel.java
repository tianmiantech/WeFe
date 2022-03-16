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

package com.welab.wefe.serving.service.database.serving.entity;

import com.welab.wefe.common.wefe.enums.AuditStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author hunter.zhao
 */
@Entity(name = "account")
public class AccountMySqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = -6835962000573567824L;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String password;

    private String salt;

    private String nickname;

    private String email;
    /**
     * Is the super administrator? The super administrator is usually the first person to create and initialize the system
     */
    @Column(name = "super_admin_role")
    private Boolean superAdminRole;
    /**
     * Is it an administrator? Administrators have more rights, such as setting whether member is visible.
     */
    @Column(name = "admin_role")
    private Boolean adminRole;

    /**
     * 审核状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "audit_status")
    private AuditStatus auditStatus;
    /**
     * 审核意见
     */
    @Column(name = "audit_comment")
    private String auditComment;

    /**
     * 是否可用
     */
    private Boolean enable;

    /**
     * 是否已注销
     */
    private boolean cancelled = false;

    //region getter/setter

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
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

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    //endregion
}
