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
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.union.service.contract.DataSetMemberPermissionContract;
import com.welab.wefe.union.service.entity.DataSetMemberPermission;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(transactionManager = "transactionUnionManager", rollbackFor = Exception.class)
public class DataSetMemberPermissionContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetMemberPermissionContractService.class);

    @Autowired
    private CryptoSuite cryptoSuite;
    @Autowired
    private DataSetMemberPermissionContract dataSetMemberPermissionContract;
    @Autowired
    private MemberContractService memberContractService;


    public void save(String dataSetId, String publicMemberList) throws StatusCodeWithException {

        String[] memberIds = publicMemberList.split(",");

        validateMembersLocal(memberIds);
        validateMembersRemote(memberIds);

        List<DataSetMemberPermission> list = Arrays.stream(memberIds)
                .map(memberId -> new DataSetMemberPermission(dataSetId, memberId))
                .collect(Collectors.toList());

        // If there is a new permission list, delete the previous permission
        deleteByDataSetId(dataSetId);
        for (DataSetMemberPermission dataSetMemberPermission :
                list) {
            try {
                String extJson = " ";
                TransactionReceipt transactionReceipt = dataSetMemberPermissionContract.insert(dataSetMemberPermission.getId()
                        , dataSetMemberPermission.getDataSetId()
                        , dataSetMemberPermission.getMemberId()
                        , DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(dataSetMemberPermission.getCreatedTime())
                        , DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(dataSetMemberPermission.getUpdatedTime())
                        , new BigInteger(String.valueOf(System.currentTimeMillis()))
                        , extJson);
                // Get receipt result
                TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                        .decodeReceiptWithValues(DataSetMemberPermissionContract.ABI, DataSetMemberPermissionContract.FUNC_INSERT, transactionReceipt);

                // Transaction execution failed
                if (!transactionIsSuccess(transactionResponse.getValues())) {
                    throw new StatusCodeWithException("data already exists", StatusCode.SYSTEM_BUSY);
                }
            } catch (Exception e) {
                LOG.error("update data set permissions error: ", e);
                throw new StatusCodeWithException("update data set permissions error: ", StatusCode.PARAMETER_VALUE_INVALID);
            }
        }
    }

    public void deleteByDataSetId(String dataSetId) throws StatusCodeWithException {
        try {
            dataSetMemberPermissionContract.deleteByDataSetId(dataSetId);
        } catch (Exception e) {
            throw new StatusCodeWithException("deleteByDataSetId failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    private void validateMembersLocal(String[] memberIds) throws StatusCodeWithException {

        boolean hasEmptyMemberId = Arrays.stream(memberIds).anyMatch(String::isEmpty);

        if (hasEmptyMemberId) {
            throw new StatusCodeWithException("Included in the empty member ID", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    private void validateMembersRemote(String[] memberIds) throws StatusCodeWithException {
        for (String memberId : memberIds) {
            if (!memberContractService.isExist(memberId)) {
                throw new StatusCodeWithException("Wrong member ID: " + memberId, StatusCode.PARAMETER_VALUE_INVALID);
            }
        }
    }


}
