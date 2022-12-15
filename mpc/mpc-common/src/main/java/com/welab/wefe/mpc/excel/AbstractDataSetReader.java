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

package com.welab.wefe.mpc.excel;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author zane.luo
 */
public abstract class AbstractDataSetReader implements Closeable {
    protected List<String> header;
    /**
     * Number of rows of read data
     */
    protected int readDataRows = 0;

    public List<String> getHeader() throws Exception {
        if (header != null) {
            return header;
        }
        List<String> list = null;
        try {
            list = doGetHeader();
        } catch (Exception e) {
            throw new Exception("读取数据集 header 信息失败：" + e.getMessage());
        }
        header = list;
        return header;
    }

    /**
     * Read all rows of data
     *
     * @param dataRowConsumer Data row consumption method
     */
    public void readAll(Consumer<Map<String, Object>> dataRowConsumer) throws Exception {
        read(dataRowConsumer, -1, -1);
    }

    /**
     * Read data row
     *
     * @param dataRowConsumer Data row consumption method
     * @param maxReadRows     Maximum number of rows that can be read
     * @param maxReadTimeInMs Maximum read time allowed
     */
    public void read(Consumer<Map<String, Object>> dataRowConsumer, long maxReadRows, long maxReadTimeInMs)
            throws Exception {
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

    public int getReadDataRows() {
        return readDataRows;
    }

    /**
     * Get header
     * 
     * @return
     * @throws Exception
     */
    protected abstract List<String> doGetHeader() throws Exception;

    /**
     * Read data row
     */
    protected abstract LinkedHashMap<String, Object> readOneRow() throws Exception;
}
