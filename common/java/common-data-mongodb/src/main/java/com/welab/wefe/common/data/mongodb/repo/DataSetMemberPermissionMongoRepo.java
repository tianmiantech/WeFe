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
import com.welab.wefe.common.data.mongodb.entity.union.DataSetMemberPermission;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetMemberPermissionExtJSON;
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
public class DataSetMemberPermissionMongoRepo extends AbstractMongoRepo {
    public boolean deleteByDataSetId(String dataSetId) {
        Query query = new QueryBuilder().append("dataSetId", dataSetId).build();
        Update udpate = new UpdateBuilder().append("status", "1").build();
        UpdateResult updateResult = mongoTemplate.updateMulti(query, udpate, DataSetMemberPermission.class);
        return updateResult.wasAcknowledged();
    }

    public List<DataSetMemberPermission> findByMemberId(String memberId) {
        if (StringUtils.isEmpty(memberId)) {
            return null;
        }
        Query query = new QueryBuilder().append("memberId", memberId).build();
        List<DataSetMemberPermission> list = mongoTemplate.find(query, DataSetMemberPermission.class);
        return list;
    }


    public void upsert(DataSetMemberPermission dataSetMemberPermission) {
        Query query = new QueryBuilder().append("dataSetMemberPermissionId", dataSetMemberPermission.getDataSetMemberPermissionId()).build();
        DataSetMemberPermission dbDataSetMemberPermission = mongoTemplate.findOne(query, DataSetMemberPermission.class);
        if (dbDataSetMemberPermission != null) {
            dataSetMemberPermission.setId(dbDataSetMemberPermission.getId());
        }
        mongoTemplate.save(dataSetMemberPermission);
    }

    public boolean updateExtJSONById(String dataSetId, DataSetMemberPermissionExtJSON extJSON) {
        if (StringUtils.isEmpty(dataSetId)) {
            return false;
        }
        Query query = new QueryBuilder().append("dataSetId", dataSetId).build();
        Update update = new UpdateBuilder().append("extJson", extJSON).build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, DataSetMemberPermission.class);
        return updateResult.wasAcknowledged();
    }

}
