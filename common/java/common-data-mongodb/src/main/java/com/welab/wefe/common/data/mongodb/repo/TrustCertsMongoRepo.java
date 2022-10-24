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
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceDefaultTag;
import com.welab.wefe.common.data.mongodb.entity.union.TrustCerts;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.entity.union.ext.UnionNodeExtJSON;
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
 **/
@Repository
public class TrustCertsMongoRepo extends AbstractMongoRepo {
    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }

    public List<TrustCerts> findAll(boolean status) {
        return mongoUnionTemplate.find(
                new QueryBuilder()
                        .append("status", status ? 1 : 0)
                        .build()
                ,
                TrustCerts.class);
    }

    public boolean existsBySerialNumber(String serialNumber) {
        Query query = new QueryBuilder().append("serialNumber", serialNumber).notRemoved().build();
        return mongoUnionTemplate.exists(query, TrustCerts.class);
    }

    public boolean deleteBySerialNumber(String serialNumber) {
        if (StringUtils.isEmpty(serialNumber)) {
            return false;
        }
        Query query = new QueryBuilder().append("serialNumber", serialNumber).notRemoved().build();
        Update update = new UpdateBuilder().append("status", 1).build();
        UpdateResult updateResult = mongoUnionTemplate.updateFirst(query, update, TrustCerts.class);
        return updateResult.wasAcknowledged();
    }

}
