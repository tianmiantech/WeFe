package com.welab.wefe.manager.service.service;

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuxin.zhang
 */
public class AbstractContractService {


    /**
     * 表的扩展字段数量
     */
    protected static final int EXT_FIELD_COUNT = 3;

    /**
     * 初始化扩展字段数组
     *
     * @param size 数组大小
     * @param exts 扩展数据原始值
     * @return
     */
    protected List<String> initExts(int size, String... exts) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(" ");
        }

        if (null == exts) {
            return result;
        }

        for (int i = 0; i < exts.length && i < size; i++) {
            result.set(i, StringUtil.isEmptyToBlank(exts[i]));
        }

        return result;
    }

    /**
     * 交易是否执行成功
     *
     * @param responseValues 回执响应结果
     * @return true：交易成功；false：交易失败
     */
    protected boolean transactionIsSuccess(String responseValues) {
        JSONArray values = JObject.parseArray(responseValues);
        if (null == values || values.isEmpty() || values.getIntValue(0) < 0) {
            return false;
        }
        return true;
    }

    protected boolean transactionException(String responseValues) {
        JSONArray values = JObject.parseArray(responseValues);
        return null == values || values.isEmpty();
    }

    /**
     * 数据找不到
     *
     * @param responseValues 回执响应结果
     * @return true：交易成功；false：交易失败
     */
    protected boolean transactionDataNotFound(String responseValues) {
        JSONArray values = JObject.parseArray(responseValues);
        return !transactionException(responseValues) && (values.getIntValue(0) < 0);
    }

    /**
     * 数据已存在
     */
    protected boolean transactionDataIsExist(String responseValues) {
        JSONArray values = JObject.parseArray(responseValues);
        return !transactionException(responseValues) && (values.getIntValue(0) == -1);
    }

    protected boolean transactionInsertFail(String responseValues) {
        JSONArray values = JObject.parseArray(responseValues);
        return !transactionException(responseValues) && (values.getIntValue(0) == -2);
    }
}
