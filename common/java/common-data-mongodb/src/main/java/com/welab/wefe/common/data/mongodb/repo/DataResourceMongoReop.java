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

import com.mongodb.client.result.UpdateResult;
import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataResourceExtJSON;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetExtJSON;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author yuxin.zhang
 */
@Repository
public class DataResourceMongoReop extends AbstractDataSetMongoRepo {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }

    @Override
    protected String getTableName() {
        return MongodbTable.Union.DATA_RESOURCE;
    }


    public boolean deleteByDataResourceId(String dataResourceId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).build();
        Update udpate = new UpdateBuilder().append("status", 1).build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, udpate, DataResource.class);
        return updateResult.wasAcknowledged();
    }


    public boolean existsByDataResourceId(String dataResourceId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).notRemoved().build();
        return mongoUnionTemplate.exists(query, DataResource.class);
    }

    public DataResource findByDataResourceId(String dataResourceId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return null;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).notRemoved().build();
        return mongoUnionTemplate.findOne(query, DataResource.class);
    }

    public List<String> findByDataResourceType(String dataResourceType) {
        if (StringUtils.isEmpty(dataResourceType)) {
            return null;
        }
        Query query = new QueryBuilder().append("dataResourceType", dataResourceType).notRemoved().build();
        query.fields().exclude("_id").include("tags");
        return mongoUnionTemplate.find(query, String.class);
    }


    public void upsert(DataResource dataResource) {
        DataResource obj = findByDataResourceId(dataResource.getDataResourceId());
        if (obj != null) {
            dataResource.setDataResourceId(obj.getDataResourceId());
        }
        mongoUnionTemplate.save(dataResource);
    }

    public boolean updateExtJSONById(String dataResourceId, DataResourceExtJSON extJSON) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).build();
        Update update = new UpdateBuilder().append("extJson", extJSON).build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, update, DataResource.class);
        return updateResult.wasAcknowledged();
    }

    public boolean updateEnable(String dataResourceId, String enable, String updatedTime) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).build();
        Update udpate = new UpdateBuilder()
                .append("enable", enable)
                .append("updatedTime", updatedTime)
                .build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, udpate, DataResource.class);
        return updateResult.wasAcknowledged();
    }

}
