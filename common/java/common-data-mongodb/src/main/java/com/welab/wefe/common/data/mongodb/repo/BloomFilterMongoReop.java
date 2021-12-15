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
import com.welab.wefe.common.data.mongodb.entity.union.BloomFilter;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.ext.BloomFilterExtJSON;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetExtJSON;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;


/**
 * @author yuxin.zhang
 */
@Repository
public class BloomFilterMongoReop extends AbstractDataSetMongoRepo {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }

    @Override
    protected String getTableName() {
        return MongodbTable.Union.BLOOM_FILTER;
    }


    public boolean existsByDataResourceId(String dataResourceId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).notRemoved().build();
        return mongoUnionTemplate.exists(query, BloomFilter.class);
    }

    public BloomFilter findByDataResourceId(String dataResourceId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return null;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).notRemoved().build();
        return mongoUnionTemplate.findOne(query, BloomFilter.class);
    }


    public void upsert(BloomFilter bloomFilter) {
        mongoUnionTemplate.save(bloomFilter);
    }

}
