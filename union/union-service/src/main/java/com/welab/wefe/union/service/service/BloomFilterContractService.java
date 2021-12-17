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
import com.welab.wefe.common.data.mongodb.entity.union.BloomFilter;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.contract.BloomFilterContract;
import com.welab.wefe.union.service.contract.DataResourceContract;
import com.welab.wefe.union.service.contract.DataSetContract;
import com.welab.wefe.union.service.entity.DataSet;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yuxin.zhang
 */
@Service
public class BloomFilterContractService extends AbstractContractService {

    @Autowired
    private BloomFilterContract bloomFilterContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    public void add(BloomFilter bloomFilter) throws StatusCodeWithException {
        try {

            TransactionReceipt transactionReceipt = bloomFilterContract.insert(
                    generateParams(bloomFilter),
                    JObject.toJSONString(bloomFilter.getExtJson())
            );

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(BloomFilterContract.ABI, BloomFilterContract.FUNC_INSERT, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to BloomFilter information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    public void updateHashFuntion(String dataResourceId,String hashFunction) throws StatusCodeWithException {
        try {
            String updatedTime = DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date());
            TransactionReceipt transactionReceipt = bloomFilterContract.updateHashFuntion(
                    dataResourceId,
                    hashFunction,
                    updatedTime
            );

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(BloomFilterContract.ABI, BloomFilterContract.FUNC_UPDATEHASHFUNTION, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to add BloomFilter information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    public void delete(String dataResourceId) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = bloomFilterContract.deleteByDataResourceId(dataResourceId);

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(BloomFilterContract.ABI, BloomFilterContract.FUNC_DELETEBYDATARESOURCEID, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to update BloomFilter information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    private List<String> generateParams(BloomFilter bloomFilter) {
        List<String> list = new ArrayList<>();
        list.add(bloomFilter.getDataResourceId());
        list.add(StringUtil.isEmptyToBlank(bloomFilter.getHashFunction()));
        list.add(bloomFilter.getCreatedTime());
        list.add(bloomFilter.getUpdatedTime());
        return list;
    }
}
