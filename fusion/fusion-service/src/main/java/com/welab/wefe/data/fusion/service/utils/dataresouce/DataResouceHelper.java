/*
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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
package com.welab.wefe.data.fusion.service.utils.dataresouce;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.ColumnDataType;
import com.welab.wefe.data.fusion.service.database.entity.DataSetColumnOutputModel;
import com.welab.wefe.data.fusion.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.data.fusion.service.dto.entity.dataset.DataSetPreviewOutputModel;
import com.welab.wefe.data.fusion.service.manager.JdbcManager;
import com.welab.wefe.data.fusion.service.service.DataSourceService;
import com.welab.wefe.data.fusion.service.utils.AbstractDataSetReader;
import com.welab.wefe.data.fusion.service.utils.CsvDataSetReader;
import com.welab.wefe.data.fusion.service.utils.ExcelDataSetReader;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 * @date 2022/2/22
 */
public class DataResouceHelper {

    private static final Pattern MATCH_INTEGER_PATTERN = Pattern.compile("^-?\\d{1,9}$");
    private static final Pattern MATCH_LONG_PATTERN = Pattern.compile("^-?\\d{10,}$");
    private static final Pattern MATCH_DOUBLE_PATTERN = Pattern.compile("^-?\\d+\\.\\d+$");

    private static DataSourceService dataSourceService;

    static {
        dataSourceService = Launcher.getBean(DataSourceService.class);
    }

    /**
     * Parse the dataset file
     */
    public static DataSetPreviewOutputModel readFile(File file) throws IOException, StatusCodeWithException {


        DataSetPreviewOutputModel output = new DataSetPreviewOutputModel();
        Map<String, DataSetColumnOutputModel> metadata = new LinkedHashMap<>();


        // Method to consume the first row of a column
        Consumer<List<String>> headRowConsumer = row -> {

            output.getHeader().addAll(row);

            for (String name : output.getHeader()) {
                DataSetColumnOutputModel column = new DataSetColumnOutputModel();
                column.setName(name);
                metadata.put(name, column);
            }

        };

        // Data line consumer
        DataRowConsumer dataRowConsumer = new DataRowConsumer(metadata, output);


        AbstractDataSetReader reader = file.getName().endsWith("csv")
                ? new CsvDataSetReader(file)
                : new ExcelDataSetReader(file);

        try {
            // Obtain column head
            headRowConsumer.accept(reader.getHeader());
            // Read data row
            reader.read(dataRowConsumer, 10000, 25_000);
        } finally {
            reader.close();
        }

        output.setMetadataList(new ArrayList<>(metadata.values()));


        return output;
    }

    /**
     * Parse the dataset file
     */
    public static DataSetPreviewOutputModel readFile(File file, List<String> rowsList) throws IOException, StatusCodeWithException {


        DataSetPreviewOutputModel output = new DataSetPreviewOutputModel();
        Map<String, DataSetColumnOutputModel> metadata = new LinkedHashMap<>();


        output.setHeader(rowsList);

        // Method to consume the first row of a column
        Consumer<List<String>> headRowConsumer = row -> {
            for (String name : output.getHeader()) {
                DataSetColumnOutputModel column = new DataSetColumnOutputModel();
                column.setName(name);
                metadata.put(name, column);
            }
        };

        // Data line consumer
        DataRowConsumer dataRowConsumer = new DataRowConsumer(metadata, output);


        AbstractDataSetReader reader = file.getName().endsWith("csv")
                ? new CsvDataSetReader(file)
                : new ExcelDataSetReader(file);

        try {
            // Obtain column head
            headRowConsumer.accept(reader.getHeader());
            // Read data row
            reader.readWithSelectRow(dataRowConsumer, -1, -1, rowsList);
        } finally {
            reader.close();
        }

        output.setMetadataList(new ArrayList<>(metadata.values()));


        return output;
    }


    /**
     * Data line consumer
     */
    private static class DataRowConsumer implements Consumer<Map<String, Object>> {

        private Map<String, DataSetColumnOutputModel> metadata;
        private DataSetPreviewOutputModel output;

        private boolean allColumnKnowDataType = false;


        public DataRowConsumer(Map<String, DataSetColumnOutputModel> metadata, DataSetPreviewOutputModel output) {
            this.metadata = metadata;
            this.output = output;
        }

        @Override
        public void accept(Map<String, Object> x) {
            // The front end only previews 10 lines of data, and too many screens freeze.
            if (output.getRawDataList().size() < 10) {
                output.getRawDataList().add(x);
            }

            if (allColumnKnowDataType) {
                return;
            }

            // Inferred data type
            boolean hasUnkonow = true;
            for (String name : output.getHeader()) {

                DataSetColumnOutputModel column = metadata.get(name);
                if (column.getDataType() == null) {

                    Object value = x.get(name);
                    ColumnDataType dataType = inferDataType(String.valueOf(value));

                    if (dataType != null) {
                        column.setDataType(dataType);
                    } else {
                        hasUnkonow = true;
                    }
                }
            }

            if (!hasUnkonow) {
                allColumnKnowDataType = true;
            }

        }

        /**
         * Inferred data type
         */
        private ColumnDataType inferDataType(String value) {
            if (StringUtil.isEmpty(value)) {
                return null;
            }

            if (MATCH_DOUBLE_PATTERN.matcher(value).find()) {
                return ColumnDataType.Double;
            }

            if (MATCH_LONG_PATTERN.matcher(value).find()) {
                return ColumnDataType.Long;
            }

            if (MATCH_INTEGER_PATTERN.matcher(value).find()) {
                return ColumnDataType.Integer;
            }

            return ColumnDataType.String;
        }
    }


    public static DataSetPreviewOutputModel readFromDB(String dataSourceId, String sql, List<String> rowList) throws Exception {
        DataSourceMySqlModel model = dataSourceService.getDataSourceById(dataSourceId);
        if (model == null) {
            throw new StatusCodeWithException("数据不存在！", StatusCode.DATA_NOT_FOUND);
        }

        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort()
                , model.getUserName(), model.getPassword(), model.getDatabaseName());
        // The total number of rows based on the query statement

        // Gets the data set column header
        List<String> header = jdbcManager.getRowHeaders(conn, sql);
        if (header.stream().distinct().count() != header.size()) {
            throw new StatusCodeWithException("数据集包含重复的字段，请处理后再尝试上传！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        DataSetPreviewOutputModel output = new DataSetPreviewOutputModel();
        LinkedHashMap<String, DataSetColumnOutputModel> metadata = new LinkedHashMap<>();
        output.setHeader(rowList);

        for (String name : output.getHeader()) {
            DataSetColumnOutputModel column = new DataSetColumnOutputModel();
            column.setName(name);
            metadata.put(name, column);
        }

        // Data line consumer
        DataRowConsumer dataRowConsumer = new DataRowConsumer(metadata, output);

        jdbcManager.readWithFieldRow(conn, sql, dataRowConsumer, 10, rowList);


        output.setMetadataList(new ArrayList<>(metadata.values()));

        return output;
    }

    public static DataSetPreviewOutputModel readFromSourceDB(String dataSourceId, String sql) throws Exception {
        DataSourceMySqlModel model = dataSourceService.getDataSourceById(dataSourceId);
        if (model == null) {
            throw new StatusCodeWithException("数据不存在！", StatusCode.DATA_NOT_FOUND);
        }

        if (sql == null) {
            throw new StatusCodeWithException("查询出错，查询语句为空", StatusCode.PARAMETER_VALUE_INVALID);
        }

        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort()
                , model.getUserName(), model.getPassword(), model.getDatabaseName());

        // Gets the data set column header
        List<String> header = jdbcManager.getRowHeaders(conn, sql);
        if (header == null) {
            throw new StatusCodeWithException("查询出错，请检查查询语句是否正确", StatusCode.PARAMETER_VALUE_INVALID);
        }

        if (header.stream().distinct().count() != header.size()) {
            throw new StatusCodeWithException("数据集包含重复的字段，请处理并尝试重新上传！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // Convert capital Y to lowercase Y
        header = header.stream().map(x -> "Y".equals(x) ? "y" : x).collect(Collectors.toList());

        // Gets the data set column header
        DataSetPreviewOutputModel output = new DataSetPreviewOutputModel();
        LinkedHashMap<String, DataSetColumnOutputModel> metadata = new LinkedHashMap<>();
        output.setHeader(header);

        for (String name : output.getHeader()) {
            DataSetColumnOutputModel column = new DataSetColumnOutputModel();
            column.setName(name);
            metadata.put(name, column);
        }

        // Data line consumer
        DataRowConsumer dataRowConsumer = new DataRowConsumer(metadata, output);

        jdbcManager.readWithFieldRow(conn, sql, dataRowConsumer, 10, header);


        output.setMetadataList(new ArrayList<>(metadata.values()));

        return output;
    }
}
