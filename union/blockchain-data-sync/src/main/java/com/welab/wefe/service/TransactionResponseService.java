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
import com.welab.wefe.bo.data.TransactionResponseBO;
import com.welab.wefe.common.data.mongodb.entity.union.TransactionResponseDetailInfo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Transaction response service class
 *
 * @author aaron.li
 * @date 2021/12/31 14:07
 **/
@Service
public class TransactionResponseService extends BaseService {
    private final static String COLLECTION_NAME_PREFIX = "BlockTr_";
    private final static String EMPTY_CONTRACT_NAME = "empty";
    private final static String EMPTY_EVENT_NAME = "empty";

    @Autowired
    protected MongoTemplate mongoTemplate;

    /**
     * save
     */
    public void save(BlockInfoBO blockInfoBO) {
        try {
            if (null == blockInfoBO) {
                return;
            }
            List<TransactionResponseBO> transactionResponseBOList = blockInfoBO.getTransactionResponseBOList();
            if (CollectionUtils.isEmpty(transactionResponseBOList)) {
                return;
            }

            for (TransactionResponseBO transactionResponseBO : transactionResponseBOList) {
                String eventName = getEventName(transactionResponseBO);
                String contractName = (StringUtil.isEmpty(transactionResponseBO.getContractName()) ? EMPTY_CONTRACT_NAME : transactionResponseBO.getContractName());

                TransactionResponseDetailInfo detailInfo = new TransactionResponseDetailInfo();
                detailInfo.setTransactionHash(transactionResponseBO.getTransactionHash());
                detailInfo.setBlockNumber(transactionResponseBO.getBlockNumber());
                detailInfo.setContractAddress(transactionResponseBO.getContractAddress());
                detailInfo.setContractName(contractName);
                detailInfo.setEventName(eventName);
                detailInfo.setData(JObject.create(JObject.toJSONString(transactionResponseBO.getTransactionResponse())));

                String collectionName = buildCollectionName(contractName, eventName);
                Index index = new Index();
                index.on("transaction_hash", Sort.Direction.ASC);
                index.background();
                mongoTemplate.indexOps(collectionName).ensureIndex(index);

                TransactionResponseDetailInfo historyDetailInfo = getByTransactionHash(collectionName, transactionResponseBO.getTransactionHash());
                if (null != historyDetailInfo) {
                    detailInfo.setId(historyDetailInfo.getId());
                }


                mongoTemplate.save(detailInfo, collectionName);
            }
        } catch (Exception e) {
            LOG.error("Failed to save transaction response info with group id:" + blockInfoBO.getGroupId() + ", block: " + blockInfoBO.getBlockNumber() + ", exception info: ", e);
        }
    }


    private TransactionResponseDetailInfo getByTransactionHash(String collectionName, String transactionHash) {
        if (StringUtil.isEmpty(transactionHash)) {
            return null;
        }

        Query query = new QueryBuilder().append("transactionHash", transactionHash).build();
        return mongoTemplate.findOne(query, TransactionResponseDetailInfo.class, collectionName);
    }

    private String buildCollectionName(String contractName, String eventName) {
        return COLLECTION_NAME_PREFIX + contractName + "_" + eventName;
    }

    /**
     * Extract event name from transaction
     */
    private String getEventName(TransactionResponseBO transactionResponseBO) {
        JObject dataObj = transactionResponseToJson(transactionResponseBO.getTransactionResponse());
        JObject eventObj = dataObj.getJObject("event_result_map");
        if (null == eventObj || eventObj.isEmpty()) {
            return EMPTY_EVENT_NAME;
        }
        Set<String> keySet = eventObj.getInnerMap().keySet();
        String eventName = keySet.stream().findFirst().get();
        return StringUtil.isEmpty(eventName) ? EMPTY_EVENT_NAME : eventName;
    }

    private JObject transactionResponseToJson(TransactionResponse transactionResponse) {
        if (null == transactionResponse) {
            return JObject.create();
        }
        return JObject.create(JObject.toJSONString(transactionResponse));
    }
}
