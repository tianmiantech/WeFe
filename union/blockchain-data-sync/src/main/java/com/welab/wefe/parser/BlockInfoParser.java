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

package com.welab.wefe.parser;

import com.welab.wefe.bo.contract.ContractInfo;
import com.welab.wefe.bo.contract.EventMetaInfo;
import com.welab.wefe.bo.contract.FieldInfo;
import com.welab.wefe.bo.data.BlockInfoBO;
import com.welab.wefe.bo.data.EventBO;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.ContractConstants;
import com.welab.wefe.constant.SyncConstant;
import com.welab.wefe.util.TransactionUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransactionReceipt;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Block information interpreter
 *
 * @author aaron.li
 **/
public class BlockInfoParser {
    private final Logger LOG = LoggerFactory.getLogger(BlockInfoParser.class);

    private BcosBlock.Block block;

    public BlockInfoParser(BcosBlock.Block block) {
        this.block = block;
    }

    public static BlockInfoParser create(BcosBlock.Block block) {
        BlockInfoParser blockInfoParser = new BlockInfoParser(block);
        return blockInfoParser;
    }

    /**
     * Interpret the event log from the transaction
     */
    private List<EventBO> parserEvent(TransactionReceipt tr, ContractInfo contractInfo) throws ABICodecException {
        List<EventBO> eventBOList = new ArrayList<>();

        List<EventMetaInfo> eventMetaInfoList = contractInfo.getEventMetaInfoList();
        if (CollectionUtils.isEmpty(eventMetaInfoList)) {
            return eventBOList;
        }
        Map<String, EventMetaInfo> eventMetaInfoMap = eventMetaInfoList.stream()
                .collect(Collectors.toMap(EventMetaInfo::getEventName, e -> e));

        // Interpret the event log
        Map<String, List<List<Object>>> events = SyncConstant.getCurrentContext().getDecoder()
                .decodeEvents(contractInfo.getAbi(), tr.getLogs());

        for (Map.Entry<String, List<List<Object>>> entry : events.entrySet()) {
            String key = entry.getKey();
            if (!eventMetaInfoMap.containsKey(key)) {
                continue;
            }

            EventMetaInfo eventMetaInfo = eventMetaInfoMap.get(key);
            for (List<Object> params : entry.getValue()) {

                Map<String, Object> entity = new HashMap<>(16);
                int i = 0;
                for (FieldInfo fieldInfo : eventMetaInfo.getFieldInfoList()) {
                    if (params.get(i) instanceof List) {
                        entity.put(fieldInfo.getSqlName(), JObject.toJSONString(params.get(i++)));
                        continue;
                    }
                    entity.put(fieldInfo.getSqlName(), params.get(i++));
                }


                entity.put("block_time_stamp", DateUtil.hexStrToDate(block.getTimestamp()));
                entity.put("tx_hash", tr.getTransactionHash());
                entity.put("contract_address", tr.getContractAddress());
                entity.put("block_height", Numeric.toBigInt(tr.getBlockNumber()).longValue());

                EventBO eventBO = new EventBO();
                eventBO.setContractName(contractInfo.getContractName());
                eventBO.setEventName(eventMetaInfo.getEventName());
                eventBO.setContractName(eventMetaInfo.getContractName());
                eventBO.setBlockNumber(block.getNumber());
                eventBO.setFrom(tr.getFrom());
                eventBO.setTo(tr.getTo());
                eventBO.setEntity(entity);
                eventBOList.add(eventBO);
            }
        }
        return eventBOList;
    }

    public BlockInfoBO parse() throws ABICodecException, ContractException {
        BlockInfoBO blockInfoBO = new BlockInfoBO();
        blockInfoBO.setBlockNumber(block.getNumber());
        blockInfoBO.setGroupId(SyncConstant.getCurrentContext().getGroupId());
        List<EventBO> eventBOList = new ArrayList<>();
        List<BcosBlock.TransactionResult> transactionResults = block.getTransactions();
        for (BcosBlock.TransactionResult result : transactionResults) {
            BcosBlock.TransactionObject to = (BcosBlock.TransactionObject) result;
            JsonTransactionResponse transaction = to.get();
            BcosTransactionReceipt bcosTransactionReceipt = SyncConstant.getCurrentContext().getClient().getTransactionReceipt(transaction.getHash());
            Optional<TransactionReceipt> opt = bcosTransactionReceipt.getTransactionReceipt();
            if (!opt.isPresent()) {
                continue;
            }
            TransactionReceipt tr = opt.get();
            ContractInfo contractInfo = TransactionUtil.getContractInfoByTransaction(transaction);
            if (null == contractInfo) {
                continue;
            }
            // Transaction destination address
            String trContractAddress = transaction.getTo();
            if (StringUtil.isNotEmpty(trContractAddress) && !trContractAddress.equals(ContractConstants.EMPTY_ADDRESS)) {
                tr.setContractAddress(trContractAddress);
            }

            // Explain the event
            eventBOList.addAll(parserEvent(tr, contractInfo));
        }

        blockInfoBO.setEventBOList(eventBOList);
        return blockInfoBO;
    }


}
