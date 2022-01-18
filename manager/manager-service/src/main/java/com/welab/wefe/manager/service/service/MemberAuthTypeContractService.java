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
import com.welab.wefe.common.data.mongodb.entity.union.MemberAuthType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.manager.service.contract.MemberAuthTypeContract;
import com.welab.wefe.manager.service.dto.authtype.MemberAuthTypeUpdateInput;
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
public class MemberAuthTypeContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(MemberAuthTypeContractService.class);

    @Autowired
    private MemberAuthTypeContract memberAuthTypeContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    /**
     * add member auth type
     */
    public void add(MemberAuthType memberAuthType) throws StatusCodeWithException {
        try {
            // send transaction
            TransactionReceipt transactionReceipt = memberAuthTypeContract.insert(
                    generateParams(memberAuthType),
                    JObject.toJSONString(memberAuthType.getExtJson())
            );

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberAuthTypeContract.ABI, MemberAuthTypeContract.FUNC_INSERT, transactionReceipt);


            LOG.info("MemberAuthType contract insert transaction, type id: {},  receipt response: {}", memberAuthType.getTypeId(), JObject.toJSON(transactionResponse).toString());

            transactionIsSuccess(transactionResponse);

        } catch (StatusCodeWithException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error("add MemberAuthType error: ", e);
            throw new StatusCodeWithException("add MemberAuthType error: ", StatusCode.SYSTEM_ERROR);
        }
    }


    public void updateByTypeId(MemberAuthTypeUpdateInput input) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = memberAuthTypeContract.update(
                    input.getTypeId(),
                    input.getTypeName(),
                    StringUtil.isEmptyToBlank(String.valueOf(System.currentTimeMillis()))
            );

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberAuthTypeContract.ABI, MemberAuthTypeContract.FUNC_UPDATE, transactionReceipt);

            transactionIsSuccess(transactionResponse);
        } catch (Exception e) {
            throw new StatusCodeWithException("updateByTypeId failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    public void deleteByTypeId(String typeId) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = memberAuthTypeContract.deleteByTypeId(typeId);

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberAuthTypeContract.ABI, MemberAuthTypeContract.FUNC_DELETEBYTYPEID, transactionReceipt);

            transactionIsSuccess(transactionResponse);
        } catch (Exception e) {
            throw new StatusCodeWithException("deleteByTypeId failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    private List<String> generateParams(MemberAuthType memberAuthType) {
        List<String> list = new ArrayList<>();
        System.out.println(memberAuthType.getTypeId());
        list.add(memberAuthType.getTypeId());
        list.add(memberAuthType.getTypeName());

        list.add(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        list.add(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        return list;
    }

}
