package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.manager.service.contract.DataSetContract;
import com.welab.wefe.manager.service.contract.DataSetMemberPermissionContract;
import com.welab.wefe.manager.service.entity.DataSetMemberPermission;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
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

    @Autowired
    private DataSetMemberPermissionContract latestVersionDataSetMemberPermissionContract;

    @Autowired
    private CryptoSuite cryptoSuite;

    public List<DataSetMemberPermission> queryByMemberId(String memberId) throws StatusCodeWithException {

        try {
            Tuple2<BigInteger, List<String>> dataSetMemberPermissionResult = latestVersionDataSetMemberPermissionContract.selectByMemberId(memberId);
            List<DataSetMemberPermission> dataSetMemberPermissionList = dataStrListToDataSetMemberPermission(dataSetMemberPermissionResult.getValue2());
            return dataSetMemberPermissionList;
        } catch (ContractException e) {
            throw new StatusCodeWithException("根据memberId查询数据集权限信息失败", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    public void save(String dataSetId, String publicMemberList) throws StatusCodeWithException {

        String[] memberIds = publicMemberList.split(",");

        validateMembersLocal(memberIds);
        validateMembersRemote(memberIds);

        List<DataSetMemberPermission> list = Arrays.stream(memberIds)
                .map(memberId -> new DataSetMemberPermission(dataSetId, memberId))
                .collect(Collectors.toList());

        // 如果有新的权限列表，那就删除之前的权限
        deleteByDataSetId(dataSetId);
        for (DataSetMemberPermission dataSetMemberPermission :
                list) {
            try {
                String extJson = " ";
                TransactionReceipt transactionReceipt = latestVersionDataSetMemberPermissionContract.insert(dataSetMemberPermission.getId()
                        , dataSetMemberPermission.getDataSetId()
                        , dataSetMemberPermission.getMemberId()
                        , DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(dataSetMemberPermission.getCreatedTime())
                        , DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(dataSetMemberPermission.getUpdatedTime())
                        , new BigInteger(String.valueOf(System.currentTimeMillis()))
                        , extJson);
                // 获取回执结果
                TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                        .decodeReceiptWithValues(DataSetMemberPermissionContract.ABI, DataSetMemberPermissionContract.FUNC_INSERT, transactionReceipt);

                // 交易执行失败
                if (!transactionIsSuccess(transactionResponse.getValues())) {
                    throw new StatusCodeWithException("该数据已存在", StatusCode.SYSTEM_BUSY);
                }
            } catch (Exception e) {
                LOG.error("更新数据集权限失败: ", e);
                throw new StatusCodeWithException("更新数据集权限失败: " + e.getMessage(), StatusCode.PARAMETER_VALUE_INVALID);
            }
        }
    }

    public void deleteByDataSetId(String dataSetId) throws StatusCodeWithException {
        try {
            latestVersionDataSetMemberPermissionContract.deleteByDataSetId(dataSetId);
        } catch (Exception e) {
            throw new StatusCodeWithException("删除数据集权限信息失败: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    private void validateMembersLocal(String[] memberIds) throws StatusCodeWithException {

        boolean hasEmptyMemberId = Arrays.stream(memberIds).anyMatch(String::isEmpty);

        if (hasEmptyMemberId) {
            throw new StatusCodeWithException("存在空的成员ID", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    private void validateMembersRemote(String[] memberIds) throws StatusCodeWithException {

        for (String memberId : memberIds) {
            if (!memberContractService.isExist(memberId)) {
                throw new StatusCodeWithException("存在错误的成员ID : " + memberId, StatusCode.PARAMETER_VALUE_INVALID);
            }
        }
    }


    /**
     * 字段字符串列表转成员对象列表
     *
     * @param dataStrList 字段字符串列表
     * @return 对象列表
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
     * 字段字符串转数据集对象
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
