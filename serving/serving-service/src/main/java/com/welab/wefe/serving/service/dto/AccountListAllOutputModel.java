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

package com.welab.wefe.serving.service.dto;


import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.AuditStatus;

/**
 * @author Zane
 */
public class AccountListAllOutputModel {
    @Check(name = "昵称")
    private String nickname;
    @Check(name = "是否是超级管理员;超级管理员通常是第一个创建并初始化系统的那个人")
    private Boolean superAdminRole;
    @Check(name = "是否是管理员;管理员有更多权限，比如设置 member 是否对外可见。")
    private Boolean adminRole;
    @Check(name = "审核状态")
    private AuditStatus auditStatus;
    @Check(name = "是否可用")
    private Boolean enable;
    /**
     * 是否已注销
     */
    private boolean cancelled;

    //region getter/setter

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
