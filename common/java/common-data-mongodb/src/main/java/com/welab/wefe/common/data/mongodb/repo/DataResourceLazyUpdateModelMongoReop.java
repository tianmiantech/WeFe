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

import com.welab.wefe.common.data.mongodb.entity.union.DataResourceLazyUpdateModel;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author yuxin.zhang
 */
@Repository
public class DataResourceLazyUpdateModelMongoReop extends AbstractMongoRepo {

    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }


    public DataResourceLazyUpdateModel findByDataResourceId(String dataResourceId) {
        if (StringUtils.isEmpty(dataResourceId)) {
            return null;
        }
        Query query = new QueryBuilder().append("dataResourceId", dataResourceId).build();
        return mongoUnionTemplate.findOne(query, DataResourceLazyUpdateModel.class);
    }

    public List<DataResourceLazyUpdateModel> findAll() {
        Query query = new QueryBuilder().sort("updateTime").build();
        return mongoUnionTemplate.find(query, DataResourceLazyUpdateModel.class);
    }

}
