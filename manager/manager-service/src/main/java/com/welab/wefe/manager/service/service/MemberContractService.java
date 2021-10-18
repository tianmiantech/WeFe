package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.manager.service.api.member.UpdateExcludeLogoApi;
import com.welab.wefe.manager.service.contract.MemberContract;
import com.welab.wefe.manager.service.dto.PagingOutput;
import com.welab.wefe.manager.service.dto.member.MemberQueryOutput;
import com.welab.wefe.manager.service.entity.Member;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 成员合约服务层
 * <p>
 * TODO 该业务层暂不支持模糊查询，待完善中..
 *
 * @author aaron.li
 * @Date 2020/12/16
 **/
@Service
public class MemberContractService extends AbstractContractService {
    private static final Logger LOG = LoggerFactory.getLogger(MemberContractService.class);

    @Autowired
    private MemberContract latestVersionMemberContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    /**
     * 新增成员
     *
     * @param member 成员信息
     * @throws StatusCodeWithException
     */
    public void add(Member member) throws StatusCodeWithException {
        try {
            String extJson = " ";
            // 获取合约
            MemberContract memberContract = latestVersionMemberContract;
            // 发送交易
            TransactionReceipt transactionReceipt = memberContract.insert(
                    generateParams(member, true),
                    extJson
            );

            // 获取回执结果
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_INSERT, transactionReceipt);

            LOG.info("Member contract insert transaction, member id: {},  receipt response: {}", member.getId(), JObject.toJSON(transactionResponse).toString());

            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("同步信息失败，区块链响应异常: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataIsExist(responseValues)) {
                throw new StatusCodeWithException("成员信息已存在，无需同步。", StatusCode.SYSTEM_BUSY);
            }
            if (transactionInsertFail(responseValues)) {
                throw new StatusCodeWithException("成员信息失败", StatusCode.SYSTEM_BUSY);
            }

        } catch (StatusCodeWithException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error("新增成员失败：", e);
            throw new StatusCodeWithException("新增成员失败", StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * 更新
     *
     * @param member 成员信息
     * @throws StatusCodeWithException
     */
    public void upsert(Member member) throws StatusCodeWithException {
        try {
            String extJson = " ";
            // 获取合约
            MemberContract memberContract = latestVersionMemberContract;

            LOG.info("MemberContractService upsert: {}" + member.getId());
            // 发送交易
            TransactionReceipt transactionReceipt = memberContract.updateExcludePublicKey(
                    generateParams(member, false),
                    extJson
            );

            // 获取回执结果
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATEEXCLUDEPUBLICKEY, transactionReceipt);

            LOG.info("Member contract update transaction , member id: {}, receipt response: {}, values: {}", member.getId(), transactionResponse, transactionResponse.getValues());

            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("更新失败，区块链响应异常: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataNotFound(responseValues)) {
                throw new StatusCodeWithException("更新失败，数据不存在", StatusCode.SYSTEM_BUSY);
            }

        } catch (Exception e) {
            LOG.error("修改成员失败：", e);
            throw new StatusCodeWithException("修改成员失败", StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * 更新成员信息（不包括logo）
     */
    public void updateExcludeLogo(UpdateExcludeLogoApi.Input input) throws StatusCodeWithException {
        try {
            String extJson = " ";
            // 获取合约
            MemberContract memberContract = latestVersionMemberContract;

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
            TransactionReceipt transactionReceipt = memberContract.updateExcludeLogo(params, extJson);

            // 获取回执结果
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATEEXCLUDELOGO, transactionReceipt);

            LOG.info("update exclude logo update transaction , member id: {}, receipt response: {}, values: {}", input.getId(), transactionResponse, transactionResponse.getValues());

            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("更新失败，区块链响应异常: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataNotFound(responseValues)) {
                throw new StatusCodeWithException("更新失败，数据不存在", StatusCode.SYSTEM_BUSY);
            }
        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新成员信息（不包括logo）失败：", e);
            throw new StatusCodeWithException("更新失败", StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * 更新活跃时间
     *
     * @param id               成员ID
     * @param lastActivityTime 活跃时间
     * @throws StatusCodeWithException
     */
    public void updateLastActivityTimeById(String id, long lastActivityTime) throws StatusCodeWithException {
        try {
            LOG.info("MemberContractService updateLastActivityTimeById: {}" + id);
            MemberContract memberContract = latestVersionMemberContract;
            TransactionReceipt transactionReceipt = memberContract.updateLastActivityTimeById(id, String.valueOf(lastActivityTime));
            // 获取回执结果
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATELASTACTIVITYTIMEBYID, transactionReceipt);

            LOG.info("updateLastActivityTimeById transaction , member id: {}, receipt response: {}, values: {}", id, transactionResponse, transactionResponse.getValues());
            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("更新失败，区块链响应异常: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataNotFound(responseValues)) {
                throw new StatusCodeWithException("更新失败，数据不存在", StatusCode.SYSTEM_BUSY);
            }

        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新活跃时间失败：", e);
            throw new StatusCodeWithException("更新活跃时间失败", StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * 更新logo
     *
     * @param id   成员ID
     * @param logo 活跃时间
     */
    public void updateLogoById(String id, String logo) throws StatusCodeWithException {
        try {
            LOG.info("MemberContractService updateLogoById: {}" + id);
            TransactionReceipt transactionReceipt = latestVersionMemberContract.updateLogoById(id, StringUtil.isEmptyToBlank(logo));
            // 获取回执结果
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATELOGOBYID, transactionReceipt);

            LOG.info("updateLogoById transaction , member id: {}, receipt response: {}, values: {}", id, transactionResponse, transactionResponse.getValues());

            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("更新失败，区块链响应异常: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataNotFound(responseValues)) {
                throw new StatusCodeWithException("更新失败，数据不存在", StatusCode.SYSTEM_BUSY);
            }

        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新logo失败：", e);
            throw new StatusCodeWithException("更新logo失败", StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * 更新成员公钥
     *
     * @throws StatusCodeWithException
     */
    public void updatePublicKey(String id, String publicKey) throws StatusCodeWithException {
        try {
            // 获取合约
            MemberContract memberContract = latestVersionMemberContract;

            // 发送交易
            TransactionReceipt transactionReceipt = memberContract.updatePublicKey(id, publicKey);

            // 获取回执结果
            TransactionResponse transactionResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(MemberContract.ABI, MemberContract.FUNC_UPDATEPUBLICKEY, transactionReceipt);

            LOG.info("Member contract updatePublicKey transaction , member id: {}, receipt response: {}", id, transactionResponse);

            String responseValues = transactionResponse.getValues();
            if (transactionException(responseValues)) {
                throw new StatusCodeWithException("更新失败，区块链响应异常: " + transactionResponse.getReturnMessage(), StatusCode.SYSTEM_BUSY);
            }
            if (transactionDataNotFound(responseValues)) {
                throw new StatusCodeWithException("更新失败，数据不存在", StatusCode.SYSTEM_BUSY);
            }

        } catch (Exception e) {
            LOG.error("更新成员公钥信息失败：", e);
            throw new StatusCodeWithException("更新成员公钥信息失败", StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * 分页查询
     */
    public PagingOutput<MemberQueryOutput> query(int pageIndex, int pageSize, String id, String name, boolean hidden, boolean freezed, boolean lostContact) throws StatusCodeWithException {
        try {
            // 获取合约
            MemberContract memberContract = latestVersionMemberContract;
            pageIndex = (pageIndex < 0 ? 0 : pageIndex);
            pageSize = (pageSize <= 0 ? 10 : pageSize);
            // 合约不支持传null查询
            id = (StringUtil.isEmpty(id) ? "" : id);
            name = (StringUtil.isEmpty(name) ? "" : name);


            String contractParamHidden = hidden ? "1" : "0";
            String contractParamFreezed = freezed ? "1" : "0";
            String contractParamLostContact = lostContact ? "1" : "0";
            // 查询列表
            Tuple2<BigInteger, List<String>> listTuple2 = memberContract.selectByPage(
                    id,
                    name,
                    contractParamHidden,
                    contractParamFreezed,
                    contractParamLostContact,
                    new BigInteger(String.valueOf(pageIndex * pageSize)),
                    new BigInteger(String.valueOf(pageSize))
            );
            // 查询总页面数
            BigInteger count = memberContract.count(
                    id,
                    name,
                    contractParamHidden,
                    contractParamFreezed,
                    contractParamLostContact
            );

            return new PagingOutput<>(count.longValue(), memberToOutputModel(dataStrListToMember(listTuple2.getValue2())));
        } catch (Exception e) {
            LOG.error("分页查询成员信息失败：", e);
            throw new StatusCodeWithException("分页查询成员信息失败", StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * 根据Id查询信息是否存在
     *
     * @param id 成员Id
     * @return true：存在，false：不存在
     * @throws StatusCodeWithException
     */
    public boolean isExist(String id) throws StatusCodeWithException {
        try {
            // 获取合约
            MemberContract memberContract = latestVersionMemberContract;
            Boolean ret = memberContract.isExist(id);
            return (null != ret && ret);
        } catch (Exception e) {
            LOG.error("根据Id判断成员是否存在失败：", e);
            throw new StatusCodeWithException("判断成员是否存在失败", StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * 查询所有
     *
     * @param id 主键ID，如果为空，则查询所有，否则根据主键去查
     * @return
     */
    public List<Member> queryAll(String id) throws StatusCodeWithException {
        try {
            // 获取合约
            MemberContract memberContract = latestVersionMemberContract;

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
            LOG.error("查询所有成员失败：", e);
            throw new StatusCodeWithException("查询所有成员失败", StatusCode.SYSTEM_ERROR);
        }
    }


    /**
     * Member转输出模型
     */
    private List<MemberQueryOutput> memberToOutputModel(List<Member> memberList) {
        List<MemberQueryOutput> memberQueryOutputList = new ArrayList<>();

        if (CollectionUtils.isEmpty(memberList)) {
            return memberQueryOutputList;
        }

        MemberQueryOutput memberQueryOutput = null;
        for (Member member : memberList) {
            memberQueryOutput = new MemberQueryOutput();
            memberQueryOutput.setId(member.getId());
            memberQueryOutput.setName(member.getName());
            memberQueryOutput.setMobile(member.getMobile());
            memberQueryOutput.setEmail(member.getEmail());
            memberQueryOutput.setAllowOpenDataSet(member.getAllowOpenDataSet());
            memberQueryOutput.setGatewayUri(member.getGatewayUri());
            memberQueryOutput.setPublicKey(member.getPublicKey());
            memberQueryOutput.setCreatedTime(member.getCreatedTime());
            memberQueryOutput.setUpdatedTime(member.getUpdatedTime());
            memberQueryOutput.setHidden(member.getHidden());
            memberQueryOutput.setFreezed(member.getFreezed());
            memberQueryOutput.setLostContact(member.getLostContact());
            memberQueryOutput.setLogo(member.getLogo());
            memberQueryOutput.setLogTime(member.getLogTime());
            memberQueryOutput.setLastActivityTime(member.getLastActivityTime());
            memberQueryOutputList.add(memberQueryOutput);

        }

        return memberQueryOutputList;
    }


    /**
     * 字段字符串列表转成员对象列表
     *
     * @param dataStrList 字段字符串列表
     * @return 对象列表
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
     * 字段字符串转成员对象
     *
     * @param dataStr 对象属性以|分割
     * @return 成员对象
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

}
