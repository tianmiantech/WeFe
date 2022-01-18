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
import com.welab.wefe.common.data.mongodb.entity.union.RealnameAuthAgreementTemplate;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.manager.service.contract.RealnameAuthAgreementTemplateContract;
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

import static com.welab.wefe.common.util.DateUtil.toStringYYYY_MM_DD_HH_MM_SS2;

/**
 * @author yuxin.zhang
 **/
@Service
public class RealnameAuthAgreementTemplateContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(RealnameAuthAgreementTemplateContractService.class);

    @Autowired
    private RealnameAuthAgreementTemplateContract contract;
    @Autowired
    private CryptoSuite cryptoSuite;

    /**
     * add UnionNode
     */
    public void add(RealnameAuthAgreementTemplate realnameAuthAgreementTemplate) throws StatusCodeWithException {
        try {
            // send transaction
            TransactionReceipt transactionReceipt = contract.insert(
                    generateAddParams(realnameAuthAgreementTemplate),
                    JObject.toJSONString(realnameAuthAgreementTemplate.getExtJson())
            );

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(RealnameAuthAgreementTemplateContract.ABI, RealnameAuthAgreementTemplateContract.FUNC_INSERT, transactionReceipt);


            LOG.info("RealnameAuthAgreementTemplateContract insert transaction, templateFileId: {},  receipt response: {}", realnameAuthAgreementTemplate.getTemplateFileId(), JObject.toJSON(transactionResponse).toString());

            transactionIsSuccess(transactionResponse);

        } catch (StatusCodeWithException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error("add RealnameAuthAgreementTemplate error: ", e);
            throw new StatusCodeWithException("add RealnameAuthAgreementTemplate error: ", StatusCode.SYSTEM_ERROR);
        }
    }


    public void enable(String template_file_id, boolean enable) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = contract.updateEnable(
                    template_file_id,
                    String.valueOf(enable ? 1 : 0),
                    toStringYYYY_MM_DD_HH_MM_SS2(new Date())
            );

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(RealnameAuthAgreementTemplateContract.ABI, RealnameAuthAgreementTemplateContract.FUNC_UPDATEENABLE, transactionReceipt);

            transactionIsSuccess(transactionResponse);
        } catch (Exception e) {
            throw new StatusCodeWithException("enable failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    private List<String> generateAddParams(RealnameAuthAgreementTemplate realnameAuthAgreementTemplate) {
        List<String> list = new ArrayList<>();
        list.add(realnameAuthAgreementTemplate.getTemplateFileId());
        list.add(realnameAuthAgreementTemplate.getTemplateFileSign());
        list.add(realnameAuthAgreementTemplate.getFileName());
        list.add(realnameAuthAgreementTemplate.getEnable());
        list.add(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        list.add(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        return list;
    }

}
