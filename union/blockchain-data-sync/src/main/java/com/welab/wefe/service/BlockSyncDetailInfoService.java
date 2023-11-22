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
import com.welab.wefe.common.data.mongodb.entity.union.BlockSyncDetailInfo;
import com.welab.wefe.common.data.mongodb.repo.BlockSyncDetailInfoMongoRepo;
import com.welab.wefe.common.util.JObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author aaron.li
 * @date 2021/12/31 11:11
 **/
@Service
public class BlockSyncDetailInfoService extends BaseService {
    private final Logger LOG = LoggerFactory.getLogger(BlockSyncDetailInfoService.class);

    @Autowired
    private BlockSyncDetailInfoMongoRepo blockSyncDetailInfoMongoRepo;

    /**
     * Save block information
     */
    public void saveBlockDetailInfo(BlockInfoBO blockInfoBO) {
        try {
            BlockSyncDetailInfo blockSyncDetailInfo = new BlockSyncDetailInfo();
            blockSyncDetailInfo.setGroupId(blockInfoBO.getGroupId());
            blockSyncDetailInfo.setBlockNumber(blockInfoBO.getBlockNumber().longValue());
            blockSyncDetailInfo.setData(JObject.create(blockInfoBO));
            blockSyncDetailInfoMongoRepo.upsert(blockSyncDetailInfo);
        } catch (Exception e) {
            LOG.error("Failed to save block detail info with group id:" + blockInfoBO.getGroupId() + ", block: " + blockInfoBO.getBlockNumber() + " , exception info: ", e);
            sendErrorMsg(blockInfoBO.getGroupId(), blockInfoBO.getBlockNumber().longValue(), e);
        }
    }
}
