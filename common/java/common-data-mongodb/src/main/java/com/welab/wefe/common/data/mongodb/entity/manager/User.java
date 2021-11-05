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

package com.welab.wefe.common.data.mongodb.entity.manager;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractMongoModel;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.USER)
public class User extends AbstractNormalMongoModel {
    private String userId = UUID.randomUUID().toString().replaceAll("-", "");
    private String account;
    private String password;
    private String nickname;
    private String email;
    /**
     * 是否是超级管理员;超级管理员通常是第一个创建并初始化系统的那个人
     */
    private boolean superAdminRole;
    /**
     * 是否是管理员;管理员有更多权限，比如设置 member 是否对外可见。
     */
    private boolean adminRole;

    /**
     * 是否可用
     */
    private boolean enable = true;


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
}
