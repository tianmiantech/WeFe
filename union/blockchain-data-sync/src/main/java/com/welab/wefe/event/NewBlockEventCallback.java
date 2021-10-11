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

package com.welab.wefe.event;

import com.welab.wefe.constant.BlockConstant;
import org.fisco.bcos.sdk.service.callback.BlockNumberNotifyCallback;
import org.fisco.bcos.sdk.service.model.BlockNumberNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Register new block event
 *
 * @author yuxin.zhang
 **/
public class NewBlockEventCallback implements BlockNumberNotifyCallback {
    private static final Logger LOG = LoggerFactory.getLogger(NewBlockEventCallback.class);

    @Override
    public void onReceiveBlockNumberInfo(String peerIpAndPort, BlockNumberNotification blockNumberNotification) {
        int groupId;
        groupId = Integer.parseInt(blockNumberNotification.getGroupId());
        BigInteger blockNumber = new BigInteger(blockNumberNotification.getBlockNumber());
        LOG.info("NewBlockEventCallBack groupId:{}, blockNumber:{}",
                groupId, blockNumber);
        //Update the latest block height information of the group
        BlockConstant.setGroupCurrentBlockNumberMap(groupId, blockNumber.longValue());
    }
}
