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

import com.welab.wefe.common.data.mongodb.entity.common.FlowLimit;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author aaron.li
 * @date 2021/10/22 17:41
 **/
@Repository
public class FlowLimitRepo extends AbstractMongoRepo {
    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    public FlowLimit findByKey(String key) {
        Query query = new QueryBuilder().append("key", key).build();
        return mongoUnionTemplate.findOne(query, FlowLimit.class);
    }

    public void save(FlowLimit flowLimit) {
        mongoUnionTemplate.save(flowLimit);
    }

    public void removeByKey(String key) {
        Query query = new QueryBuilder().append("key", key).build();
        mongoUnionTemplate.remove(query, FlowLimit.class);
    }

    public List<FlowLimit> findAll() {
        return mongoUnionTemplate.findAll(FlowLimit.class);
    }

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }
}
