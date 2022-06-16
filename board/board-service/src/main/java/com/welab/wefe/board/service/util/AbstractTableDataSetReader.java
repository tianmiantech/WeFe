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

package com.welab.wefe.board.service.util;

import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnInputModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.Validator;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.ColumnDataTypeInferrer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * data set reader
 *
 * @author zane.luo
 */
public abstract class AbstractTableDataSetReader implements Closeable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected List<String> header;
    protected boolean containsY;
    /**
     * Number of rows of data read
     */
    protected int readDataRows = 0;
    private Map<String, DataSetColumnInputModel> metadataMap;

    public AbstractTableDataSetReader(List<DataSetColumnInputModel> metadataList) {
        if (metadataList != null) {
            this.metadataMap = metadataList.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
        }
    }

    public List<String> getHeader() throws StatusCodeWithException {
        if (header != null) {
            return header;
        }

        List<String> list = null;
        try {
            list = doGetHeader();
        } catch (Exception e) {
            throw new StatusCodeWithException("读取数据集 header 信息失败：" + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }


        for (int i = 0; i < list.size(); i++) {
            String columnName = list.get(i);
            if (StringUtil.isEmpty(columnName)) {
                StatusCode.PARAMETER_VALUE_INVALID
                        .throwException("数据集列头中第" + (i + 1) + "列名称为空，请处理后重试。");
            }
        }

        if (list.stream().distinct().count() != list.size()) {
            throw new StatusCodeWithException("数据集包含重复的字段，请处理后重新上传。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        if (list.size() == 0) {
            throw new StatusCodeWithException("数据集首行为空", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (list.size() == 1) {
            throw new StatusCodeWithException("数据集仅一列，不支持仅有 Id 列的数据集上传。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // Convert uppercase Y to lowercase y
        list = list.stream().map(x -> "Y".equals(x) ? "y" : x).collect(Collectors.toList());

        containsY = list.contains("y");

        header = list;

        return header;
    }

    /**
     * Read all data rows
     *
     * @param dataRowConsumer Data row consumption method
     */
    public void readAll(Consumer<LinkedHashMap<String, Object>> dataRowConsumer) throws StatusCodeWithException {
        read(dataRowConsumer, -1, -1);
    }

    /**
     * Read data row
     *
     * @param dataRowConsumer Data row consumption method
     * @param maxReadRows     Maximum number of read lines allowed
     * @param maxReadTimeInMs Maximum read time allowed
     */
    public void read(Consumer<LinkedHashMap<String, Object>> dataRowConsumer, long maxReadRows, long maxReadTimeInMs) throws StatusCodeWithException {

        long start = System.currentTimeMillis();

        LinkedHashMap<String, Object> row;
        while ((row = readOneRow()) != null) {

            if (getHeader().size() != row.size()) {
                StatusCode
                        .PARAMETER_VALUE_INVALID
                        .throwException(
                                "数据集第" + readDataRows + "行有" + row.size()
                                        + "列，与列头数（" + getHeader().size()
                                        + "）不匹配，请处理后重新上传。"
                        );
            }

            checkValue(row);

            dataRowConsumer.accept(row);

            readDataRows++;

            // Limit the number of rows read
            if (maxReadRows > 0 && readDataRows >= maxReadRows) {
                break;
            }

            // Limit the duration of reading
            if (maxReadTimeInMs > 0 && System.currentTimeMillis() - start > maxReadTimeInMs) {
                break;
            }
        }
    }

    /**
     * Check whether the value of the feature matches the declared data type
     */
    private void checkValue(LinkedHashMap<String, Object> row) throws StatusCodeWithException {

        if (this.metadataMap == null || this.metadataMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : row.entrySet()) {

            // skip null value
            String value = String.valueOf(entry.getValue());
            if (ColumnDataTypeInferrer.isEmptyValue(value)) {
                continue;
            }

            DataSetColumnInputModel column = this.metadataMap.get(entry.getKey());
            if (column == null) {
                continue;
            }

            boolean isValid = false;
            switch (column.getDataType()) {
                case Long:
                    isValid = Validator.isLong(value);
                    break;
                case Double:
                    isValid = Validator.isDouble(value);
                    break;
                case Boolean:
                    isValid = Validator.isBoolean(value);
                    break;
                case Integer:
                    // 这里做一点兼容，如果小数的尾数为0，则也认为是合法的整数。
                    if (value.contains(".")) {
                        value = StringUtil.trim(value, '0', '.');
                    }
                    isValid = Validator.isInteger(value);
                    break;
                case Enum:
                case String:
                default:
                    return;
            }

            if (!isValid) {
                StatusCode.ERROR_IN_DATA_RESOURCE_ADD_FORM.throwException(
                        "数据集的特征 " + column.getName()
                                + " 声明为 " + column.getDataType()
                                + " 类型，但在 " + (readDataRows + 1)
                                // 由于有并发，所以这里的行号会不准确，必须表达为附近。
                                + " 行附近发现不满足类型的值：" + value
                );
            }
        }

    }

    public boolean isContainsY() throws StatusCodeWithException {
        if (header == null) {
            getHeader();
        }

        return containsY;
    }

    public long getReadDataRows() {
        return readDataRows;
    }


    protected abstract List<String> doGetHeader() throws Exception;

    /**
     * Get the total number of rows in the data set
     */
    public abstract long getTotalDataRowCount();

    /**
     * Read data row
     */
    protected abstract LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException;
}
