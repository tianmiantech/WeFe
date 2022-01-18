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

import com.welab.wefe.common.data.mongodb.entity.contract.tool.BlockSyncHeight;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author aaron.li
 **/
@Repository
public class BlockSyncHeightMongoReop extends AbstractMongoRepo {

    public BlockSyncHeight findByGroupId(Integer groupId) {
        Query query = new QueryBuilder().append("groupId", groupId).build();
        return mongoTemplate.findOne(query, BlockSyncHeight.class);
    }

    public void upsert(BlockSyncHeight blockSyncHeight) {
        BlockSyncHeight dbRecord = findByGroupId(blockSyncHeight.getGroupId());
        if (dbRecord != null) {
            if (dbRecord.getBlockNumber() >= blockSyncHeight.getBlockNumber()) {
                return;
            }
            blockSyncHeight.setId(dbRecord.getId());
            blockSyncHeight.setCreateTime(dbRecord.getCreateTime());
        }
        blockSyncHeight.setUpdateTime(System.currentTimeMillis());
        mongoTemplate.save(blockSyncHeight);
    }

}
