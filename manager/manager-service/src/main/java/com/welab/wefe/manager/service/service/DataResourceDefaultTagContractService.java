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

package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceDefaultTag;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.manager.service.contract.DataResourceDefaultTagContract;
import com.welab.wefe.manager.service.dto.tag.DataResourceDefaultTagUpdateInput;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Service
public class DataResourceDefaultTagContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(DataResourceDefaultTagContractService.class);

    @Autowired
    private DataResourceDefaultTagContract dataResourceDefaultTagContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    /**
     * add dataResourceDefaultTag
     */
    public void add(DataResourceDefaultTag dataResourceDefaultTag) throws StatusCodeWithException {
        try {
            // send transaction
            TransactionReceipt transactionReceipt = dataResourceDefaultTagContract.insert(
                    generateParams(dataResourceDefaultTag),
                    JObject.toJSONString(dataResourceDefaultTag.getExtJson())
            );

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(DataResourceDefaultTagContract.ABI, DataResourceDefaultTagContract.FUNC_INSERT, transactionReceipt);


            LOG.info("DataResourceDefaultTag contract insert transaction, tagId id: {},  receipt response: {}", dataResourceDefaultTag.getTagId(), JObject.toJSON(transactionResponse).toString());

            transactionIsSuccess(transactionResponse);

        } catch (StatusCodeWithException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error("add dataResourceDefaultTag error: ", e);
            throw new StatusCodeWithException("add dataResourceDefaultTag error: ", StatusCode.SYSTEM_ERROR);
        }
    }


    public void updateByTagId(DataResourceDefaultTagUpdateInput input) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = dataResourceDefaultTagContract.update(
                    input.getTagId(),
                    input.getTagName(),
                    JObject.toJSONString(input.getExtJson()),
                    StringUtil.isEmptyToBlank(String.valueOf(System.currentTimeMillis()))
            );

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(DataResourceDefaultTagContract.ABI, DataResourceDefaultTagContract.FUNC_UPDATE, transactionReceipt);


            transactionIsSuccess(transactionResponse);
        } catch (Exception e) {
            throw new StatusCodeWithException("updateByTagId failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    public void deleteByTagId(String tagId) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = dataResourceDefaultTagContract.deleteByTagId(tagId);

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(DataResourceDefaultTagContract.ABI, DataResourceDefaultTagContract.FUNC_DELETEBYTAGID, transactionReceipt);


            transactionIsSuccess(transactionResponse);
        } catch (Exception e) {
            throw new StatusCodeWithException("deleteByTagId failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    private List<String> generateParams(DataResourceDefaultTag dataResourceDefaultTag) {
        List<String> list = new ArrayList<>();
        list.add(dataResourceDefaultTag.getTagId());
        list.add(dataResourceDefaultTag.getTagName());
        list.add(dataResourceDefaultTag.getDataResourceType().toString());
        list.add(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        list.add(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        return list;
    }

}
