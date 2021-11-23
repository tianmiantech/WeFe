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
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.api.member.RealNameAuthApi;
import com.welab.wefe.union.service.api.member.UpdateExcludeLogoApi;
import com.welab.wefe.union.service.common.BlockChainContext;
import com.welab.wefe.union.service.contract.MemberContract;
import com.welab.wefe.union.service.entity.Member;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigInteger;
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
    private MemberMongoReop memberMongoReop;
    /**
     * add member
     */
    public void add(Member member) throws StatusCodeWithException {
        try {
            String extJson = " ";
            // get contract
            MemberContract memberContract = getContract();
            // send transaction
            TransactionReceipt transactionReceipt = memberContract.insert(
                    generateParams(member, true),
                    extJson
            );

            // get receipt result
            TransactionResponse transactionResponse = BlockChainContext.getInstance().getUnionTransactionDecoder()
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_INSERT, transactionReceipt);

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


    /**
     *
     */
    public void upsert(Member member) throws StatusCodeWithException {
        try {
            String extJson = " ";
            MemberContract memberContract = getContract();

            LOG.info("MemberContractService upsert: {}" + member.getId());
            // Send transaction
            TransactionReceipt transactionReceipt = memberContract.updateExcludePublicKey(
                    generateParams(member, false),
                    extJson
            );

            // Get receipt result
            TransactionResponse transactionResponse = BlockChainContext.getInstance().getUnionTransactionDecoder()
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


    /**
     * Update member information (not including logo)
     */
    public void updateExcludeLogo(UpdateExcludeLogoApi.Input input) throws StatusCodeWithException {
        try {
            String extJson = " ";
            MemberContract memberContract = getContract();

            List<String> params = new ArrayList<>();
            params.add(input.getId());
            params.add(StringUtil.isEmptyToBlank(input.getName()));
            params.add(StringUtil.isEmptyToBlank(input.getMobile()));
            params.add(String.valueOf((null != input.getAllowOpenDataSet() && input.getAllowOpenDataSet()) ? 1 : 0));
            params.add(String.valueOf((null != input.getHidden() && input.getHidden()) ? 1 : 0));
            params.add(String.valueOf((null != input.getFreezed() && input.getFreezed()) ? 1 : 0));
            params.add(String.valueOf((null != input.getLostContact() && input.getLostContact()) ? 1 : 0));
            params.add(StringUtil.isEmptyToBlank(input.getPublicKey()));
            params.add(StringUtil.isEmptyToBlank(input.getEmail()));
            params.add(StringUtil.isEmptyToBlank(input.getGatewayUri()));
            params.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date())));
            params.add(StringUtil.isEmptyToBlank(String.valueOf(System.currentTimeMillis())));
            params.add(StringUtil.isEmptyToBlank(String.valueOf(System.currentTimeMillis())));

            LOG.info("MemberContractService updateExcludeLogo: {}" + input.getId());
            // Send transaction
            TransactionReceipt transactionReceipt = memberContract.updateExcludeLogo(params, extJson);

            TransactionResponse transactionResponse = BlockChainContext.getInstance().getUnionTransactionDecoder()
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATEEXCLUDELOGO, transactionReceipt);

            LOG.info("updateExcludeLogo update transaction , member id: {}, receipt response: {}, values: {}", input.getId(), transactionResponse, transactionResponse.getValues());

            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("updateExcludeLogo failed,blockchain response error: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataNotFound(responseValues)) {
                throw new StatusCodeWithException("updateExcludeLogo failed,Data is not exist", StatusCode.SYSTEM_BUSY);
            }
        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("updateExcludeLogo failed: ", e);
            throw new StatusCodeWithException("updateExcludeLogo failed", StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * update member lastactivitytime by id
     */
    public void updateLastActivityTimeById(String id, String lastActivityTime) throws StatusCodeWithException {
        try {
            LOG.info("MemberContractService updateLastActivityTimeById: {}" + id);
            MemberContract memberContract = getContract();
            // Send transaction
            TransactionReceipt transactionReceipt = memberContract.updateLastActivityTimeById(id, lastActivityTime);
            TransactionResponse transactionResponse = BlockChainContext.getInstance().getUnionTransactionDecoder()
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATELASTACTIVITYTIMEBYID, transactionReceipt);

            LOG.info("updateLastActivityTimeById transaction , member id: {}, receipt response: {}, values: {}", id, transactionResponse, transactionResponse.getValues());
            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("updateLastActivityTimeById failed，blockchain response error: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataNotFound(responseValues)) {
                throw new StatusCodeWithException("updateLastActivityTimeById failed，Data is not exist", StatusCode.SYSTEM_BUSY);
            }

        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("updateLastActivityTimeById failed: ", e);
            throw new StatusCodeWithException("updateLastActivityTimeById failed", StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * update member logo by id
     *
     * @param id   member ID
     * @param logo member logo
     */
    public void updateLogoById(String id, String logo) throws StatusCodeWithException {
        try {
            LOG.info("MemberContractService updateLogoById: {}" + id);
            // Send transaction
            TransactionReceipt transactionReceipt = getContract().updateLogoById(id, StringUtil.isEmptyToBlank(logo));
            TransactionResponse transactionResponse = BlockChainContext.getInstance().getUnionTransactionDecoder()
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATELOGOBYID, transactionReceipt);

            LOG.info("updateLogoById transaction , member id: {}, receipt response: {}, values: {}", id, transactionResponse, transactionResponse.getValues());

            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("updateLogoById failed，blockchain response error: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataNotFound(responseValues)) {
                throw new StatusCodeWithException("updateLogoById failed，Data is not exist", StatusCode.SYSTEM_BUSY);
            }

        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("updateLogoById failed: ", e);
            throw new StatusCodeWithException("updateLogoById failed", StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * update member publickey
     */
    public void updatePublicKey(String id, String publicKey) throws StatusCodeWithException {
        try {
            MemberContract memberContract = getContract();
            // Send transaction
            TransactionReceipt transactionReceipt = memberContract.updatePublicKey(id, publicKey);

            TransactionResponse transactionResponse = BlockChainContext.getInstance().getUnionTransactionDecoder()
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATEPUBLICKEY, transactionReceipt);

            LOG.info("Member contract updatePublicKey transaction , member id: {}, receipt response: {}", id, transactionResponse);

            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("updatePublicKey failed，blockchain response error: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataNotFound(responseValues)) {
                throw new StatusCodeWithException("updatePublicKey failed，Data is not exist", StatusCode.SYSTEM_BUSY);
            }

        } catch (Exception e) {
            LOG.error("updatePublicKey failed：", e);
            throw new StatusCodeWithException("updatePublicKey failed", StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * Check if the member information exists
     *
     * @return true：Exists, false: not exist
     */
    public boolean isExist(String id) throws StatusCodeWithException {
        try {
            MemberContract memberContract = getContract();
            Boolean ret = memberContract.isExist(id);
            return (null != ret && ret);
        } catch (Exception e) {
            LOG.error("Check if the member information exists failed: ", e);
            throw new StatusCodeWithException("Check if the member information exists failed: ", StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * @param id if it is empty, all are queried, otherwise according to the primary key
     */
    public List<Member> queryAll(String id) throws StatusCodeWithException {
        try {
            MemberContract memberContract = getContract();

            if (StringUtil.isNotEmpty(id)) {
                return dataStrListToMember(memberContract.selectById(id).getValue2());
            }

            BigInteger count = memberContract.count("", "", "", "", "");
            if (count.intValue() <= 0) {
                return new ArrayList<>();
            }
            int pageSize = 10;
            int totalPage = (count.intValue() + pageSize - 1) / pageSize;
            List<String> memberStrList = new ArrayList<>();
            for (int pageNo = 0; pageNo < totalPage; pageNo++) {
                Tuple2<BigInteger, List<String>> listTuple2 = memberContract.selectByPage("", "", "", "", "", new BigInteger(String.valueOf(pageNo * pageSize)), new BigInteger(String.valueOf(pageSize)));
                if (CollectionUtils.isNotEmpty(listTuple2.getValue2())) {
                    memberStrList.addAll(listTuple2.getValue2());
                }
            }

            return dataStrListToMember(memberStrList);
        } catch (Exception e) {
            LOG.error("queryAll member failed: ", e);
            throw new StatusCodeWithException("queryAll member failed", StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * get contract
     */
    private MemberContract getContract() throws StatusCodeWithException {
        BlockChainContext blockChainContext = BlockChainContext.getInstance();
        return blockChainContext.getLatestVersionMemberContract();
    }


    /**
     * Field string list to member object list
     */
    private List<Member> dataStrListToMember(List<String> dataStrList) {
        List<Member> memberList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dataStrList)) {
            return memberList;
        }

        for (String dataStr : dataStrList) {
            memberList.add(dataStrToMember(dataStr));
        }

        return memberList;
    }

    /**
     * Field string to member object
     *
     * @param dataStr Object attributes are divided by |
     */
    private Member dataStrToMember(String dataStr) {
        if (StringUtil.isEmpty(dataStr)) {
            return null;
        }
        String[] dataStrArray = dataStr.split("\\|");
        Member member = new Member();
        member.setId(StringUtil.strTrim(dataStrArray[0]));
        member.setName(StringUtil.strTrim(dataStrArray[1]));
        member.setMobile(StringUtil.strTrim(dataStrArray[2]));
        member.setAllowOpenDataSet(Integer.parseInt(dataStrArray[3]));
        member.setHidden(Integer.parseInt(dataStrArray[4]));
        member.setFreezed(Integer.parseInt(dataStrArray[5]));
        member.setLostContact(Integer.parseInt(dataStrArray[6]));
        member.setLogo(StringUtil.strTrim(dataStrArray[7]));
        member.setPublicKey(StringUtil.strTrim(dataStrArray[8]));
        member.setEmail(StringUtil.strTrim(dataStrArray[9]));
        member.setGatewayUri(StringUtil.strTrim(dataStrArray[10]));
        member.setCreatedTime(DateUtil.stringToDate(StringUtil.strTrim(dataStrArray[11]), DateUtil.YYYY_MM_DD_HH_MM_SS2));
        member.setUpdatedTime(DateUtil.stringToDate(StringUtil.strTrim(dataStrArray[12]), DateUtil.YYYY_MM_DD_HH_MM_SS2));
        member.setLastActivityTime(Long.parseLong(dataStrArray[13]));
        member.setLogTime(Long.parseLong(dataStrArray[14]));
        return member;
    }

    private List<String> generateParams(Member member, boolean isContainPublicKey) {
        List<String> list = new ArrayList<>();
        list.add(member.getId());
        list.add(StringUtil.isEmptyToBlank(member.getName()));
        list.add(StringUtil.isEmptyToBlank(member.getMobile()));
        list.add(String.valueOf(member.getAllowOpenDataSet()));
        list.add(String.valueOf(member.getHidden()));
        list.add(String.valueOf(member.getFreezed()));
        list.add(String.valueOf(member.getLostContact()));
        if (isContainPublicKey) {
            list.add(StringUtil.isEmptyToBlank(member.getPublicKey()));
        }
        list.add(StringUtil.isEmptyToBlank(member.getEmail()));
        list.add(StringUtil.isEmptyToBlank(member.getGatewayUri()));
        list.add(StringUtil.isEmptyToBlank(member.getLogo()));
        list.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(member.getCreatedTime())));
        list.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(member.getUpdatedTime())));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(System.currentTimeMillis())));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(System.currentTimeMillis())));
        return list;
    }


    public void updateExtJson(String memberId,MemberExtJSON extJSON) throws StatusCodeWithException {
        try {
            MemberContract memberContract = getContract();
            JObject extJson = JObject.create(memberMongoReop.findMemberId(memberId).getExtJson());
            Field[] fields = extJSON.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                if (null != fields[i].get(extJSON)) {
                    extJson.put(StringUtil.camelCaseToUnderLineCase(fields[i].getName()), fields[i].get(extJSON));
                }
            }
            TransactionReceipt transactionReceipt = memberContract.updateExtJson(memberId,
                    extJson.toString());

            // Get receipt result
            TransactionResponse transactionResponse = BlockChainContext.getInstance().getUnionTransactionDecoder()
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATEEXTJSON, transactionReceipt);

            transactionIsSuccess(transactionResponse);

        } catch (
                Exception e) {
            throw new StatusCodeWithException("Failed to updateExtJson set information: " + e, StatusCode.SYSTEM_ERROR);
        }
    }


}
