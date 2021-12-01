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

package com.welab.wefe.fusion.core.utils;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.io.excel.ExcelReader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Used to read excel - formatted data set files
 *
 * @author zane.luo
 */
public class ExcelDataSetReader extends AbstractDataSetReader {

    private ExcelReader reader;

    public ExcelDataSetReader(File file) throws IOException {
        reader = new ExcelReader(file);
    }

    @Override
    protected List<String> doGetHeader() throws Exception {
        return reader.getColumnNames(0);
    }

    @Override
    protected LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException {

        // Read data row
        List<Object> rowData = reader.getRowData(0, readDataRows + 1);

        if (rowData == null) {
            return null;
        }

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < header.size(); i++) {

            // Add the default column of the data row to NULL
            Object value = rowData.size() > i ? rowData.get(i) : null;

            map.put(header.get(i), value);
        }
        return map;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
