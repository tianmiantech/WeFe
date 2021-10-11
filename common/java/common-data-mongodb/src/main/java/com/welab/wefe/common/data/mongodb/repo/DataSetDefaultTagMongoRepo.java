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

import com.mongodb.client.result.UpdateResult;
import com.welab.wefe.common.data.mongodb.entity.contract.data.DataSetDefaultTag;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Repository
public class DataSetDefaultTagMongoRepo extends AbstractMongoRepo {
    public List<DataSetDefaultTag> findAll() {
        return mongoTemplate.findAll(DataSetDefaultTag.class);
    }

    public boolean deleteByTagId(String tagId) {
        if (StringUtils.isEmpty(tagId)) {
            return false;
        }
        Query query = new QueryBuilder().append("tagId", tagId).build();
        Update udpate = new UpdateBuilder().append("status", 1).build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, udpate, DataSetDefaultTag.class);
        return updateResult.wasAcknowledged();
    }

    public boolean update(String tagId, String tagName) {
        if (StringUtils.isEmpty(tagId)) {
            return false;
        }
        Query query = new QueryBuilder().append("tagId", tagId).build();
        Update udpate = new UpdateBuilder().append("tagName", tagName).build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, udpate, DataSetDefaultTag.class);
        return updateResult.wasAcknowledged();
    }
}
