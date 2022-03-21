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

package com.welab.wefe.common.data.mongodb.repo;

import com.alibaba.fastjson.JSONArray;
import com.mongodb.client.result.UpdateResult;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.Account;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Repository
public class AccountMongoRepo extends AbstractMongoRepo {

    @Autowired
    protected MongoTemplate mongoManagerTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoManagerTemplate;
    }


    public Account find(String account, String password) {
        Query query = new QueryBuilder()
                .append("account", account)
                .append("password", password)
                .build();
        return mongoManagerTemplate.findOne(query, Account.class);
    }


    public boolean checkAccountIsExist(String phoneNumber) {
        Query query = new QueryBuilder()
                .append("phoneNumber", phoneNumber)
                .build();
        return mongoManagerTemplate.exists(query, Account.class);
    }

    public long count() {
        return mongoManagerTemplate.count(new Query(),Account.class);
    }

    public Account findByPhoneNumber(String phoneNumber) {
        Query query = new QueryBuilder()
                .append("phoneNumber", phoneNumber)
                .build();
        return mongoManagerTemplate.findOne(query, Account.class);
    }

    public Account getSuperAdmin() {
        Query query = new QueryBuilder()
                .append("superAdminRole", true)
                .build();
        return mongoManagerTemplate.findOne(query, Account.class);
    }

    public Account findByAccountId(String accountId) {
        Query query = new QueryBuilder()
                .append("accountId", accountId)
                .build();
        return mongoManagerTemplate.findOne(query, Account.class);
    }


    public void changeAdminRole(String accountId, boolean adminRole) {
        Query query = new QueryBuilder().append("accountId", accountId).build();
        Update update = new UpdateBuilder().append("adminRole", adminRole).build();
        mongoManagerTemplate.updateFirst(query, update, Account.class);
    }

    public void enableAccount(String accountId, boolean enable, String updateBy, String auditComment) {
        Query query = new QueryBuilder().append("accountId", accountId).build();
        Update update = new UpdateBuilder()
                .append("enable", enable)
                .append("updatedBy",updateBy)
                .append("updateTime",System.currentTimeMillis())
                .append("auditComment",auditComment)
                .build();
        mongoManagerTemplate.updateFirst(query, update, Account.class);
    }

    public void changeAccountToSuperAdminRole(String accountId, String updateBy) {
        Query query = new QueryBuilder().append("accountId", accountId).build();
        Update update = new UpdateBuilder()
                .append("superAdminRole", true)
                .append("adminRole", true)
                .append("updatedBy",updateBy)
                .append("updateTime",System.currentTimeMillis())
                .build();
        mongoManagerTemplate.updateFirst(query, update, Account.class);
    }

    public void cancelSuperAdmin(String accountId) {
        Query query = new QueryBuilder().append("accountId", accountId).build();
        Update update = new UpdateBuilder()
                .append("superAdminRole", false)
                .append("adminRole", false)
                .append("updatedBy",accountId)
                .append("updateTime",System.currentTimeMillis())
                .build();
        mongoManagerTemplate.updateFirst(query, update, Account.class);
    }


    public void auditAccount(String accountId, AuditStatus auditStatus, String auditComment) {
        Query query = new QueryBuilder().append("accountId", accountId).build();
        Update update = new UpdateBuilder()
                .append("auditStatus", auditStatus.name())
                .append("auditComment", auditComment)
                .build();
        mongoManagerTemplate.updateFirst(query, update, Account.class);
    }

    public void updatePassword(String accountId, String password, String salt, JSONArray historyPasswords) {
        Query query = new QueryBuilder().append("accountId", accountId).build();
        Update update = new UpdateBuilder()
                .append("password", password)
                .append("salt", salt)
                .append("historyPasswordList", historyPasswords)
                .append("needUpdatePassword",false)
                .build();
        mongoManagerTemplate.updateFirst(query, update, Account.class);
    }

    public void update(String accountId, String nickname, String email) {
        Query query = new QueryBuilder().append("accountId", accountId).build();
        Update update = new UpdateBuilder()
                .append("nickname", nickname)
                .append("email", email)
                .append("updateTime",System.currentTimeMillis())
                .build();
        mongoManagerTemplate.updateFirst(query, update, Account.class);
    }

    public PageOutput<Account> findList(String phoneNumber, String nickname, Boolean adminRole, int pageIndex, int pageSize) {
        Query query = new QueryBuilder()
                .append("phoneNumber", phoneNumber)
                .like("nickname", nickname)
                .append("adminRole", adminRole)
                .page(pageIndex, pageSize)
                .build();
        List<Account> list = mongoManagerTemplate.find(query, Account.class);
        long count = mongoManagerTemplate.count(query, Account.class);
        return new PageOutput<>(pageIndex, count, pageSize, list);
    }

    public List<Account> findAll() {
        return mongoManagerTemplate.findAll(Account.class);
    }

    public void updateLastActionTime(String userId) {
        Query query = new QueryBuilder().append("userId", userId).build();
        Update update = new UpdateBuilder()
                .append("lastActionTime", new Date())
                .build();
        mongoManagerTemplate.updateFirst(query, update, Account.class);
    }


    public long disableAccountWithoutAction90Days() {
        Query query = new QueryBuilder().gte("lastActionTime", DateUtil.minusDays(new Date(),90)).build();
        Update update = new UpdateBuilder()
                .append("enable", false)
                .build();
        UpdateResult updateResult = mongoManagerTemplate.updateMulti(query, update, Account.class);
        return updateResult.getModifiedCount();
    }

    public long cancelAccountWithoutAction180Days() {
        Query query = new QueryBuilder().gte("lastActionTime", DateUtil.minusDays(new Date(),180)).build();
        Update update = new UpdateBuilder()
                .append("cancelled", true)
                .build();
        UpdateResult updateResult = mongoManagerTemplate.updateMulti(query, update, Account.class);
        return updateResult.getModifiedCount();
    }
}
