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

package com.welab.wefe.board.service.util;

import com.welab.wefe.board.service.dto.vo.data_resource.BloomFilterColumnInputModel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.io.excel.ExcelReader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Used to read data set files in excel format
 *
 * @author zane.luo
 */
public class ExcelBloomfilterReader extends AbstractBloomfilterReader {
    private final ExcelReader reader;

    public ExcelBloomfilterReader(File file) throws IOException {
        this(null, file);
    }

    public ExcelBloomfilterReader(List<BloomFilterColumnInputModel> metadataList, File file) throws IOException {
        super(metadataList);
        reader = new ExcelReader(file);
    }

    @Override
    protected List<String> doGetHeader() throws Exception {
        return reader.getColumnNames(0);
    }

    @Override
    public long getTotalDataRowCount() {
        return reader.getRowCount(0) - 1;
    }

    @Override
    protected LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException {

        // Read data row
        List<Object> row = reader.getRowData(0, readDataRows + 1);

        if (row == null) {
            return null;
        }

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < header.size(); i++) {

            // Supplement the default column of the data row to null
            Object value = row.size() > i ? row.get(i) : null;

            map.put(header.get(i), value);
        }
        return map;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
