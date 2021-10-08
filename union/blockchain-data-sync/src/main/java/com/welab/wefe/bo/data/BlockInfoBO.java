/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.bo.data;

import java.math.BigInteger;
import java.util.List;

/**
 * @author aaron.li
 **/
public class BlockInfoBO {
    private Integer groupId;
    private BigInteger blockNumber;

    private List<EventBO> eventBOList;

    @Override
    public String toString() {
        return "{" +
                "groupId=" + groupId +
                ", blockNumber=" + blockNumber +
                ", eventBOList=" + eventBOList +
                '}';
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public List<EventBO> getEventBOList() {
        return eventBOList;
    }

    public void setEventBOList(List<EventBO> eventBOList) {
        this.eventBOList = eventBOList;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }
}
