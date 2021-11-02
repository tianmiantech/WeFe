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
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.manager.service.contract.MemberContract;
import com.welab.wefe.manager.service.dto.member.RealNameAuthInput;
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
 * @author aaron.li
 **/
@Service
public class MemberContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(MemberContractService.class);

    @Autowired
    private MemberContract memberContract;
    @Autowired
    private MemberMongoReop memberMongoReop;

    @Autowired
    private CryptoSuite cryptoSuite;


    /**
     *
     */
    public void update(Member member) throws StatusCodeWithException {
        try {

            LOG.info("MemberContractService upsert: {}" + member.getId());
            // Send transaction
            TransactionReceipt transactionReceipt = memberContract.updateExcludePublicKey(
                    generateParams(member),
                    JObject.toJSONString(member.getExtJson())
            );

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATEEXCLUDEPUBLICKEY, transactionReceipt);

            LOG.info("Member contract update transaction, member id: {}, receipt response: {}, values: {}", member.getId(), transactionResponse, transactionResponse.getValues());

            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("upsert failed,blockchain response error: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataNotFound(responseValues)) {
                throw new StatusCodeWithException("upsert failed，Data is not exist", StatusCode.SYSTEM_BUSY);
            }

        } catch (Exception e) {
            LOG.error("upsert member failed：", e);
            throw new StatusCodeWithException("upsert member failed", StatusCode.SYSTEM_ERROR);
        }
    }

    public void updateExtJson(String memberId,MemberExtJSON extJSON) throws StatusCodeWithException {
        try {
            JObject extJson = JObject.create(memberMongoReop.findMemberId(memberId).getExtJson());
            Field[] fields = extJSON.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                if (null != fields[i].get(extJSON)) {
                    extJson.put(fields[i].getName(), fields[i].get(extJSON));
                }
            }
            TransactionReceipt transactionReceipt = memberContract.updateExtJson(memberId,
                    extJson.toString());

            // Get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATEEXTJSON, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to updateExtJson set information: " + e, StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * Check if the member information exists
     *
     * @return true：Exists, false: not exist
     */
    public boolean isExist(String id) throws StatusCodeWithException {
        try {
            Boolean ret = memberContract.isExist(id);
            return (null != ret && ret);
        } catch (Exception e) {
            LOG.error("Check if the member information exists failed: ", e);
            throw new StatusCodeWithException("Check if the member information exists failed: ", StatusCode.SYSTEM_ERROR);
        }
    }

    private List<String> generateParams(Member member) {
        List<String> list = new ArrayList<>();
        list.add(member.getMemberId());
        list.add(member.getName());
        list.add(member.getMobile());
        list.add(member.getAllowOpenDataSet());
        list.add(member.getHidden());
        list.add(member.getFreezed());
        list.add(member.getLostContact());

        list.add(member.getEmail());
        list.add(member.getGatewayUri());
        list.add(member.getLogo());
        list.add(member.getCreatedTime());
        list.add(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(System.currentTimeMillis())));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(System.currentTimeMillis())));
        return list;
    }

}
