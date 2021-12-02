package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.manager.service.contract.UnionNodeContract;
import com.welab.wefe.manager.service.dto.union.UnionNodeEnableInput;
import com.welab.wefe.manager.service.dto.union.UnionNodeUpdateInput;
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
public class UnionNodeContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(UnionNodeContractService.class);

    @Autowired
    private UnionNodeContract unionNodeContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    /**
     * add UnionNode
     */
    public void add(UnionNode unionNode) throws StatusCodeWithException {
        try {
            // send transaction
            TransactionReceipt transactionReceipt = unionNodeContract.insert(
                    generateAddParams(unionNode),
                    JObject.toJSONString(unionNode.getExtJson())
            );

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(UnionNodeContract.ABI, UnionNodeContract.FUNC_INSERT, transactionReceipt);


            LOG.info("UnionNode contract insert transaction, unionBaseUrl: {},  receipt response: {}", unionNode.getUnionBaseUrl(), JObject.toJSON(transactionResponse).toString());

            transactionIsSuccess(transactionResponse);

        } catch (StatusCodeWithException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error("add UnionNode error: ", e);
            throw new StatusCodeWithException("add UnionNode error: ", StatusCode.SYSTEM_ERROR);
        }
    }


    public void update(UnionNodeUpdateInput input) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = unionNodeContract.update(
                    input.getUnionNodeId(),
                    generateUpdateParams(input)
            );


            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(UnionNodeContract.ABI, UnionNodeContract.FUNC_UPDATE, transactionReceipt);


            transactionIsSuccess(transactionResponse);
        } catch (Exception e) {
            throw new StatusCodeWithException("update UnionNode failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    public void enable(UnionNodeEnableInput input) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = unionNodeContract.updateEnable(
                    input.getUnionNodeId(),
                    String.valueOf(input.getEnable() ? 1 : 0),
                    toStringYYYY_MM_DD_HH_MM_SS2(new Date())
            );


            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(UnionNodeContract.ABI, UnionNodeContract.FUNC_UPDATEENABLE, transactionReceipt);


            transactionIsSuccess(transactionResponse);
        } catch (Exception e) {
            throw new StatusCodeWithException("update UnionNode failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    public void deleteByUnionNodeId(String unionNodeId) throws StatusCodeWithException {
        try {
            TransactionReceipt transactionReceipt = unionNodeContract.deleteByUnionNodeId(unionNodeId);


            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(UnionNodeContract.ABI, UnionNodeContract.FUNC_DELETEBYUNIONNODEID, transactionReceipt);


            transactionIsSuccess(transactionResponse);
        } catch (Exception e) {
            throw new StatusCodeWithException("deleteByUnionNodeId failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    private List<String> generateAddParams(UnionNode unionNode) {
        List<String> list = new ArrayList<>();
        list.add(unionNode.getUnionNodeId());
        list.add(unionNode.getUnionBaseUrl());
        list.add(unionNode.getOrganizationName());
        list.add(unionNode.getEnable());
        list.add(unionNode.getCreatedTime());
        list.add(unionNode.getUpdatedTime());

        return list;
    }

    private List<String> generateUpdateParams(UnionNodeUpdateInput input) {
        List<String> list = new ArrayList<>();
        list.add(input.getUnionBaseUrl());
        list.add(input.getOrganizationName());
        list.add(toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        return list;
    }

}
