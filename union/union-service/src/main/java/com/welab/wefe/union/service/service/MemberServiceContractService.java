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
import com.welab.wefe.common.data.mongodb.entity.union.MemberService;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberServiceExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberServiceMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.contract.MemberServiceContract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Service
public class MemberServiceContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(MemberServiceContractService.class);

    @Autowired
    private MemberServiceMongoReop memberServiceMongoReop;
    @Autowired
    private CryptoSuite cryptoSuite;
    @Autowired
    private MemberServiceContract memberServiceContract;

    /**
     * save member service
     */
    public void save(MemberService memberService) throws StatusCodeWithException {
        try {
            // send transaction
            TransactionReceipt transactionReceipt = memberServiceContract.insert(
                    generateAddParams(memberService),
                    JObject.toJSONString(memberService.getExtJson())
            );

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberServiceContract.ABI, MemberServiceContract.FUNC_INSERT, transactionReceipt);
            if(transactionDataIsExist(transactionResponse.getValues())){
                update(memberService);
            } else {
                transactionIsSuccess(transactionResponse);
            }
            LOG.info("MemberServiceContract save transaction, member service id: {},  receipt response: {}", memberService.getServiceId(), JObject.toJSON(transactionResponse).toString());


        } catch (StatusCodeWithException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error("save member service error: ", e);
            throw new StatusCodeWithException("save member service error: ", StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * update member service
     */
    public void update(MemberService memberService) throws StatusCodeWithException {
        try {
            LOG.info("MemberServiceContract update serviceId: {}" + memberService.getServiceId());
            // Send transaction
            TransactionReceipt transactionReceipt = memberServiceContract.update(
                    memberService.getServiceId(),
                    generateUpdateParams(memberService),
                    DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date())
            );
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberServiceContract.ABI, MemberServiceContract.FUNC_UPDATE, transactionReceipt);

            LOG.info("MemberServiceContract update transaction , member serviceId: {}, receipt response: {}, values: {}", memberService.getServiceId(), transactionResponse, transactionResponse.getValues());

            transactionIsSuccess(transactionResponse);

        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("MemberServiceContract update failed: ", e);
            throw new StatusCodeWithException("MemberServiceContract update failed", StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * Check if the member service information exists
     *
     * @return true：Exists, false: not exist
     */
    public boolean isExist(String id) throws StatusCodeWithException {
        try {
            Boolean ret = memberServiceContract.isExist(id);
            return (null != ret && ret);
        } catch (Exception e) {
            LOG.error("Check if the member service information exists failed: ", e);
            throw new StatusCodeWithException("Check if the member service information exists failed: ", StatusCode.SYSTEM_ERROR);
        }
    }


    public void updateExtJson(String memberId, MemberServiceExtJSON extJSON) throws StatusCodeWithException {
        try {
            JObject extJson = JObject.create(memberServiceMongoReop.findByServiceId(memberId).getExtJson());
            Field[] fields = extJSON.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                if (null != fields[i].get(extJSON)) {
                    extJson.put(StringUtil.camelCaseToUnderLineCase(fields[i].getName()), fields[i].get(extJSON));
                }
            }
            TransactionReceipt transactionReceipt = memberServiceContract.updateExtJson(memberId,
                    extJson.toString(), DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberServiceContract.ABI, MemberServiceContract.FUNC_UPDATEEXTJSON, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to updateExtJson set information: " + e, StatusCode.SYSTEM_ERROR);
        }
    }


    public void deleteByServiceId(String serviceId) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = memberServiceContract.deleteByServiceId(serviceId);
            // Get receipt result
            TransactionResponse deleteResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberServiceContract.ABI, MemberServiceContract.FUNC_DELETEBYSERVICEID, transactionReceipt);
            if (!transactionIsSuccess(deleteResponse.getValues())) {
                throw new StatusCodeWithException("transaction failed", StatusCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            throw new StatusCodeWithException("Failed to delete data set information: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    private List<String> generateAddParams(MemberService memberService) {
        List<String> list = new ArrayList<>();
        list.add(memberService.getServiceId());
        list.add(memberService.getMemberId());
        list.add(memberService.getName());
        list.add(memberService.getBaseUrl());
        list.add(memberService.getApiName());
        list.add(StringUtil.isEmptyToBlank(memberService.getServiceType()));
        list.add(StringUtil.isEmptyToBlank(memberService.getQueryParams()));
        list.add(memberService.getServiceStatus());
        list.add(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        list.add(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        return list;
    }

    private List<String> generateUpdateParams(MemberService memberService) {
        List<String> list = new ArrayList<>();
        list.add(memberService.getName());
        list.add(memberService.getBaseUrl());
        list.add(memberService.getApiName());
        list.add(StringUtil.isEmptyToBlank(memberService.getServiceType()));
        list.add(StringUtil.isEmptyToBlank(memberService.getQueryParams()));
        list.add(memberService.getServiceStatus());
        return list;
    }


}
