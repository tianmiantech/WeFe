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
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.contract.ImageDataSetContract;
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
public class ImageDataSetContractService extends AbstractContractService {


    @Autowired
    private MemberContractService memberContractService;
    @Autowired
    private ImageDataSetContract imageDataSetContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    public void upsert(ImageDataSet imageDataSet) throws StatusCodeWithException {
        try {
            if (!memberContractService.isExist(imageDataSet.getMemberId())) {
                throw new StatusCodeWithException("Member ID is not exist", StatusCode.INVALID_USER);
            }

            TransactionReceipt insertTransactionReceipt = imageDataSetContract.insert(generateParams(imageDataSet, true),
                    JObject.toJSONString(imageDataSet.getExtJson()));

            // Get receipt result
            TransactionResponse insertResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(ImageDataSetContract.ABI, ImageDataSetContract.FUNC_INSERT, insertTransactionReceipt);
            String values = insertResponse.getValues();
            if (!transactionIsSuccess(values) && transactionDataIsExist(values)) {
                TransactionReceipt updateTransactionReceipt = imageDataSetContract.update(generateParams(imageDataSet, false));

                // Get receipt result
                TransactionResponse updateResponse = new TransactionDecoderService(cryptoSuite)
                        .decodeReceiptWithValues(ImageDataSetContract.ABI, ImageDataSetContract.FUNC_UPDATE, updateTransactionReceipt);
                transactionIsSuccess(updateResponse);
            } else {
                transactionIsSuccess(insertResponse);
            }

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to add data set information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }



    public void updateLabeledCount(String dataSetId, String labeledCount, String sampleCount,String labelList,String labelCompleted) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = imageDataSetContract.updateLabeledCount(
                    dataSetId,
                    labeledCount,
                    sampleCount,
                    labelList,
                    labelCompleted,
                    DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(ImageDataSetContract.ABI, ImageDataSetContract.FUNC_UPDATELABELEDCOUNT, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to add data set information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }


    public void deleteById(String dataSetId) throws StatusCodeWithException {
        try {
            boolean isExist = imageDataSetContract.isExist(dataSetId);
            if (isExist) {
                TransactionReceipt transactionReceipt = imageDataSetContract.deleteByDataSetId(dataSetId);
                // Get receipt result
                TransactionResponse deleteResponse = new TransactionDecoderService(cryptoSuite)
                        .decodeReceiptWithValues(ImageDataSetContract.ABI, ImageDataSetContract.FUNC_DELETEBYDATASETID, transactionReceipt);

                transactionIsSuccess(deleteResponse);
            }
        } catch (Exception e) {
            throw new StatusCodeWithException("Failed to delete data set information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    private List<String> generateParams(ImageDataSet imageDataSet, boolean isAdd) {
        List<String> list = new ArrayList<>();
        list.add(imageDataSet.getDataSetId());
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getMemberId()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getName()));
        list.add(String.valueOf(imageDataSet.getTags()));
        list.add(String.valueOf(imageDataSet.getDescription()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getForJobType()));
        list.add(String.valueOf(imageDataSet.getLabelList()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getSampleCount()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getLabeledCount()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getLabelCompleted()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getFilesSize()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getPublicLevel()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getPublicMemberList()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getUsageCountInJob()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getUsageCountInFlow()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getUsageCountInProject()));
        if (isAdd) {
            list.add(StringUtil.isEmptyToBlank(imageDataSet.getEnable()));
            list.add(StringUtil.isEmptyToBlank(imageDataSet.getCreatedTime()));
        }
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getUpdatedTime()));
        return list;
    }
}
