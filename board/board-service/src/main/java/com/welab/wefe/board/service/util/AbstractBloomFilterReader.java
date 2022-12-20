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

import com.welab.wefe.board.service.dto.fusion.BloomFilterColumnInputModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * bloom_filter reader
 *
 * @author jacky.jiang
 */
public abstract class AbstractBloomFilterReader implements Closeable {

    protected List<String> header;
    protected boolean containsY;
    /**
     * Number of rows of data read
     */
    protected int readDataRows = 0;
    private Map<String, BloomFilterColumnInputModel> metadataMap;

    public AbstractBloomFilterReader(List<BloomFilterColumnInputModel> metadataList) {
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
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "读取数据 header 信息失败：" + e.getMessage());
        }

        // trim column name
        list = list
                .stream()
                .map(x -> x.trim())
                .collect(Collectors.toList());

        if (list.stream().distinct().count() != list.size()) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "数据包含重复的字段，请处理后重新上传。");
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
    public void readAll(Consumer<LinkedHashMap<String, Object>> dataRowConsumer) throws Exception {
        read(dataRowConsumer, -1, -1);
    }

    /**
     * Read data row
     *
     * @param dataRowConsumer Data row consumption method
     * @param maxReadRows     Maximum number of read lines allowed
     * @param maxReadTimeInMs Maximum read time allowed
     */
    public void read(Consumer<LinkedHashMap<String, Object>> dataRowConsumer, long maxReadRows, long maxReadTimeInMs) throws Exception {

        long start = System.currentTimeMillis();

        LinkedHashMap<String, Object> row;
        while ((row = readOneRow()) != null) {

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
     * Get the total number of rows in the bloomFilter
     */
    public abstract long getTotalDataRowCount();

    /**
     * Read data row
     */
    protected abstract LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException, Exception;
}
