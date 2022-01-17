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

package com.welab.wefe.service;

import com.welab.wefe.bo.data.BlockInfoBO;
import com.welab.wefe.common.data.mongodb.entity.contract.tool.BlockSyncHeight;
import com.welab.wefe.common.data.mongodb.repo.BlockSyncHeightMongoReop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author aaron.li
 * @date 2021/12/30 18:01
 **/
@Service
public class BlockSyncHeightService extends BaseService {
    @Autowired
    private BlockSyncHeightMongoReop blockSyncHeightMongoReop;

    /**
     * Record the block height information that has been successfully synchronized
     */
    public void save(BlockInfoBO blockInfoBO) {
        BlockSyncHeight blockSyncHeight = new BlockSyncHeight();
        blockSyncHeight.setBlockNumber(blockInfoBO.getBlockNumber().longValue());
        blockSyncHeight.setGroupId(blockInfoBO.getGroupId());
        blockSyncHeightMongoReop.upsert(blockSyncHeight);
    }

    public BlockSyncHeight findByGroupId(Integer groupId) {
        return blockSyncHeightMongoReop.findByGroupId(groupId);
    }
}
