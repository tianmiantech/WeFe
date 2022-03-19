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

package com.welab.wefe.common.data.mongodb.entity.manager;

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.USER)
public class User extends AbstractNormalMongoModel {
    private String userId = UUID.randomUUID().toString().replaceAll("-", "");
    private String account;
    private String password;
    private String realname;
    private String salt;
    private String email;
    private JSONArray historyPasswordList;

    private boolean superAdminRole;

    private boolean adminRole;


    private boolean enable = true;

    private boolean cancelled = false;


    private AuditStatus auditStatus;

    private String auditComment;

    private boolean needUpdatePassword;


    private Date lastActionTime;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSuperAdminRole() {
        return superAdminRole;
    }

    public void setSuperAdminRole(boolean superAdminRole) {
        this.superAdminRole = superAdminRole;
    }

    public boolean isAdminRole() {
        return adminRole;
    }

    public void setAdminRole(boolean adminRole) {
        this.adminRole = adminRole;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
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

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public JSONArray getHistoryPasswordList() {
        return historyPasswordList;
    }

    public void setHistoryPasswordList(JSONArray historyPasswordList) {
        this.historyPasswordList = historyPasswordList;
    }

    public Date getLastActionTime() {
        return lastActionTime;
    }

    public void setLastActionTime(Date lastActionTime) {
        this.lastActionTime = lastActionTime;
    }

    public boolean isNeedUpdatePassword() {
        return needUpdatePassword;
    }

    public void setNeedUpdatePassword(boolean needUpdatePassword) {
        this.needUpdatePassword = needUpdatePassword;
    }
}
