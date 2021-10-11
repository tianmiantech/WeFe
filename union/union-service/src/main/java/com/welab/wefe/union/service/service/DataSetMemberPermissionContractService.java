/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.common.BlockChainContext;
import com.welab.wefe.union.service.contract.DataSetMemberPermissionContract;
import com.welab.wefe.union.service.entity.DataSetMemberPermission;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DataSetMemberPermissionContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetMemberPermissionContractService.class);


    @Autowired
    private MemberContractService memberContractService;

    private BlockChainContext blockChainContext = BlockChainContext.getInstance();

    public List<DataSetMemberPermission> queryByMemberId(String memberId) throws StatusCodeWithException {

        try {
            DataSetMemberPermissionContract contract = blockChainContext.getUnionDataSetMemberPermissionContract();
            Tuple2<BigInteger, List<String>> dataSetMemberPermissionResult = contract.selectByMemberId(memberId);
            List<DataSetMemberPermission> dataSetMemberPermissionList = dataStrListToDataSetMemberPermission(dataSetMemberPermissionResult.getValue2());
            return dataSetMemberPermissionList;
        } catch (ContractException e) {
            throw new StatusCodeWithException("Failed to query data set permission information by memberId", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

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
                DataSetMemberPermissionContract contract = blockChainContext.getUnionDataSetMemberPermissionContract();
                TransactionReceipt transactionReceipt = contract.insert(dataSetMemberPermission.getId()
                        , dataSetMemberPermission.getDataSetId()
                        , dataSetMemberPermission.getMemberId()
                        , DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(dataSetMemberPermission.getCreatedTime())
                        , DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(dataSetMemberPermission.getUpdatedTime())
                        , new BigInteger(String.valueOf(System.currentTimeMillis()))
                        , extJson);
                // Get receipt result
                TransactionResponse transactionResponse = BlockChainContext.getInstance().getUnionTransactionDecoder()
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
            blockChainContext.getUnionDataSetMemberPermissionContract().deleteByDataSetId(dataSetId);
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


    /**
     * Field list to member object list
     *
     * @param dataStrList Field string list
     * @return
     */
    private List<DataSetMemberPermission> dataStrListToDataSetMemberPermission(List<String> dataStrList) {
        List<DataSetMemberPermission> dataSetMemberPermissionList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dataStrList)) {
            return dataSetMemberPermissionList;
        }

        for (String dataStr : dataStrList) {
            dataSetMemberPermissionList.add(dataStrToDataSetMemberPermission(dataStr));
        }

        return dataSetMemberPermissionList;
    }

    /**
     * Field string to data set object
     */
    private DataSetMemberPermission dataStrToDataSetMemberPermission(String dataStr) {
        if (StringUtil.isEmpty(dataStr)) {
            return null;
        }
        String[] dataStrArray = dataStr.split("\\|");
        DataSetMemberPermission dataset = new DataSetMemberPermission();
        dataset.setId(StringUtil.strTrim(dataStrArray[0]));
        dataset.setDataSetId(StringUtil.strTrim(dataStrArray[1]));
        dataset.setMemberId(StringUtil.strTrim(dataStrArray[2]));
        dataset.setCreatedTime(DateUtil.stringToDate(StringUtil.strTrim(dataStrArray[3]), DateUtil.YYYY_MM_DD_HH_MM_SS2));
        dataset.setUpdatedTime(DateUtil.stringToDate(StringUtil.strTrim(dataStrArray[4]), DateUtil.YYYY_MM_DD_HH_MM_SS2));
        return dataset;
    }

}
