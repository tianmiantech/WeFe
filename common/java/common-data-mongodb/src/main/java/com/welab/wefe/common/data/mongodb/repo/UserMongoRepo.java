/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.data.mongodb.repo;

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Repository
public class UserMongoRepo extends AbstractMongoRepo {

    @Autowired
    protected MongoTemplate mongoManagerTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoManagerTemplate;
    }


    public User find(String account, String password) {
        Query query = new QueryBuilder()
                .append("account", account)
                .append("password", password)
                .build();
        return mongoManagerTemplate.findOne(query, User.class);
    }

    public User findByAccount(String account) {
        Query query = new QueryBuilder()
                .append("account", account)
                .build();
        return mongoManagerTemplate.findOne(query, User.class);
    }

    public User findByUserId(String userId) {
        Query query = new QueryBuilder()
                .append("userId", userId)
                .build();
        return mongoManagerTemplate.findOne(query, User.class);
    }


    public boolean checkUserByAccountAndPassword(String account, String password) {
        Query query = new QueryBuilder()
                .append("account", account)
                .append("password", password)
                .build();
        return mongoManagerTemplate.exists(query, User.class);
    }

    public void changeUserRole(String userId, boolean adminRole) {
        Query query = new QueryBuilder().append("userId", userId).build();
        Update update = new UpdateBuilder().append("adminRole", adminRole).build();
        mongoManagerTemplate.updateFirst(query, update, User.class);
    }

    public void changePassword(String userId, String password,String salt) {
        Query query = new QueryBuilder().append("userId", userId).build();
        Update update = new UpdateBuilder()
                .append("password", password)
                .append("salt",salt)
                .build();
        mongoManagerTemplate.updateFirst(query, update, User.class);
    }

    public void update(String userId, String nickname, String email) {
        Query query = new QueryBuilder().append("userId", userId).build();
        Update update = new UpdateBuilder()
                .append("nickname", nickname)
                .append("email", email)
                .build();
        mongoManagerTemplate.updateFirst(query, update, User.class);
    }

    public PageOutput<User> findList(String account, String nickname, Boolean adminRole, int pageIndex, int pageSize) {
        Query query = new QueryBuilder()
                .append("account", account)
                .like("nickname", nickname)
                .append("adminRole", adminRole)
                .page(pageIndex, pageSize)
                .build();
        List<User> list = mongoManagerTemplate.find(query, User.class);
        long count = mongoManagerTemplate.count(query, User.class);
        return new PageOutput<User>(pageIndex, count, pageSize, list);
    }
}
