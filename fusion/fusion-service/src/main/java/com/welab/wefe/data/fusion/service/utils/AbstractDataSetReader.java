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

package com.welab.wefe.data.fusion.service.utils;

import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
public abstract class AbstractDataSetReader implements Closeable {
    protected List<String> header;
    protected boolean containsY;
    /**
     * Number of rows of read data
     */
    protected int readDataRows = 0;

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

        if (list.stream().distinct().count() != list.size()) {
            throw new StatusCodeWithException("The dataset contains duplicate fields. Please handle and re-upload.", StatusCode.PARAMETER_VALUE_INVALID);
        }

        list = list.stream().map(x -> "Y".equals(x) ? "y" : x).collect(Collectors.toList());

        containsY = list.contains("y");

        header = list;

        return header;
    }

    public List<String> getHeader(List<String> rowsList) throws StatusCodeWithException {
        if (header != null) {
            return header;
        }

        List<String> list = null;
        try {
            list = doGetHeader();
        } catch (Exception e) {
            throw new StatusCodeWithException("读取数据集 header 信息失败：" + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        if (list.stream().distinct().count() != list.size()) {
            throw new StatusCodeWithException("数据集包含重复的字段，请处理后重新上传。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        header = rowsList;

        return header;
    }

    /**
     * Read all rows of data
     *
     * @param dataRowConsumer Data row consumption method
     */
    public void readAll(Consumer<Map<String, Object>> dataRowConsumer) throws IOException, StatusCodeWithException {
        read(dataRowConsumer, -1, -1);
    }

    /**
     * Read data row
     *
     * @param dataRowConsumer Data row consumption method
     * @param maxReadRows     Maximum number of rows that can be read
     * @param maxReadTimeInMs Maximum read time allowed
     */
    public void read(Consumer<Map<String, Object>> dataRowConsumer, long maxReadRows, long maxReadTimeInMs) throws StatusCodeWithException {

        long start = System.currentTimeMillis();

        LinkedHashMap<String, Object> row;
        while ((row = readOneRow()) != null) {

            dataRowConsumer.accept(row);

            readDataRows++;

            // Limits the number of rows read
            if (maxReadRows > 0 && readDataRows >= maxReadRows) {
                break;
            }

            // Limit the read duration
            if (maxReadTimeInMs > 0 && System.currentTimeMillis() - start > maxReadTimeInMs) {
                break;
            }
        }
    }

    /**
     * Read all rows of data
     *
     * @param dataRowConsumer Data row consumption method
     */
    public void readAllWithSelectRow(Consumer<Map<String, Object>> dataRowConsumer, List<String> rows, int processCount) throws IOException, StatusCodeWithException {
        readWithSelectRow(dataRowConsumer, -1, -1, rows, processCount);
    }

    /**
     * Read data row
     *
     * @param dataRowConsumer Data row consumption method
     * @param maxReadRows     Maximum number of rows that can be read
     * @param maxReadTimeInMs Maximum read time allowed
     */
    public void readWithSelectRow(Consumer<Map<String, Object>> dataRowConsumer, long maxReadRows, long maxReadTimeInMs, List<String> rows, int processCount) throws StatusCodeWithException {

        long start = System.currentTimeMillis();

        LinkedHashMap<String, Object> line;
        int count = 0;
        while ((line = readOneRow()) != null & CommonThreadPool.TASK_SWITCH) {
            count++;

            if (count < processCount + 1) {
                continue;
            }

            List<Object> fields = new ArrayList<>(line.keySet());
            List<Object> values = new ArrayList<>(line.values());
            LinkedHashMap<String, Object> newLine = new LinkedHashMap<>();
            for (int i = 0; i < line.size(); i++) {
                if (rows.contains(fields.get(i))) {
                    newLine.put((String) fields.get(i), values.get(i));
                }
            }

            dataRowConsumer.accept(newLine);

            readDataRows++;


            if (maxReadRows > 0 && readDataRows >= maxReadRows) {
                break;
            }

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

    public int getReadDataRows() {
        return readDataRows;
    }

    /**
     * Get header
     * @return
     * @throws Exception
     */
    protected abstract List<String> doGetHeader() throws Exception;

    /**
     * Read data row
     */
    protected abstract LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException;
}
