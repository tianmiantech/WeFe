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

package com.welab.wefe.board.service.api.data_resource.bloom_filter;

import com.welab.wefe.board.service.constant.BloomfilterAddMethod;
import com.welab.wefe.board.service.constant.DataSetAddMethod;
import com.welab.wefe.board.service.database.entity.DataSourceMysqlModel;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnOutputModel;
import com.welab.wefe.board.service.service.data_resource.bloom_filter.BloomFilterService;
import com.welab.wefe.board.service.util.AbstractTableDataSetReader;
import com.welab.wefe.board.service.util.CsvTableDataSetReader;
import com.welab.wefe.board.service.util.ExcelTableDataSetReader;
import com.welab.wefe.board.service.util.JdbcManager;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.ListUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.ColumnDataTypeInferrer;
import com.welab.wefe.common.wefe.enums.ColumnDataType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
 * @author Jacky.jiang
 */
@Api(path = "bloom_filter/preview", name = "preview bloom_filter rows")
public class BloomFilterPreviewApi extends AbstractApi<BloomFilterPreviewApi.Input, BloomFilterPreviewApi.Output> {

    private static final Pattern MATCH_INTEGER_PATTERN = Pattern.compile("^-?\\d{1,9}$");
    private static final Pattern MATCH_LONG_PATTERN = Pattern.compile("^-?\\d{10,}$");
    private static final Pattern MATCH_DOUBLE_PATTERN = Pattern.compile("^-?\\d+\\.\\d+$");

    @Autowired
    BloomFilterService bloomfilterService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        Output output = new Output();
        // Read data from the database for preview
        if (BloomfilterAddMethod.Database.equals(input.getBloomfilterAddMethod())) {
            // Test whether SQL can be queried normally
            boolean result = bloomfilterService.testSqlQuery(input.getDataSourceId(), input.getSql());
            if (result) {
                output = readFromDatabase(input.getDataSourceId(), input.getSql());
            }
        } else {
            String filename = input.getFilename();
            File file = bloomfilterService.getBloomfilterFile(input.getBloomfilterAddMethod(), filename);
            try {
                output = readFile(file);
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
            }
        }

        //generateMetadata(output);

        return success(output);
    }

    /**
     * Parse the dataset file
     */
    private Output readFile(File file) throws IOException, StatusCodeWithException {


        Output output = new Output();
        LinkedHashMap<String, DataSetColumnOutputModel> metadata = new LinkedHashMap<>();


        // How to consume the first row of a column
        Consumer<List<String>> headRowConsumer = row -> {

            output.header.addAll(row);

            for (String name : output.header) {
                DataSetColumnOutputModel column = new DataSetColumnOutputModel();
                column.setName(name);
                metadata.put(name, column);
            }

        };

        // Data line consumer
        DataRowConsumer dataRowConsumer = new DataRowConsumer(metadata, output);


        try (
                AbstractTableDataSetReader reader = file.getName().endsWith("csv")
                        ? new CsvTableDataSetReader(file)
                        : new ExcelTableDataSetReader(file)
        ) {
            // Get column header
            headRowConsumer.accept(reader.getHeader());
            // Read data row
            reader.read(dataRowConsumer, 10000, 10_000);
        }

        output.setMetadataList(new ArrayList<>(metadata.values()));


        return output;
    }

    /**
     * Data line consumer
     */
    private static class DataRowConsumer implements Consumer<LinkedHashMap<String, Object>> {

        private final LinkedHashMap<String, DataSetColumnOutputModel> metadata;
        private final Output output;

        private boolean allColumnKnowDataType = false;


        public DataRowConsumer(LinkedHashMap<String, DataSetColumnOutputModel> metadata, Output output) {
            this.metadata = metadata;
            this.output = output;
        }

        @Override
        public void accept(LinkedHashMap<String, Object> x) {
            // The front end only previews 10 rows of data, too many interfaces will freeze.
            if (output.rawDataList.size() < 10) {
                output.rawDataList.add(x);
            }

            if (allColumnKnowDataType) {
                return;
            }

            // Infer data type
            boolean hasUnkonow = true;
            for (String name : output.header) {

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
         * Infer data type
         */
        private ColumnDataType inferDataType(String value) {
            if (ColumnDataTypeInferrer.isEmptyValue(value)) {
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

    private Output readFromDatabase(String dataSourceId, String sql) throws StatusCodeWithException {
        DataSourceMysqlModel model = bloomfilterService.getDataSourceById(dataSourceId);
        if (model == null) {
            throw new StatusCodeWithException("dataSourceId在数据库不存在", StatusCode.DATA_NOT_FOUND);
        }

        Connection conn = JdbcManager.getConnection(
                model.getDatabaseType(),
                model.getHost(),
                model.getPort(),
                model.getUserName(),
                model.getPassword(),
                model.getDatabaseName()
        );

        // Get the column header of the data set
        List<String> header = JdbcManager.getRowHeaders(conn, sql);
        if (header.stream().distinct().count() != header.size()) {
            throw new StatusCodeWithException("数据集包含重复的字段，请处理后重新上传。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // Convert uppercase Y to lowercase y
        header = header.stream().map(x -> "Y".equals(x) ? "y" : x).collect(Collectors.toList());

        boolean containsY = header.contains("y");
        int yIndex = header.indexOf("y");

        // If there is a y column, move y to the second column (the first column is the primary key).
        if (containsY) {
            ListUtil.moveElement(header, yIndex, 1);
        }

        Output output = new Output();
        LinkedHashMap<String, DataSetColumnOutputModel> metadata = new LinkedHashMap<>();
        output.setHeader(header);

        for (String name : output.header) {
            DataSetColumnOutputModel column = new DataSetColumnOutputModel();
            column.setName(name);
            metadata.put(name, column);
        }

        // Data line consumer
        DataRowConsumer dataRowConsumer = new DataRowConsumer(metadata, output);

        JdbcManager.readWithFieldRow(conn, sql, dataRowConsumer, 10);


        output.setMetadataList(new ArrayList<>(metadata.values()));

        return output;
    }


    //region dto

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "文件名", messageOnEmpty = "请指定过滤器文件")
        private String filename;

        @Check(require = true, name = "数据集添加方法")
        private BloomfilterAddMethod bloomfilterAddMethod;

        @Check(name = "数据源id")
        private String dataSourceId;

        @Check(name = "sql脚本")
        private String sql;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            // If the source is a database, dataSourceId and sql must not be empty.
            if (DataSetAddMethod.Database.equals(bloomfilterAddMethod)) {
                if (StringUtils.isEmpty(dataSourceId)) {
                    throw new StatusCodeWithException("dataSourceId在数据库不存在", StatusCode.DATA_NOT_FOUND);
                }

                if (StringUtils.isEmpty(sql)) {
                    throw new StatusCodeWithException("请填入sql查询语句", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
                }
            }
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public BloomfilterAddMethod getBloomfilterAddMethod() {
            return bloomfilterAddMethod;
        }

        public void setBloomfilterAddMethod(BloomfilterAddMethod bloomfilterAddMethod) {
            this.bloomfilterAddMethod = bloomfilterAddMethod;
        }

        public String getDataSourceId() {
            return dataSourceId;
        }

        public void setDataSourceId(String dataSourceId) {
            this.dataSourceId = dataSourceId;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }
    }

    public static class Output {
        @Check(name = "字段列表")
        private List<String> header = new ArrayList<>();
        @Check(name = "原始数据列表")
        private List<Map<String, Object>> rawDataList = new ArrayList<>();
        @Check(name = "元数据信息")
        private List<DataSetColumnOutputModel> metadataList = new ArrayList<>();


        public List<String> getHeader() {
            return header;
        }

        public void setHeader(List<String> header) {
            this.header = header;
        }

        public List<Map<String, Object>> getRawDataList() {
            return rawDataList;
        }

        public void setRawDataList(List<Map<String, Object>> rawDataList) {
            this.rawDataList = rawDataList;
        }

        public List<DataSetColumnOutputModel> getMetadataList() {
            return metadataList;
        }

        public void setMetadataList(List<DataSetColumnOutputModel> metadataList) {
            this.metadataList = metadataList;
        }
    }


    //endregion
}
