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

package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.manager.service.contract.DataSetContract;
import com.welab.wefe.manager.service.dto.dataset.DataSetUpdateExtJsonInput;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(transactionManager = "transactionUnionManager", rollbackFor = Exception.class)
public class DataSetContractService extends AbstractContractService {

    @Autowired
    private DataSetContract dataSetContract;
    @Autowired
    private DataSetMongoReop dataSetMongoReop;
    @Autowired
    private CryptoSuite cryptoSuite;

    public void updateExtJson(DataSetUpdateExtJsonInput input) throws StatusCodeWithException {
        try {
            JObject extJson = JObject.create(dataSetMongoReop.findDataSetId(input.getId()).getExtJson());
            Field[] fields = input.getExtJson().getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                if (null != fields[i].get(input.getExtJson())) {
                    extJson.put(StringUtil.camelCaseToUnderLineCase(fields[i].getName()), fields[i].get(input.getExtJson()));
                }
            }
            TransactionReceipt transactionReceipt = dataSetContract.updateExtJson(input.getId(),
                    extJson.toString());

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(DataSetContract.ABI, DataSetContract.FUNC_UPDATEEXTJSON, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to updateExtJson set information: " + e, StatusCode.SYSTEM_ERROR);
        }
    }
}
