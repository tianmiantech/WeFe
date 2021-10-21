package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.DataSetDefaultTag;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.manager.service.contract.DataSetDefaultTagContract;
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
 * @author aaron.li
 * @date 2020/12/16
 **/
@Service
public class DatSetDefaultTagContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(DatSetDefaultTagContractService.class);

    @Autowired
    private DataSetDefaultTagContract dataSetDefaultTagContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    /**
     * add member
     */
    public void add(DataSetDefaultTag member) throws StatusCodeWithException {
        try {
            // send transaction
            TransactionReceipt transactionReceipt = dataSetDefaultTagContract.insert(
                    generateParams(member, true),
                    JObject.toJSONString(member.getExtJson())
            );

            // get receipt result
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(DataSetDefaultTagContract.ABI, DataSetDefaultTagContract.FUNC_INSERT, transactionReceipt);


            LOG.info("Member contract insert transaction, member id: {},  receipt response: {}", member.getId(), JObject.toJSON(transactionResponse).toString());

            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("Failed to synchronize information，blockchain response error: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataIsExist(responseValues)) {
                throw new StatusCodeWithException("Member already exists", StatusCode.SYSTEM_BUSY);
            }
            if (transactionInsertFail(responseValues)) {
                throw new StatusCodeWithException("Member information failed", StatusCode.SYSTEM_BUSY);
            }

        } catch (StatusCodeWithException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error("add member error: ", e);
            throw new StatusCodeWithException("add member error: ", StatusCode.SYSTEM_ERROR);
        }
    }


    public void deleteByTagId(String tagId) throws StatusCodeWithException {
        try {
            dataSetDefaultTagContract.deleteByTagId(tagId);
        } catch (Exception e) {
            throw new StatusCodeWithException("deleteByDataSetId failed: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }


    private List<String> generateParams(DataSetDefaultTag dataSetDefaultTag, boolean isContainPublicKey) {
        List<String> list = new ArrayList<>();
        list.add(dataSetDefaultTag.getTagId());
        list.add(dataSetDefaultTag.getTagName());

        list.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date())));
        list.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date())));
        return list;
    }

}
