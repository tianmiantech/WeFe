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

package com.welab.wefe.manager.service.dto.user;

import com.welab.wefe.common.data.mongodb.entity.manager.Account;
import com.welab.wefe.common.web.dto.AbstractApiOutput;

/**
 * @author yuxin.zhang
 */
public class LoginOutput extends AbstractApiOutput {
    private String accountId;

    private String token;

    private String phoneNumber;

    private String nickname;

    private String email;

    private Boolean superAdminRole;

    private Boolean adminRole;
    private boolean needUpdatePassword;

    public LoginOutput() {
    }

    public LoginOutput(String token, Account model) {
        this.accountId = model.getAccountId();
        this.token = token;
        this.phoneNumber = model.getPhoneNumber();
        this.nickname = model.getNickname();
        this.email = model.getEmail();
        this.superAdminRole = model.getSuperAdminRole();
        this.adminRole = model.getAdminRole();
        this.needUpdatePassword = model.isNeedUpdatePassword();
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public boolean isNeedUpdatePassword() {
        return needUpdatePassword;
    }

    public void setNeedUpdatePassword(boolean needUpdatePassword) {
        this.needUpdatePassword = needUpdatePassword;
    }
}
