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

package com.welab.wefe.service;

import com.welab.wefe.bo.data.BlockInfoBO;
import com.welab.wefe.bo.data.EventBO;
import com.welab.wefe.common.data.mongodb.entity.union.BlockSyncContractHeight;
import com.welab.wefe.common.data.mongodb.repo.BlockSyncContractHeightMongoRepo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author aaron.li
 * @date 2021/12/31 11:09
 **/
@Service
public class BlockSyncContractHeightService extends BaseService {

    @Autowired
    private BlockSyncContractHeightMongoRepo blockSyncContractHeightMongoRepo;

    /**
     * Record the block height contract information that has been successfully synchronized
     */
    public void save(BlockInfoBO blockInfoBO) {
        List<EventBO> eventBOList = blockInfoBO.getEventBOList();
        if (CollectionUtils.isEmpty(eventBOList)) {
            return;
        }
        Set<String> contractNameList = new HashSet<>(16);
        eventBOList.forEach(x -> contractNameList.add(x.getContractName()));
        BlockSyncContractHeight blockSyncContractHeight;
        for (String contractName : contractNameList) {
            blockSyncContractHeight = new BlockSyncContractHeight();
            blockSyncContractHeight.setGroupId(blockInfoBO.getGroupId());
            blockSyncContractHeight.setBlockNumber(blockInfoBO.getBlockNumber().longValue());
            blockSyncContractHeight.setContractName(contractName);
            blockSyncContractHeightMongoRepo.upsertByGroupIdAndContractName(blockSyncContractHeight);
        }
    }

    public BlockSyncContractHeight findByGroupIdAndContractName(Integer groupId, String contractName) {
        return blockSyncContractHeightMongoRepo.findByGroupIdAndContractName(groupId, contractName);
    }

    public void upsertByGroupIdAndContractName(BlockSyncContractHeight blockSyncContractHeight) {
        blockSyncContractHeightMongoRepo.upsertByGroupIdAndContractName(blockSyncContractHeight);
    }
}
