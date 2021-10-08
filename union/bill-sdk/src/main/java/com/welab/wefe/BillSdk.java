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

package com.welab.wefe;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.BlockChainContext;
import com.welab.wefe.contract.BillContract;
import com.welab.wefe.model.BillModel;
import org.apache.commons.collections.map.HashedMap;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @author yuxin.zhang
 */
public class BillSdk {
    private static final Logger LOG = LoggerFactory.getLogger(BillSdk.class);
    static Map<Integer, BlockChainContext> blockChainContextMap = new HashedMap();
    private static final BillSdk BILL_SDK = new BillSdk();


    public static BillSdk getInstance() {
        return BILL_SDK;
    }

    private BillSdk() {
    }

    public void init() {
        BlockChainContext blockChainContext = new BlockChainContext(1);
        try {
            blockChainContext.init();
            blockChainContextMap.put(1, blockChainContext);
            List<String> groupList = blockChainContext.getClient().getGroupList().getGroupList();
            for (String groupId : groupList) {
                blockChainContextMap.put(
                        Integer.parseInt(groupId)
                        , new BlockChainContext(Integer.parseInt(groupId)).init()
                );
            }
            LOG.error("Init com.welab.wefe.BillSdk success");
        } catch (Exception e) {
            LOG.error("Init com.welab.wefe.BillSdk exception: ", e);
        }
    }

    private void checkGroupInit(int groupId) throws Exception {
        if (null == blockChainContextMap.get(groupId)) {
            try {
                blockChainContextMap.put(
                        groupId,
                        new BlockChainContext(groupId).init());
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /**
     * Store call records
     * @param billModel
     * @param groupId
     * @return
     * @throws Exception
     */
    public boolean save(BillModel billModel, int groupId) throws Exception {
        boolean ret = false;
        checkGroupInit(groupId);
        BlockChainContext blockChainContext = blockChainContextMap.get(groupId);
        BillContract billContract = blockChainContext.getLatestVersionBillContract();
        try {
            TransactionReceipt transactionReceipt = billContract.insert(
                    billModel.getId(),
                    billModel.getSeqNo(),
                    billModel.getFromMemberId(),
                    billModel.getModelId(),
                    billModel.getAlgorithm(),
                    billModel.getFlType(),
                    billModel.getMyRole(),
                    new BigInteger(String.valueOf(billModel.getCreatedTime()))
            );

            // Get receipt results
            TransactionResponse insertResponse = blockChainContext.getTransactionDecoder()
                    .decodeReceiptWithValues(BillContract.ABI, "insert", transactionReceipt);

            // Transaction execution failed
            if (transactionIsSuccess(insertResponse.getValues())) {
                ret = true;
            }

        } catch (Exception e) {
            LOG.error("insert bill exception:", e);
        }

        return ret;
    }


    /**
     * Is the transaction executed successfully
     *
     * @param responseValues Receipt response result
     * @return true：Successful trade; False: transaction failed
     */
    private boolean transactionIsSuccess(String responseValues) {
        JSONArray values = JSONObject.parseArray(responseValues);
        if (null == values || values.isEmpty() || values.getIntValue(0) <= 0) {
            return false;
        }
        return true;
    }

}
