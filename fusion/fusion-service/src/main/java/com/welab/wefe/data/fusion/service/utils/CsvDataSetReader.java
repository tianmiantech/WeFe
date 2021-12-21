/*
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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Used to read data set files in CSV form
 * <p>
 * Using the component：
 * https://github.com/osiegmar/FastCSV
 *
 * @author zane.luo
 */
public class CsvDataSetReader extends AbstractDataSetReader {

    private CsvReader reader = new CsvReader();
    private CsvParser parser;

    public CsvDataSetReader(File file) throws IOException {
        this.reader.setContainsHeader(false);
        this.reader.setSkipEmptyRows(true);

        this.parser = reader.parse(file, StandardCharsets.UTF_8);
    }

    @Override
    protected List<String> doGetHeader() throws Exception {

        CsvRow row = parser.nextRow();
        return row.getFields();
    }

    @Override
    protected LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException {

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        try {
            CsvRow row = parser.nextRow();
            if (row == null) {
                return null;
            }

            for (int i = 0; i < header.size(); i++) {
                map.put(header.get(i), row.getField(i));
            }

        } catch (IOException e) {
            throw new StatusCodeWithException("读取数据集中的数据行失败：" + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return map;
    }


    @Override
    public void close() throws IOException {
        parser.close();
    }
}
