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

package com.welab.wefe.listener;

import com.welab.wefe.bo.contract.ContractInfo;
import com.welab.wefe.constant.BlockConstant;
import com.welab.wefe.event.NewBlockEventCallback;
import com.welab.wefe.task.DataSyncTask;
import com.welab.wefe.util.ContractParserUtil;
import com.welab.wefe.util.PropertiesUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.service.GroupManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author aaron.li
 **/
@Component
public class InitListener implements ApplicationListener<ApplicationStartedEvent> {
    private final Logger LOG = LoggerFactory.getLogger(InitListener.class);

    @Autowired
    private DataSyncTask dataSyncTask;

    @Autowired
    private BcosSDK bcosSDK;

    /**
     * The root directory of the abi and bin of the contract
     */
    @Value("${contract.solidity-path}")
    private String contractSolidityPath;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // Register block event
        registerNewBlockEventInfo();

        //Initialize contract information
        initContractInfo();

        // Start task
        dataSyncTask.startTask();
    }

    private void initContractInfo() {
        //Load contract information
        List<ContractInfo> contractInfoList = PropertiesUtil.getContractInfos(contractSolidityPath);
        if (CollectionUtils.isEmpty(contractInfoList)) {
            LOG.error("\n" +
                    "Please upload the abi and bin information of the contract to the abi and bin subdirectories of " + contractSolidityPath + " respectively...");
            System.exit(0);
        }

        // Set contract context
        ContractParserUtil.parse(contractInfoList);
    }

    /**
     * Register new block event
     */
    private void registerNewBlockEventInfo() {
        NewBlockEventCallback callback = new NewBlockEventCallback();
        GroupManagerService groupManagerService = bcosSDK.getGroupManagerService();
        String registerId = null;
        try {
            //Get the latest block of each group
            Set<Integer> groupList = groupManagerService.getGroupList();
            for (Integer groupId :
                    groupList) {
                long blockNumber = groupManagerService.getLatestBlockNumberByGroup(groupId).longValue();
                BlockConstant.setGroupCurrentBlockNumberMap(groupId, blockNumber);
            }
            registerId = groupManagerService.registerBlockNotifyCallback(callback);
        } catch (Exception e) {
            LOG.error("register newBlockEvent error:[]", e);
            groupManagerService.eraseBlockNotifyCallback(registerId);
            System.exit(1);
        }
    }

}
