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

package com.welab.wefe.board.service.util;

import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnInputModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Used to read data set files in csv format
 * <p>
 * https://github.com/osiegmar/FastCSV
 *
 * @author zane.luo
 */
public class CsvTableDataSetReader extends AbstractTableDataSetReader {

    private final CsvParser parser;
    private long totalRowCount;
    private final File file;

    public CsvTableDataSetReader(File file) throws IOException {
        this(null, file);
    }

    public CsvTableDataSetReader(List<DataSetColumnInputModel> metadataList, File file) throws IOException {
        super(metadataList);
        this.file = file;

        CsvReader reader = new CsvReader();
        reader.setContainsHeader(false);
        reader.setSkipEmptyRows(true);
        this.parser = reader.parse(file, StandardCharsets.UTF_8);
    }

    @Override
    protected List<String> doGetHeader() throws Exception {

        CsvRow row = parser.nextRow();
        return row.getFields();
    }

    @Override
    public long getTotalDataRowCount() {
        if (totalRowCount > 0) {
            return totalRowCount;
        }

        // Get the number of file lines
        try {
            FileReader fr = new FileReader(file);
            LineNumberReader lnr = new LineNumberReader(fr);
            lnr.skip(Long.MAX_VALUE);
            totalRowCount = lnr.getLineNumber() - 1L;
            lnr.close();
            fr.close();
        } catch (IOException e) {
            return 0;
        }

        return totalRowCount;
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
                String value = "";
                if (row.getFieldCount() > i) {
                    value = row.getField(i);
                }
                map.put(header.get(i), value);
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
