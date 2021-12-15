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
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.contract.DataResourceContract;
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
public abstract class DataResourceContractService extends AbstractContractService {

    @Autowired
    private DataResourceContract dataResourceContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    public void add(DataResource dataResource) throws StatusCodeWithException {
        try {
            String extJson = JObject.toJSONString(dataResource.getExtJson());
            TransactionReceipt insertTransactionReceipt = dataResourceContract.insert(generateAddParams(dataResource),
                    extJson);

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(DataResourceContract.ABI, DataResourceContract.FUNC_INSERT, insertTransactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to add DataResource information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }

    public void update(DataResource dataResource) throws StatusCodeWithException {
        try {
            String updatedTime = DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date());
            TransactionReceipt transactionReceipt = dataResourceContract.update(
                    dataResource.getDataResourceId(),
                    generateUpdateParams(dataResource),
                    updatedTime
            );

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(DataResourceContract.ABI, DataResourceContract.FUNC_UPDATE, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to update DataResource information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }

    private List<String> generateAddParams(DataResource dataResource) {
        List<String> list = new ArrayList<>();
        list.add(dataResource.getDataResourceId());
        list.add(dataResource.getMemberId());
        list.addAll(generateParams(dataResource));
        list.add(dataResource.getDataResourceType());
        list.add(dataResource.getCreatedTime());
        list.add(dataResource.getUpdatedTime());
        return list;
    }

    private List<String> generateUpdateParams(DataResource dataResource) {
        return generateParams(dataResource);
    }

    private List<String> generateParams(DataResource dataResource) {
        List<String> list = new ArrayList<>();
        list.add(dataResource.getName());
        list.add(StringUtil.isEmptyToBlank(dataResource.getDescription()));
        list.add(StringUtil.isEmptyToBlank(dataResource.getTags()));
        list.add(StringUtil.isEmptyToBlank(dataResource.getTotalDataCount()));
        list.add(StringUtil.isEmptyToBlank(dataResource.getPublicLevel()));
        list.add(StringUtil.isEmptyToBlank(dataResource.getPublicMemberList()));
        list.add(StringUtil.isEmptyToBlank(dataResource.getUsageCountInJob()));
        list.add(StringUtil.isEmptyToBlank(dataResource.getUsageCountInFlow()));
        list.add(StringUtil.isEmptyToBlank(dataResource.getUsageCountInProject()));
        list.add(StringUtil.isEmptyToBlank(dataResource.getUsageCountInMember()));
        return list;
    }
}
