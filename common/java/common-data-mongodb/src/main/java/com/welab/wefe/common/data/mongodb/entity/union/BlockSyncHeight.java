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

package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Exported block height
 *
 * @author aaron.li
 **/
@Document(collection = MongodbTable.Union.BLOCK_SYNC_HEIGHT)
public class BlockSyncHeight extends AbstractNormalMongoModel {
    private Integer groupId;
    private Long blockNumber;


    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

}
