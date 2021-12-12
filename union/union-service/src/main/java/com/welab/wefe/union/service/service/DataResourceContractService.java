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
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.contract.DataResourceContract;
import com.welab.wefe.union.service.contract.DataSetContract;
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
@Transactional(transactionManager = "transactionUnionManager", rollbackFor = Exception.class)
public abstract class DataResourceContractService extends AbstractContractService {

    @Autowired
    private DataResourceContract dataResourceContract;
    @Autowired
    private CryptoSuite cryptoSuite;
    @Autowired
    private MemberContractService memberContractService;
    @Autowired
    private DataResourceMongoReop dataResourceMongoReop;

    public void upsert(DataResource dataResource) throws StatusCodeWithException {
        try {
            String extJson = JObject.toJSONString(dataResource.getExtJson());
            TransactionReceipt insertTransactionReceipt = dataResourceContract.insert(generateParams(dataResource),
                    extJson);

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(DataResourceContract.ABI, DataResourceContract.FUNC_INSERT, insertTransactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to add data set information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }

    public void deleteById(String dataSetId) throws StatusCodeWithException {
        try {
            boolean isExist = dataResourceContract.isExist(dataSetId);
            if (isExist) {
                TransactionReceipt transactionReceipt = dataResourceContract.deleteByDataResourceId(dataSetId);
                // Get receipt result
                TransactionResponse deleteResponse = new TransactionDecoderService(cryptoSuite)
                        .decodeReceiptWithValues(DataResourceContract.ABI, DataResourceContract.FUNC_DELETEBYDATARESOURCEID, transactionReceipt);
                if (!transactionIsSuccess(deleteResponse.getValues())) {
                    throw new StatusCodeWithException("transaction failed", StatusCode.SYSTEM_ERROR);
                }
            }
        } catch (Exception e) {
            throw new StatusCodeWithException("Failed to delete data resource information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    private List<String> generateParams(DataResource dataResource) {
        List<String> list = new ArrayList<>();
        list.add(dataResource.getDataResourceId());
        list.add(dataResource.getMemberId());
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
        list.add(StringUtil.isEmptyToBlank(dataResource.getDataResourceType()));
        list.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date())));
        list.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date())));
        return list;
    }
}
