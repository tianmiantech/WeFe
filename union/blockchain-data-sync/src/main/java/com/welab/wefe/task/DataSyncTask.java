/*
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

package com.welab.wefe.task;

import com.welab.wefe.bo.data.BlockInfoBO;
import com.welab.wefe.bo.data.EventBO;
import com.welab.wefe.common.data.mongodb.entity.union.BlockSyncContractHeight;
import com.welab.wefe.common.data.mongodb.entity.union.BlockSyncDetailInfo;
import com.welab.wefe.common.data.mongodb.entity.union.BlockSyncHeight;
import com.welab.wefe.common.data.mongodb.repo.BlockSyncContractHeightMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.BlockSyncDetailInfoMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.BlockSyncHeightMongoReop;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.constant.BlockConstant;
import com.welab.wefe.constant.SyncConstant;
import com.welab.wefe.exception.BusinessException;
import com.welab.wefe.parser.BlockInfoParser;
import com.welab.wefe.tool.DataProcessor;
import com.welab.wefe.tool.DataSyncContext;
import com.welab.wefe.util.BlockUtil;
import com.welab.wefe.util.WechatUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;

/**
 * Blockchain data sync
 *
 * @author aaron.li
 **/
@Component
public class DataSyncTask {
    private final Logger LOG = LoggerFactory.getLogger(DataSyncTask.class);

    @Autowired
    private BcosSDK bcosSDK;

    @Autowired
    private BlockSyncHeightMongoReop blockSyncHeightMongoReop;

    @Autowired
    private BlockSyncDetailInfoMongoRepo blockSyncDetailInfoMongoRepo;

    @Autowired
    private BlockSyncContractHeightMongoRepo blockSyncContractHeightMongoRepo;

    /**
     * The group ID of the sync data, separated by commas
     */
    @Value("${contract.data-sync-group-id}")
    private String dataSyncGroupId;

    @Value("${wechat.bot-url}")
    private String wechatUrl;


    public void startTask() {
        if (StringUtil.isEmpty(dataSyncGroupId) || StringUtil.isEmpty(dataSyncGroupId.trim())) {
            LOG.warn("please config data sync group id.");
            return;
        }


        String[] groupIds = dataSyncGroupId.trim().split(",");
        for (String groupId : groupIds) {
            if (!NumberUtils.isNumber(groupId)) {
                continue;
            }

            cn.hutool.core.thread.ThreadUtil.execAsync(new CrawlRunner(NumberUtils.toInt(groupId)));
        }
    }

    public class CrawlRunner implements Runnable {
        private int groupId;
        private Client client;

        public CrawlRunner(int groupId) {
            this.groupId = groupId;
        }

        @Override
        public void run() {
            try {
                client = bcosSDK.getClient(groupId);
                // Create a data sync context and save it to the instance
                DataSyncContext dataSyncContext = DataSyncContext.create(client);
                SyncConstant.setCurrentContext(dataSyncContext);
                //Query part of the data that has been synchronized
                BlockSyncHeight blockSyncHeight = blockSyncHeightMongoReop.findByGroupId(dataSyncContext.getGroupId());
                long startBlockNumber = (null == blockSyncHeight ? 0 : blockSyncHeight.getBlockNumber() + 1);
                while (true) {
                    // The length of the synchronized part is greater than the current maximum length, it proves that the synchronization has been completed
                    Long currentBlockNumber = BlockConstant.getGroupBlockNumber(dataSyncContext.getGroupId());
                    if (null == currentBlockNumber || currentBlockNumber < 0 ||
                            startBlockNumber > currentBlockNumber) {
                        long sleepMillis = 500;
                        LOG.info("Start sync data with group id: {}, current block number is: {}, less than start block number: {}, current thread sleep {} ms.", groupId, currentBlockNumber, startBlockNumber, sleepMillis);
                        ThreadUtil.sleep(sleepMillis);
                        continue;
                    }

                    while (startBlockNumber <= currentBlockNumber) {
                        LOG.info("Start sync data with group id: {}, block : {}, current block number: {} ....", groupId, startBlockNumber, currentBlockNumber.longValue());
                        try {
                            startSync(startBlockNumber);
                            LOG.info("Sync of data with group id: {}, block: {} succeeded.", groupId, startBlockNumber);
                            startBlockNumber++;
                        } catch (BusinessException e) {
                            LOG.error("Failed to sync data with group id: " + groupId + ", block: " + startBlockNumber + " , exception info: ", e);
                            // Business error proofs need not be repeated, Manual intervention is required
                            sendErrorMsg(groupId, startBlockNumber, e);
                            return;
                        } catch (Exception e) {
                            LOG.error("Failed to sync data with group id:" + groupId + ", block: " + startBlockNumber + " , exception info: ", e);
                            sendErrorMsg(groupId, startBlockNumber, e);
                            ThreadUtil.sleepSeconds(5);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Failed to sync data with group id:" + groupId + " exception info: ", e);
                sendErrorMsg(groupId, -1, e);
            }
        }

        /**
         * Sync block data
         */
        private void startSync(long blockNumber) throws Exception {
            for (int i = 0; i < 6; i++) {
                // get block by block number
                BcosBlock.Block block = BlockUtil.getBlock(this.client, new BigInteger(String.valueOf(blockNumber)));
                BlockInfoBO blockInfoBO = BlockInfoParser.create(block).parse();

                if (CollectionUtils.isEmpty(blockInfoBO.getEventBOList())) {
                    // retry
                    Thread.sleep(500);
                    continue;
                }

                BlockInfoBO filterBlockInfoBO = filterBlockInfoBO(blockInfoBO);

                DataProcessor.parseBlockData(filterBlockInfoBO);

                saveBlockSyncHeight(blockInfoBO);

                saveBlockSyncContractHeight(filterBlockInfoBO);

                saveBlockDetailInfo(blockInfoBO);
            }
        }

        /**
         * Record the block height information that has been successfully synchronized
         */
        private void saveBlockSyncHeight(BlockInfoBO blockInfoBO) {
            BlockSyncHeight blockSyncHeight = new BlockSyncHeight();
            blockSyncHeight.setBlockNumber(blockInfoBO.getBlockNumber().longValue());
            blockSyncHeight.setGroupId(blockInfoBO.getGroupId());
            blockSyncHeightMongoReop.upsert(blockSyncHeight);
        }


        /**
         * Record the block height contract information that has been successfully synchronized
         */
        private void saveBlockSyncContractHeight(BlockInfoBO blockInfoBO) {
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


        /**
         * Filter the contract events that have been synchronized
         */
        private BlockInfoBO filterBlockInfoBO(BlockInfoBO blockInfoBO) {
            BlockInfoBO copyBlockInfoBO = new BlockInfoBO();
            BeanUtils.copyProperties(blockInfoBO, copyBlockInfoBO);
            List<EventBO> eventBOList = copyBlockInfoBO.getEventBOList();
            if (CollectionUtils.isEmpty(eventBOList)) {
                return blockInfoBO;
            }

            List<EventBO> filterResultEventBOList = new ArrayList<>();
            BlockSyncContractHeight blockSyncContractHeight;
            for (EventBO eventBO : eventBOList) {
                blockSyncContractHeight = blockSyncContractHeightMongoRepo.findByGroupIdAndContractName(blockInfoBO.getGroupId(), eventBO.getContractName());
                // Prove that the events of the contract have not been synchronized
                if (null == blockSyncContractHeight || (eventBO.getBlockNumber().longValue() > blockSyncContractHeight.getBlockNumber())) {
                    filterResultEventBOList.add(eventBO);
                    continue;
                }
                LOG.info("sync data, group id: {}, block number: {}, contract name: {}, already synchronized, no need to synchronize again", blockInfoBO.getGroupId(), blockInfoBO.getBlockNumber(), eventBO.getEventName());
            }
            copyBlockInfoBO.setEventBOList(filterResultEventBOList);
            return copyBlockInfoBO;
        }

        /**
         * Save block information
         */
        private void saveBlockDetailInfo(BlockInfoBO blockInfoBO) {
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

        /**
         * Send error warning
         */
        private void sendErrorMsg(int groupId, long blockNumber, Exception e) {
            String errorMsg;
            if (e instanceof BusinessException) {
                errorMsg = "Warning!!!, Business exception! Business exception! Business exception! Important things are to be repeated for 3 times! Sync group id: " + groupId + ", block number: " + blockNumber + ", data exception: " + e.getMessage();
            } else {
                errorMsg = "Sync group id: " + groupId + ", block number " + blockNumber + ", data exception: " + e.getMessage();
            }
            errorMsg += "\n\n" + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date());
            WechatUtil.send(wechatUrl, errorMsg);
        }
    }
}
