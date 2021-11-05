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

import com.welab.wefe.common.data.mongodb.entity.union.BlockSyncDetailInfo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author aaron.li
 **/
@Repository
public class BlockSyncDetailInfoMongoRepo extends AbstractMongoRepo {
    @Autowired
    protected MongoTemplate mongoUnionTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoUnionTemplate;
    }

    public BlockSyncDetailInfo findByGroupIdAndBlockNumber(int groupId, long blockNumber) {
        Query query = new QueryBuilder().append("groupId", groupId).append("blockNumber", blockNumber).build();
        return mongoUnionTemplate.findOne(query, BlockSyncDetailInfo.class);
    }

    public void upsert(BlockSyncDetailInfo blockSyncDetailInfo) {
        BlockSyncDetailInfo dbRecord = findByGroupIdAndBlockNumber(blockSyncDetailInfo.getGroupId(), blockSyncDetailInfo.getBlockNumber());
        if (dbRecord != null) {
            blockSyncDetailInfo.setId(dbRecord.getId());
            blockSyncDetailInfo.setCreateTime(dbRecord.getCreateTime());
        }
        blockSyncDetailInfo.setUpdateTime(System.currentTimeMillis());
        mongoUnionTemplate.save(blockSyncDetailInfo);
    }


}
