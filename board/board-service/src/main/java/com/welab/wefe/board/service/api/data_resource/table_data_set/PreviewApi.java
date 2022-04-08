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

package com.welab.wefe.board.service.api.data_resource.table_data_set;

import com.welab.wefe.board.service.constant.DataSetAddMethod;
import com.welab.wefe.board.service.database.entity.DataSourceMysqlModel;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnOutputModel;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
@Api(path = "table_data_set/preview", name = "preview data set rows")
public class PreviewApi extends AbstractApi<PreviewApi.Input, PreviewApi.Output> {

    @Autowired
    TableDataSetService tableDataSetService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        Output output = new Output();
        // Read data from the database for preview
        if (DataSetAddMethod.Database.equals(input.getDataSetAddMethod())) {
            // Test whether SQL can be queried normally
            boolean result = tableDataSetService.testSqlQuery(input.getDataSourceId(), input.getSql());
            if (result) {
                output = readFromDatabase(input.getDataSourceId(), input.getSql());
            }
        } else {

            File file = tableDataSetService.getDataSetFile(input.getDataSetAddMethod(), input.getFilename());
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


        ColumnDataTypeInferrer columnDataTypeInferrer;

        try (
                AbstractTableDataSetReader reader = file.getName().endsWith("csv")
                        ? new CsvTableDataSetReader(file)
                        : new ExcelTableDataSetReader(file)
        ) {

            // Get column header
            List<String> header = reader.getHeader();

            // 读取数据并推理每个字段的数据类型
            columnDataTypeInferrer = new ColumnDataTypeInferrer(header);
            reader.read(columnDataTypeInferrer, 10000, 10_000);
        }

        return new Output(columnDataTypeInferrer);
    }

    private Output readFromDatabase(String dataSourceId, String sql) throws StatusCodeWithException {
        DataSourceMysqlModel model = tableDataSetService.getDataSourceById(dataSourceId);
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

        ColumnDataTypeInferrer columnDataTypeInferrer = new ColumnDataTypeInferrer(header);

        JdbcManager.readWithFieldRow(conn, sql, columnDataTypeInferrer, 10);


        return new Output(columnDataTypeInferrer);
    }


    //region dto

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "文件名", messageOnEmpty = "请指定数据集文件")
        private String filename;

        @Check(require = true, name = "数据集添加方法")
        private DataSetAddMethod dataSetAddMethod;

        @Check(name = "数据源id")
        private String dataSourceId;

        @Check(name = "sql脚本")
        private String sql;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            // If the source is a database, dataSourceId and sql must not be empty.
            if (DataSetAddMethod.Database.equals(dataSetAddMethod)) {
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

        public DataSetAddMethod getDataSetAddMethod() {
            return dataSetAddMethod;
        }

        public void setDataSetAddMethod(DataSetAddMethod dataSetAddMethod) {
            this.dataSetAddMethod = dataSetAddMethod;
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

        public Output() {
        }

        public Output(ColumnDataTypeInferrer inferrer) {
            header = inferrer.getColumnNames();
            rawDataList = inferrer.getSamples();
            metadataList = inferrer.getResult().entrySet().stream().map(x -> {
                        DataSetColumnOutputModel model = new DataSetColumnOutputModel();
                        model.setName(x.getKey());
                        model.setDataType(x.getValue());
                        return model;
                    })
                    .collect(Collectors.toList());
        }

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
