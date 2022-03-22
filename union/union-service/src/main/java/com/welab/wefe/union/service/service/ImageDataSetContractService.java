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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yuxin.zhang
 */
@Service
public class ImageDataSetContractService extends AbstractContractService {

    @Autowired
    private ImageDataSetContract imageDataSetContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    public void add(ImageDataSet imageDataSet) throws StatusCodeWithException {
        try {

            TransactionReceipt transactionReceipt = imageDataSetContract.insert(
                    generateAddParams(imageDataSet),
                    JObject.toJSONString(imageDataSet.getExtJson())
            );

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(ImageDataSetContract.ABI, ImageDataSetContract.FUNC_INSERT, transactionReceipt);

            String responseValues = transactionResponse.getValues();

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to add ImageDataSet information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }

    public void update(ImageDataSet imageDataSet) throws StatusCodeWithException {
        try {
            String updatedTime = DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date());
            TransactionReceipt transactionReceipt = imageDataSetContract.update(
                    imageDataSet.getDataResourceId(),
                    generateUpdateParams(imageDataSet),
                    updatedTime
            );

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(ImageDataSetContract.ABI, ImageDataSetContract.FUNC_UPDATE, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to update ImageDataSet set information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }


    public void delete(String dataResourceId) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = imageDataSetContract.deleteByDataResourceId(dataResourceId);

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(ImageDataSetContract.ABI, ImageDataSetContract.FUNC_DELETEBYDATARESOURCEID, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to update ImageDataSet information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    private List<String> generateAddParams(ImageDataSet imageDataSet) {
        List<String> list = new ArrayList<>();
        list.add(imageDataSet.getDataResourceId());
        list.addAll(generateParams(imageDataSet));
        list.add(imageDataSet.getCreatedTime());
        list.add(imageDataSet.getUpdatedTime());
        return list;
    }

    private List<String> generateUpdateParams(ImageDataSet imageDataSet) {
        return generateParams(imageDataSet);
    }

    private List<String> generateParams(ImageDataSet imageDataSet) {
        List<String> list = new ArrayList<>();
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getForJobType().name()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getLabelList()));
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getLabeledCount()));
        list.add(imageDataSet.getLabelCompleted());
        list.add(StringUtil.isEmptyToBlank(imageDataSet.getFileSize()));
        return list;
    }
}
