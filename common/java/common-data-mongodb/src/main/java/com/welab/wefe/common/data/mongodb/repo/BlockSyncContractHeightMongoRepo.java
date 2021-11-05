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

import com.welab.wefe.common.data.mongodb.entity.union.BlockSyncContractHeight;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author aaron.li
 **/
@Repository
public class BlockSyncContractHeightMongoRepo extends AbstractMongoRepo {
    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    public BlockSyncContractHeight findByGroupIdAndContractName(Integer groupId, String contractName) {
        Query query = new QueryBuilder().append("groupId", groupId).append("contractName", contractName).build();
        return mongoUnionTemplate.findOne(query, BlockSyncContractHeight.class);
    }

    public void upsertByGroupIdAndContractName(BlockSyncContractHeight blockSyncContractHeight) {
        BlockSyncContractHeight dbRecord = findByGroupIdAndContractName(blockSyncContractHeight.getGroupId(), blockSyncContractHeight.getContractName());
        if (dbRecord != null) {
            blockSyncContractHeight.setId(dbRecord.getId());
            blockSyncContractHeight.setCreateTime(dbRecord.getCreateTime());
        }
        blockSyncContractHeight.setUpdateTime(System.currentTimeMillis());
        mongoUnionTemplate.save(blockSyncContractHeight);
    }

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }
}
