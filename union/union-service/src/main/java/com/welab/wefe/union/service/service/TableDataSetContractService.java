/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.union.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.TableDataSet;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.contract.TableDataSetContract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(transactionManager = "transactionUnionManager", rollbackFor = Exception.class)
public class TableDataSetContractService extends DataResourceContractService {

    @Autowired
    private TableDataSetContract tableDataSetContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    public void add(TableDataSet tableDataSet) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = tableDataSetContract.insert(
                    generateAddParams(tableDataSet),
                    JObject.toJSONString(tableDataSet.getExtJson())
            );

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(TableDataSetContract.ABI, TableDataSetContract.FUNC_INSERT, transactionReceipt);

            transactionIsSuccess(transactionResponse);
        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to add TableDataSet information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }

    public void update(TableDataSet tableDataSet) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = tableDataSetContract.update(
                    tableDataSet.getDataResourceId(),
                    generateUpdateParams(tableDataSet),
                    tableDataSet.getUpdatedTime()
            );

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(TableDataSetContract.ABI, TableDataSetContract.FUNC_INSERT, transactionReceipt);

            transactionIsSuccess(transactionResponse);
        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to add TableDataSet information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }


    public void deleteById(String dataResourceId) throws StatusCodeWithException {
        try {
            boolean isExist = tableDataSetContract.isExist(dataResourceId);
            if (isExist) {
                TransactionReceipt transactionReceipt = tableDataSetContract.deleteByDataResourceId(dataResourceId);
                // Get receipt result
                TransactionResponse deleteResponse = new TransactionDecoderService(cryptoSuite)
                        .decodeReceiptWithValues(TableDataSetContract.ABI, TableDataSetContract.FUNC_DELETEBYDATARESOURCEID, transactionReceipt);
                if (!transactionIsSuccess(deleteResponse.getValues())) {
                    throw new StatusCodeWithException("transaction failed", StatusCode.SYSTEM_ERROR);
                }
            }
        } catch (Exception e) {
            throw new StatusCodeWithException("Failed to delete TableDataSet information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    private List<String> generateAddParams(TableDataSet tableDataSet) {
        List<String> list = new ArrayList<>();
        list.add(tableDataSet.getDataResourceId());
        list.addAll(generateParams(tableDataSet));
        list.add(tableDataSet.getCreatedTime());
        list.add(tableDataSet.getUpdatedTime());
        return list;
    }

    private List<String> generateUpdateParams(TableDataSet tableDataSet) {
        List<String> list = generateParams(tableDataSet);
        list.add(tableDataSet.getUpdatedTime());
        return list;
    }

    private List<String> generateParams(TableDataSet tableDataSet) {
        List<String> list = new ArrayList<>();
        list.add(tableDataSet.getContainsY());
        list.add(StringUtil.isEmptyToBlank(tableDataSet.getColumnCount()));
        list.add(StringUtil.isEmptyToBlank(tableDataSet.getColumnNameList()));
        list.add(StringUtil.isEmptyToBlank(tableDataSet.getFeatureCount()));
        list.add(StringUtil.isEmptyToBlank(tableDataSet.getFeatureNameList()));
        return list;
    }
}
