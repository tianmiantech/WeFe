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

import com.mongodb.client.result.UpdateResult;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetDefaultTagExtJSON;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * @author yuxin.zhang
 **/
public abstract class AbstractDataSetDefaultTagMongoRepo extends AbstractMongoRepo {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }


    protected abstract String getTableName();

    public <T> List<T> findAll(Class<T> t) {
        return mongoUnionTemplate.find(new QueryBuilder().notRemoved().build(), t);
    }

    public boolean deleteByTagId(String tagId) {
        if (StringUtils.isEmpty(tagId)) {
            return false;
        }
        Query query = new QueryBuilder().append("tagId", tagId).build();
        Update udpate = new UpdateBuilder().append("status", 1).build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, udpate, getTableName());
        return updateResult.wasAcknowledged();
    }

    public boolean update(String tagId, String tagName, DataSetDefaultTagExtJSON extJson, String updatedTime) {
        if (StringUtils.isEmpty(tagId)) {
            return false;
        }
        Query query = new QueryBuilder().append("tagId", tagId).build();
        Update udpate = new UpdateBuilder()
                .append("tagName", tagName)
                .append("extJson", extJson)
                .append("updatedTime", updatedTime)
                .build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, udpate, getTableName());
        return updateResult.wasAcknowledged();
    }

    public boolean updateExtJSONById(String tagId, DataSetDefaultTagExtJSON extJSON) {
        if (StringUtils.isEmpty(tagId)) {
            return false;
        }
        Query query = new QueryBuilder().append("tagId", tagId).build();
        Update update = new UpdateBuilder().append("extJson", extJSON).build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, update, getTableName());
        return updateResult.wasAcknowledged();
    }
}
