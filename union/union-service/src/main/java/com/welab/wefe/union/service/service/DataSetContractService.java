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

import com.alibaba.fastjson.JSON;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractMongoModel;
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceLazyUpdateModel;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetExtJSON;
import com.welab.wefe.common.data.mongodb.repo.*;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.api.dataresource.LazyUpdateApi;
import com.welab.wefe.union.service.contract.DataSetContract;
import com.welab.wefe.union.service.entity.DataSet;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuxin.zhang
 */
@Service
public class DataSetContractService extends AbstractContractService {

    @Autowired
    private DataSetContract dataSetContract;
    @Autowired
    private CryptoSuite cryptoSuite;
    @Autowired
    private MemberContractService memberContractService;
    @Autowired
    private DataSetMongoReop dataSetMongoReop;
    @Autowired
    private ImageDataSetMongoReop imageDataSetMongoReop;
    @Autowired
    private TableDataSetMongoReop tableDataSetMongoReop;
    @Autowired
    private BloomFilterMongoReop bloomFilterMongoReop;
    @Autowired
    private DataResourceLazyUpdateModelMongoReop dataResourceLazyUpdateModelMongoReop;

    public void upsert(DataSet dataset) throws StatusCodeWithException {
        try {
            String extJson;
            if (null != dataSetMongoReop.findDataSetId(dataset.getId())) {
                extJson = JObject.create(dataSetMongoReop.findDataSetId(dataset.getId()).getExtJson()).toString();
            } else {
                DataSetExtJSON dataSetExtJSON = new DataSetExtJSON();
                dataSetExtJSON.setEnable(true);
                extJson = JSON.toJSONString(dataSetExtJSON);
            }
            if (!memberContractService.isExist(dataset.getMemberId())) {
                throw new StatusCodeWithException("Member ID is not exist", StatusCode.INVALID_USER);
            }

            TransactionReceipt insertTransactionReceipt = dataSetContract.insert(generateParams(dataset),
                    extJson);

            // Get receipt result
            TransactionResponse insertResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(DataSetContract.ABI, DataSetContract.FUNC_INSERT, insertTransactionReceipt);

            // Transaction execution failed
            if (!transactionIsSuccess(insertResponse.getValues())) {
                // Update the data if it exists
                TransactionReceipt updateTransactionReceipt = dataSetContract.update(generateParams(dataset),
                        extJson);

                // Get receipt result
                TransactionResponse updateResponse = new TransactionDecoderService(cryptoSuite)
                        .decodeReceiptWithValues(DataSetContract.ABI, DataSetContract.FUNC_UPDATE, updateTransactionReceipt);
                if (!transactionIsSuccess(updateResponse.getValues())) {
                    throw new StatusCodeWithException("Failed to update data set information", StatusCode.SYSTEM_ERROR);
                }
            }
        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to add data set information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }


    public void deleteById(String dataSetId) throws StatusCodeWithException {
        try {
            boolean isExist = dataSetContract.isExist(dataSetId);
            if (isExist) {
                TransactionReceipt transactionReceipt = dataSetContract.deleteByDataSetId(dataSetId);
                // Get receipt result
                TransactionResponse deleteResponse = new TransactionDecoderService(cryptoSuite)
                        .decodeReceiptWithValues(DataSetContract.ABI, DataSetContract.FUNC_DELETEBYDATASETID, transactionReceipt);
                if (!transactionIsSuccess(deleteResponse.getValues())) {
                    throw new StatusCodeWithException("transaction failed", StatusCode.SYSTEM_ERROR);
                }
            }
        } catch (Exception e) {
            throw new StatusCodeWithException("Failed to delete data set information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    public void lazyUpdate(LazyUpdateApi.Input input) throws StatusCodeWithException {
        AbstractMongoModel dataResourceModel = null;
        switch (input.getDataResourceType()) {
            case ImageDataSet:
                dataResourceModel = imageDataSetMongoReop.findByDataResourceId(input.getDataResourceId());
                break;
            case TableDataSet:
                dataResourceModel = tableDataSetMongoReop.findByDataResourceId(input.getDataResourceId());
                break;
            case BloomFilter:
                dataResourceModel = bloomFilterMongoReop.findByDataResourceId(input.getDataResourceId());
                break;
            default:
                throw new StatusCodeWithException(StatusCode.INVALID_DATASET, "非法的数据源类型：" + input.getDataResourceType());
        }
        if (null == dataResourceModel) {
            throw new StatusCodeWithException(StatusCode.INVALID_DATASET, input.getDataResourceId());
        }
        DataResourceLazyUpdateModel dataResourceLazyUpdateModel = dataResourceLazyUpdateModelMongoReop.findByDataResourceId(input.getDataResourceId());
        dataResourceLazyUpdateModel = (null == dataResourceLazyUpdateModel ? new DataResourceLazyUpdateModel() : dataResourceLazyUpdateModel);
        dataResourceLazyUpdateModel.setDataResourceId(input.getDataResourceId());
        dataResourceLazyUpdateModel.setDataResourceType(input.getDataResourceType());
        dataResourceLazyUpdateModel.setLabeledCount(null == input.getLabeledCount() ? 0 : input.getLabeledCount());
        dataResourceLazyUpdateModel.setTotalDataCount(null == input.getTotalDataCount() ? 0 : input.getTotalDataCount());
        dataResourceLazyUpdateModel.setLabelList(input.getLabelList());
        dataResourceLazyUpdateModel.setUsageCountInJob(null == input.getUsageCountInJob() ? 0 : input.getUsageCountInJob());
        dataResourceLazyUpdateModel.setUsageCountInFlow(null == input.getUsageCountInFlow() ? 0 : input.getUsageCountInFlow());
        dataResourceLazyUpdateModel.setUsageCountInProject(null == input.getUsageCountInProject() ? 0 : input.getUsageCountInProject());
        dataResourceLazyUpdateModel.setUsageCountInMember(null == input.getUsageCountInMember() ? 0 : input.getUsageCountInMember());
        dataResourceLazyUpdateModel.setLabelCompleted(Boolean.TRUE.equals(input.getLabelCompleted()));
        dataResourceLazyUpdateModelMongoReop.save(dataResourceLazyUpdateModel);
    }

    private List<String> generateParams(DataSet dataSet) {
        List<String> list = new ArrayList<>();
        list.add(dataSet.getId());
        list.add(StringUtil.isEmptyToBlank(dataSet.getMemberId()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getName()));
        list.add(String.valueOf(dataSet.getContainsY()));
        list.add(String.valueOf(dataSet.getRowCount()));
        list.add(String.valueOf(dataSet.getColumnCount()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getColumnNameList()));
        list.add(String.valueOf(dataSet.getFeatureCount()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getFeatureNameList()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getPublicLevel()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getPublicMemberList()));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(dataSet.getUsageCountInJob())));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(dataSet.getUsageCountInFlow())));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(dataSet.getUsageCountInProject())));
        list.add(StringUtil.isEmptyToBlank(dataSet.getDescription()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getTags()));
        list.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(dataSet.getCreatedTime())));
        list.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(dataSet.getUpdatedTime())));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(System.currentTimeMillis())));
        return list;
    }
}
