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
import com.welab.wefe.common.data.mongodb.entity.union.TrustCerts;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.manager.service.contract.TrustCertsContract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Service
public class TrustCertsContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(TrustCertsContractService.class);

    @Autowired
    private TrustCertsContract trustCertsContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    /**
     * add TrustCerts
     */
    public void add(TrustCerts trustCerts) throws StatusCodeWithException {
        try {
            // send transaction
            TransactionReceipt transactionReceipt = trustCertsContract.insert(
                    generateAddParams(trustCerts),
                    JObject.toJSONString(trustCerts.getExtJson())
            );

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(TrustCertsContract.ABI, TrustCertsContract.FUNC_INSERT, transactionReceipt);


            LOG.info("TrustCerts contract insert transaction, certId: {},  receipt response: {}", trustCerts.getCertId(), JObject.toJSON(transactionResponse).toString());

            transactionIsSuccess(transactionResponse);

        } catch (StatusCodeWithException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error("add TrustCerts error: ", e);
            throw new StatusCodeWithException("add TrustCerts error: ", StatusCode.SYSTEM_ERROR);
        }
    }


    public void deleteBySerialNumber(String serialNumber) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = trustCertsContract.deleteBySerialNumber(serialNumber);

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(TrustCertsContract.ABI, TrustCertsContract.FUNC_DELETEBYSERIALNUMBER, transactionReceipt);


            transactionIsSuccess(transactionResponse);
        } catch (Exception e) {
            throw new StatusCodeWithException("deleteBySerialNumber failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    public boolean isExistBySerialNumber(String serialNumber) throws StatusCodeWithException {
        try {
            return trustCertsContract.isExistBySerialNumber(serialNumber);
        } catch (Exception e) {
            throw new StatusCodeWithException("isExistBySerialNumber failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    private List<String> generateAddParams(TrustCerts trustCerts) {
        List<String> list = new ArrayList<>();
        list.add(trustCerts.getCertId());
        list.add(StringUtil.isEmptyToBlank(trustCerts.getSerialNumber()));
        list.add(StringUtil.isEmptyToBlank(trustCerts.getCertContent()));
        list.add(StringUtil.isEmptyToBlank(trustCerts.getpCertId()));
        list.add(StringUtil.isEmptyToBlank(trustCerts.getIssuerOrg()));
        list.add(StringUtil.isEmptyToBlank(trustCerts.getIssuerCn()));
        list.add(StringUtil.isEmptyToBlank(trustCerts.getSubjectOrg()));
        list.add(StringUtil.isEmptyToBlank(trustCerts.getSubjectCn()));
        list.add(StringUtil.isEmptyToBlank(trustCerts.getIsCaCert()));
        list.add(StringUtil.isEmptyToBlank(trustCerts.getIsRootCert()));
        list.add(trustCerts.getCreatedTime());
        list.add(trustCerts.getUpdatedTime());
        return list;
    }

}
