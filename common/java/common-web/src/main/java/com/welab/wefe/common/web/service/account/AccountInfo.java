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
package com.welab.wefe.common.web.service.account;

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.common.wefe.enums.AuditStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zane
 * @date 2022/3/16
 */
public class AccountInfo {
    public String id;
    /**
     * 用户名
     */
    public String phoneNumber;
    /**
     * 昵称
     */
    public String nickname;
    /**
     * 密码
     */
    public String password;
    /**
     * 密码的加密盐
     */
    public String salt;
    /**
     * 账号的审核状态
     */
    public AuditStatus auditStatus;
    /**
     * 审核评论
     */
    public String auditComment;
    /**
     * 是否是管理员
     */
    public boolean adminRole;
    /**
     * 是否是超级管理员
     */
    public boolean superAdminRole;
    /**
     * 是否处于启用状态
     */
    public boolean enable;
    /**
     * 账号是否已被注销
     */
    public boolean cancelled;
    /**
     * 曾经使用过的密码
     */
    public List<HistoryPasswordItem> historyPasswordList;

    public void setHistoryPasswordList(JSONArray array) {
        if (array == null) {
            this.historyPasswordList = new ArrayList<>();
        } else {
            this.historyPasswordList = array.toJavaList(HistoryPasswordItem.class);
        }

    }

    /**
     * 获取
     */
    public List<HistoryPasswordItem> getPasswordHistoryList(int count) {
        if (this.historyPasswordList == null) {
            return new ArrayList<>();
        }
        return historyPasswordList.subList(Math.max(historyPasswordList.size() - count, 0), historyPasswordList.size());
    }

    // region getter/setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public boolean isAdminRole() {
        return adminRole;
    }

    public void setAdminRole(boolean adminRole) {
        this.adminRole = adminRole;
    }

    public boolean isSuperAdminRole() {
        return superAdminRole;
    }

    public void setSuperAdminRole(boolean superAdminRole) {
        this.superAdminRole = superAdminRole;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public List<HistoryPasswordItem> getHistoryPasswordList() {
        return historyPasswordList;
    }

    public void setPasswordHistoryList(List<HistoryPasswordItem> passwordHistoryList) {
        this.historyPasswordList = passwordHistoryList;
    }

    // endregion
}
