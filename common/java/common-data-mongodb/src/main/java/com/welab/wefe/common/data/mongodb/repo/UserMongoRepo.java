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

package com.welab.wefe.common.data.mongodb.repo;

import com.welab.wefe.common.data.mongodb.entity.union.DataSetMemberPermission;
import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Repository
public class UserMongoRepo extends AbstractMongoRepo {

//    public List<User> findByMemberId(String memberId) {
//        Query query = new QueryBuilder().append("memberId", memberId).build();
//        List<DataSetMemberPermission> list = mongoTemplate.find(query, DataSetMemberPermission.class);
//        return list;
//    }

    public User find(String account, String password) {
        Query query = new QueryBuilder()
                .append("account", account)
                .append("password", password)
                .build();
        return mongoTemplate.findOne(query, User.class);
    }


    public boolean checkUserByAccountAndPassword(String account, String password) {
        Query query = new QueryBuilder()
                .append("account", account)
                .append("password", password)
                .build();
        return mongoTemplate.exists(query, User.class);
    }

}
