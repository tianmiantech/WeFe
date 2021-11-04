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
import com.welab.wefe.common.data.mongodb.entity.union.MemberAuthType;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberAuthTypeExtJSON;
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
public class MemberAuthTypeMongoRepo extends AbstractMongoRepo {
    public List<MemberAuthType> findList() {
        return mongoTemplate.find(new QueryBuilder().notRemoved().build(),MemberAuthType.class);
    }

    public boolean deleteByTypeId(String typeId) {
        if (StringUtils.isEmpty(typeId)) {
            return false;
        }
        Query query = new QueryBuilder().append("typeId", typeId).build();
        Update udpate = new UpdateBuilder().append("status", 1).build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, udpate, MemberAuthType.class);
        return updateResult.wasAcknowledged();
    }

    public boolean update(String typeId, String typeName, String updatedTime) {
        if (StringUtils.isEmpty(typeId)) {
            return false;
        }
        Query query = new QueryBuilder().append("typeId", typeId).build();
        Update udpate = new UpdateBuilder()
                .append("typeName", typeName)
                .append("updatedTime", updatedTime)
                .build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, udpate, MemberAuthType.class);
        return updateResult.wasAcknowledged();
    }

    public boolean updateExtJSONById(String typeId, MemberAuthTypeExtJSON extJSON) {
        if (StringUtils.isEmpty(typeId)) {
            return false;
        }
        Query query = new QueryBuilder().append("typeId", typeId).build();
        Update update = new UpdateBuilder().append("extJson", extJSON).build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, MemberAuthType.class);
        return updateResult.wasAcknowledged();
    }
}
