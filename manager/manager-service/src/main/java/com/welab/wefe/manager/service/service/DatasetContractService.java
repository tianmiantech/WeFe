package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.dto.PageableApiOutput;
import com.welab.wefe.manager.service.contract.DataSetContract;
import com.welab.wefe.manager.service.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.manager.service.dto.dataset.TagsDTO;
import com.welab.wefe.manager.service.entity.DataSet;
import com.welab.wefe.manager.service.entity.DataSetForTag;
import com.welab.wefe.manager.service.entity.DataSetMemberPermission;
import com.welab.wefe.manager.service.entity.Member;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DatasetContractService extends AbstractContractService {

    @Autowired
    private MemberContractService memberContractService;
    @Autowired
    private DataSetMemberPermissionContractService dataSetMemberPermissionContractService;
    @Autowired
    private DataSetContract latestVersionDataSetContract;
    @Autowired
    private CryptoSuite cryptoSuite;

    public void upsert(DataSet dataset) throws StatusCodeWithException {
        try {
            String extJson = " ";
            if (!memberContractService.isExist(dataset.getMemberId())) {
                throw new StatusCodeWithException("成员ID不存在", StatusCode.INVALID_USER);
            }

            TransactionReceipt insertTransactionReceipt = latestVersionDataSetContract.insert(generateParams(dataset),
                    extJson);

            // 获取回执结果
            TransactionResponse insertResponse = new TransactionDecoderService(cryptoSuite)
                    .decodeReceiptWithValues(DataSetContract.ABI, DataSetContract.FUNC_INSERT, insertTransactionReceipt);

            // 交易执行失败
            if (!transactionIsSuccess(insertResponse.getValues())) {
                //如果存在则更新数据
                TransactionReceipt updateTransactionReceipt = latestVersionDataSetContract.update(generateParams(dataset),
                        extJson);

                // 获取回执结果
                TransactionResponse updateResponse = new TransactionDecoderService(cryptoSuite)
                        .decodeReceiptWithValues(DataSetContract.ABI, DataSetContract.FUNC_UPDATE, updateTransactionReceipt);
                if (!transactionIsSuccess(updateResponse.getValues())) {
                    throw new StatusCodeWithException("更新数据集信息失败", StatusCode.SYSTEM_ERROR);
                }
            }
        } catch (
                Exception e) {
            throw new StatusCodeWithException("新增数据集信息失败: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }

    public PageableApiOutput<DataSetQueryOutput> findList(int pageIndex, int pageSize, String name, String memberName, Boolean containsY, String tag, String curMemberId, String id, String memberId) throws StatusCodeWithException {
        List<DataSet> dataSetList = new ArrayList<>();
        List<Member> memberList = new ArrayList<>();

        try {
            Tuple2<BigInteger, List<String>> dataSetResult = latestVersionDataSetContract.select(
                    id == null ? "" : id,
                    memberId == null ? "" : memberId,
                    containsY == null ? BigInteger.valueOf(-1) : BigInteger.valueOf(containsY ? 1 : 0),
                    BigInteger.valueOf(-1));
            if (dataSetResult.getValue1().intValue() == 0) {
                dataSetList = dataStrListToDataSet(dataSetResult.getValue2());
            }

            memberList = memberContractService.queryAll(null);

            List<String> dataSetIdList = getDataSetIdListByMemberId(curMemberId);
            dataSetList = dataSetList.stream().filter(dataSet ->
                    (dataSet.getPublicLevel().equals(DataSetPublicLevel.Public.name()) || dataSetIdList.contains(dataSet.getId()))
                            && (StringUtil.isEmpty(name) || dataSet.getName().contains(name))
                            && (StringUtil.isEmpty(tag) || dataSet.getTags().contains(tag))).collect(Collectors.toList());

            Map<String, String> memberIdNameMap = memberList.stream().filter(member ->
                    member.getAllowOpenDataSet() == 1 && (StringUtil.isEmpty(memberName) || member.getName().contains(memberName))
            ).collect(Collectors.toMap(Member::getId, Member::getName));

            List<DataSetQueryOutput> totalList = dataSetList.stream()
                    .filter(dataSet ->
                            memberIdNameMap.containsKey(dataSet.getMemberId()))
                    .map(dataSet ->
                            new DataSetQueryOutput(dataSet, memberIdNameMap.get(dataSet.getMemberId())))
                    .collect(Collectors.toList());
            //分页
            Map<Integer, List<DataSetQueryOutput>> pageDataMap = partition(totalList, pageSize);
            //返回当前页数据
            return new PageableApiOutput<>(totalList.size(), pageSize, pageDataMap.get(pageIndex));
        } catch (Exception e) {
            throw new StatusCodeWithException("查询失败: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

    }

    public PageableApiOutput<DataSetQueryOutput> findListMgr(int pageIndex, int pageSize, String name, String memberName, Boolean containsY, String tag, String id, String memberId) throws StatusCodeWithException {

        List<DataSet> dataSetList = new ArrayList<>();
        List<Member> memberList = new ArrayList<>();

        try {
            Tuple2<BigInteger, List<String>> dataSetResult = latestVersionDataSetContract.select(
                    id == null ? "" : id,
                    memberId == null ? "" : memberId,
                    containsY == null ? BigInteger.valueOf(-1) : BigInteger.valueOf(containsY ? 1 : 0),
                    BigInteger.valueOf(-1)
            );

            if (dataSetResult.getValue1().intValue() == 0) {
                dataSetList = dataStrListToDataSet(dataSetResult.getValue2());
            }
            memberList = memberContractService.queryAll(null);

            dataSetList = dataSetList.stream().filter(dataSet ->
                    (StringUtil.isEmpty(name) || dataSet.getName().contains(name))
                            && (StringUtil.isEmpty(tag) || dataSet.getTags().contains(tag))).collect(Collectors.toList());

            Map<String, String> memberIdNameMap = memberList.stream().filter(member ->
                    member.getAllowOpenDataSet() == 1 && (StringUtil.isEmpty(memberName) || member.getName().contains(memberName))
            ).collect(Collectors.toMap(Member::getId, Member::getName));

            List<DataSetQueryOutput> totalList = dataSetList.stream()
                    .filter(dataSet ->
                            memberIdNameMap.containsKey(dataSet.getMemberId()))
                    .map(dataSet ->
                            new DataSetQueryOutput(dataSet, memberIdNameMap.get(dataSet.getMemberId())))
                    .collect(Collectors.toList());
            //分页
            Map<Integer, List<DataSetQueryOutput>> pageDataMap = partition(totalList, pageSize);
            //返回当前页数据
            return new PageableApiOutput<>(totalList.size(), pageSize, pageDataMap.get(pageIndex));
        } catch (Exception e) {
            throw new StatusCodeWithException("查询失败: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    public void deleteById(String dataSetId) throws StatusCodeWithException {
        try {
            boolean isExist = latestVersionDataSetContract.isExist(dataSetId);
            if(isExist){
                TransactionReceipt transactionReceipt = latestVersionDataSetContract.deleteByDataSetId(dataSetId);
                // 获取回执结果
                TransactionResponse deleteResponse = new TransactionDecoderService(cryptoSuite)
                        .decodeReceiptWithValues(DataSetContract.ABI, DataSetContract.FUNC_DELETEBYDATASETID, transactionReceipt);
                if (!transactionIsSuccess(deleteResponse.getValues())) {
                    throw new StatusCodeWithException("交易失败", StatusCode.SYSTEM_ERROR);
                }
            }
        } catch (Exception e) {
            throw new StatusCodeWithException("删除数据集信息失败: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * 翻页查询
     */
    public List<TagsDTO> getTagList(String tagName) throws StatusCodeWithException {
        List<DataSet> dataSetList = new ArrayList<>();
        try {
            Tuple2<BigInteger, List<String>> queryResult = latestVersionDataSetContract.selectAll();
            if (queryResult.getValue1().intValue() == 0) {
                dataSetList = dataStrListToDataSet(queryResult.getValue2());
            }
            Map<String, Long> groupMap = dataSetList.stream()
                    .filter(dataSet -> StringUtil.isEmpty(tagName) || dataSet.getTags().contains(tagName))
                    .collect(Collectors.groupingBy(DataSet::getTags, Collectors.counting()));

            List<DataSetForTag> tagList = groupMap.entrySet().stream()
                    .map(m -> new DataSetForTag(m.getKey(), m.getValue()))
                    .collect(Collectors.toList());

            Map<String, Long> map = new HashMap<>();
            // 把数据库记录的tags字段进行拆分，去重，排序
            tagList
                    .stream()
                    .map(DataSetForTag::getTags)
                    .flatMap(tag -> Arrays.stream(tag.split(",")))
                    .filter(StringUtil::isNotEmpty)
                    .filter(word -> {
                        if (StringUtil.isEmpty(tagName)) {
                            return true;
                        } else {
                            return word.contains(tagName);
                        }
                    })
                    .collect(Collectors.toList())
                    .forEach(word -> map.put(word, map.getOrDefault(word, 0L) + 1));


            List<TagsDTO> list = new ArrayList<>();
            map.forEach((word, count) -> list.add(new TagsDTO(word, count)));

            return list;
        } catch (ContractException e) {
            throw new StatusCodeWithException("查询所有数据集信息失败: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }


    }

    public DataSet findByIdMgr(String id) throws StatusCodeWithException {
        try {
            Tuple2<BigInteger, List<String>> queryResult = latestVersionDataSetContract.selectById(id);
            if (queryResult.getValue1().intValue() == 0) {
                DataSet dataSet = dataStrToDataSet(queryResult.getValue2().get(0));
                return dataSet;
            }
            return null;
        } catch (ContractException e) {
            throw new StatusCodeWithException("查询数据集详情信息失败: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }



    private List<String> getDataSetIdListByMemberId(String memberId) throws StatusCodeWithException {
        List<String> dataSetIdList = new ArrayList<>();
        if (StringUtil.isNotEmpty(memberId)) {
            List<DataSetMemberPermission> dataSetMemberPermissionList = dataSetMemberPermissionContractService.queryByMemberId(memberId);
            dataSetIdList = dataSetMemberPermissionList.stream().map(x -> x.getDataSetId()).collect(Collectors.toList());
        }
        return dataSetIdList;
    }


    /**
     * 字段字符串列表转成员对象列表
     *
     * @param dataStrList 字段字符串列表
     * @return 对象列表
     */
    public static List<DataSet> dataStrListToDataSet(List<String> dataStrList) {
        List<DataSet> datasetList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dataStrList)) {
            return datasetList;
        }

        for (String dataStr : dataStrList) {
            datasetList.add(dataStrToDataSet(dataStr));
        }

        return datasetList;
    }

    /**
     * 字段字符串转数据集对象
     */
    public static DataSet dataStrToDataSet(String dataStr) {
        if (StringUtil.isEmpty(dataStr)) {
            return null;
        }

        String[] dataStrArray = dataStr.split("\\|");
        DataSet dataset = new DataSet();
        dataset.setId(StringUtil.strTrim(dataStrArray[0]));
        dataset.setMemberId(StringUtil.strTrim(dataStrArray[1]));
        dataset.setName(StringUtil.strTrim(dataStrArray[2]));
        dataset.setContainsY(Integer.parseInt(dataStrArray[3]));
        dataset.setRowCount(Long.parseLong(StringUtil.strTrim(dataStrArray[4])));
        dataset.setColumnCount(Integer.parseInt(StringUtil.strTrim(dataStrArray[5])));
        dataset.setColumnNameList(StringUtil.strTrim(dataStrArray[6]));
        dataset.setFeatureCount(Integer.parseInt(StringUtil.strTrim(dataStrArray[7])));
        dataset.setFeatureNameList(StringUtil.strTrim(dataStrArray[8]));
        dataset.setPublicLevel(StringUtil.strTrim(dataStrArray[9]));
        dataset.setPublicMemberList(StringUtil.strTrim(dataStrArray[10]));
        if(StringUtil.isNotEmpty(dataStrArray[11].trim())){
            dataset.setUsageCountInJob(Integer.parseInt(dataStrArray[11].trim()));
        }
        if(StringUtil.isNotEmpty(dataStrArray[12].trim())){
            dataset.setUsageCountInFlow(Integer.parseInt(dataStrArray[12].trim()));
        }
        if(StringUtil.isNotEmpty(dataStrArray[13].trim())){
            dataset.setUsageCountInProject(Integer.parseInt(dataStrArray[13].trim()));
        }

        dataset.setDescription(StringUtil.strTrim(dataStrArray[14]));
        dataset.setTags(StringUtil.strTrim(dataStrArray[15]));
        dataset.setCreatedTime(DateUtil.stringToDate(StringUtil.strTrim(dataStrArray[16]), DateUtil.YYYY_MM_DD_HH_MM_SS2));
        dataset.setUpdatedTime(DateUtil.stringToDate(StringUtil.strTrim(dataStrArray[17]), DateUtil.YYYY_MM_DD_HH_MM_SS2));
        dataset.setLogTime(Long.parseLong(StringUtil.strTrim(dataStrArray[18])));
        return dataset;
    }


    private <R> Map<Integer, List<R>> partition(List<R> list, int pageSize) {
        return IntStream.iterate(0, i -> i + pageSize)
                .limit((list.size() + pageSize - 1) / pageSize)
                .boxed()
                .collect(Collectors.toMap(i -> i / pageSize, i -> list.subList(i, min(i + pageSize, list.size()))));
    }


    private List<String> generateParams(DataSet dataSet){
        List<String> list = new ArrayList<>();
        list.add(dataSet.getId());
        list.add(StringUtil.isEmptyToBlank(dataSet.getMemberId()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getName()));
        list.add(String.valueOf(dataSet.getContainsY()));
        list.add(String.valueOf(dataSet.getRowCount()));
        list.add(String.valueOf(dataSet.getColumnCount()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getColumnNameList()));
        list.add(String.valueOf(dataSet.getFeatureCount()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getFeatureNameList()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getPublicLevel()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getPublicMemberList()));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(dataSet.getUsageCountInJob())));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(dataSet.getUsageCountInFlow())));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(dataSet.getUsageCountInProject())));
        list.add(StringUtil.isEmptyToBlank(dataSet.getDescription()));
        list.add(StringUtil.isEmptyToBlank(dataSet.getTags()));
        list.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(dataSet.getCreatedTime())));
        list.add(StringUtil.isEmptyToBlank(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(dataSet.getUpdatedTime())));
        list.add(StringUtil.isEmptyToBlank(String.valueOf(System.currentTimeMillis())));
        return list;
    }
}
