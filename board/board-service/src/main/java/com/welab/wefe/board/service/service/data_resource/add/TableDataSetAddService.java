/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.service.data_resource.add;

import com.welab.wefe.board.service.constant.DataSetAddMethod;
import com.welab.wefe.board.service.database.entity.DataSourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceUploadTaskMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.repository.data_resource.TableDataSetRepository;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.TableDataSetAddInputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.DataSetColumnService;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.service.data_resource.DataResourceUploadTaskService;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.board.service.util.*;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DataResourceType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The service class for add data set
 *
 * @author Zane
 */
@Service
public class TableDataSetAddService extends AbstractDataResourceAddService {

    @Autowired
    protected TableDataSetRepository tableDataSetRepository;
    @Autowired
    protected TableDataSetService tableDataSetService;
    @Autowired
    protected DataSetStorageService dataSetStorageService;
    @Autowired
    protected DataSetColumnService dataSetColumnService;
    @Autowired
    protected DataResourceUploadTaskService dataResourceUploadTaskService;


    @Override
    public void doAdd(AbstractDataResourceUpdateInputModel in, DataResourceUploadTaskMysqlModel task, DataResourceMysqlModel m) throws StatusCodeWithException {
        TableDataSetAddInputModel input = (TableDataSetAddInputModel) in;
        TableDataSetMysqlModel model = (TableDataSetMysqlModel) m;

        // Parse and save the original data set
        AbstractTableDataSetReader dataSetReader = createDataSetReader(input);
        readAllToStorage(model, dataSetReader, input.isDeduplication());

        // save data set info to database
        tableDataSetRepository.save(model);

        // save data set column info to database
        dataSetColumnService.update(model.getId(), input.getMetadataList());

        // Delete files uploaded by HttpUpload
        try {
            if (input.getDataSetAddMethod().equals(DataSetAddMethod.HttpUpload)) {
                File file = tableDataSetService.getDataSetFile(input.getDataSetAddMethod(), input.getFilename());
                FileUtils.deleteQuietly(file);
            }
        } catch (StatusCodeWithException e) {
            super.log(e);
        }

        // Refresh the data set tag list
        CacheObjects.refreshTableDataSetTags();

    }

    @Override
    protected Class<? extends DataResourceMysqlModel> getMysqlModelClass() {
        return TableDataSetMysqlModel.class;
    }

    @Override
    protected DataResourceType getDataResourceType() {
        return DataResourceType.TableDataSet;
    }

    /**
     * create AbstractDataSetReader
     */
    private AbstractTableDataSetReader createDataSetReader(TableDataSetAddInputModel input) throws StatusCodeWithException {
        switch (input.getDataSetAddMethod()) {
            case Database:
                return createSqlDataSetReader(input);
            case HttpUpload:
            case LocalFile:
                return createFileDataSetReader(input);
            default:
                StatusCode
                        .UNEXPECTED_ENUM_CASE
                        .throwException("暂不支持的数据集解析方式：" + input.getDataSetAddMethod());
        }

        return null;
    }

    /**
     * create CsvDataSetReader/ExcelDataSetReader
     */
    private AbstractTableDataSetReader createFileDataSetReader(TableDataSetAddInputModel input) throws StatusCodeWithException {
        try {
            File file = tableDataSetService.getDataSetFile(input.getDataSetAddMethod(), input.getFilename());
            boolean isCsv = file.getName().endsWith("csv");
            return isCsv
                    ? new CsvTableDataSetReader(input.getMetadataList(), file)
                    : new ExcelTableDataSetReader(input.getMetadataList(), file);

        } catch (IOException e) {
            StatusCode.FILE_IO_ERROR.throwException(e);
            return null;
        }
    }

    /**
     * create SqlDataSetReader
     */
    private SqlTableDataSetReader createSqlDataSetReader(TableDataSetAddInputModel input) throws StatusCodeWithException {
        DataSourceMysqlModel dataSource = tableDataSetService.getDataSourceById(input.getDataSourceId());
        if (dataSource == null) {
            throw new StatusCodeWithException("此dataSourceId在数据库不存在", StatusCode.DATA_NOT_FOUND);
        }
        Connection conn = JdbcManager.getConnection(
                dataSource.getDatabaseType(),
                dataSource.getHost(),
                dataSource.getPort(),
                dataSource.getUserName(),
                dataSource.getPassword(),
                dataSource.getDatabaseName()
        );

        return new SqlTableDataSetReader(input.getMetadataList(), conn, input.getSql());
    }

    /**
     * Parse the dataset file and save it to lmdb/clickhouse
     *
     * @param deduplication Do you need to de-duplicate the data set
     */
    private void readAllToStorage(TableDataSetMysqlModel model, AbstractTableDataSetReader dataSetReader, boolean deduplication) throws StatusCodeWithException {
        long start = System.currentTimeMillis();
        LOG.info("开始解析数据集：" + model.getId());

        // update data set upload task info
        DataResourceUploadTaskMysqlModel uploadProgress = dataResourceUploadTaskService.findByDataResourceId(model.getId());
        dataResourceUploadTaskService.update(uploadProgress, x -> x.setTotalDataCount(dataSetReader.getTotalDataRowCount()));

        // get data set headers
        List<String> rawHeaders = dataSetReader.getHeader();
        // order headers
        List<String> sortedHeaders = sortHeaders(rawHeaders);

        // save headers info to storage
        dataSetStorageService.saveHeaderRow(model.getId(), sortedHeaders);
        // data row consumption method
        TableDataSetAddServiceDataRowConsumer dataRowConsumer = new TableDataSetAddServiceDataRowConsumer(model.getId(), deduplication, dataSetReader);

        // read all data rows of the raw data set
        dataSetReader.readAll(dataRowConsumer);

        // wait for the consumption queue to finish
        dataRowConsumer.waitForFinishAndClose();

        LOG.info("数据集解析完毕：" + model.getId() + " spend:" + ((System.currentTimeMillis() - start) / 1000) + "s");

        // fill model
        model.setContainsY(dataSetReader.isContainsY());
        model.setPrimaryKeyColumn(rawHeaders.get(0));
        model.setTotalDataCount(dataSetReader.getReadDataRows() - dataRowConsumer.getRepeatDataCount());
        model.setColumnCount(rawHeaders.size());
        model.setColumnNameList(StringUtil.join(sortedHeaders, ','));
        model.setFeatureCount(dataSetReader.isContainsY() ? rawHeaders.size() - 2 : rawHeaders.size() - 1);
        model.setFeatureNameList(StringUtil.join(rawHeaders.stream().filter(x -> !model.getPrimaryKeyColumn().equals(x) && !"y".equals(x)).collect(Collectors.toList()), ','));
        model.setyCount(dataSetReader.isContainsY() ? 1 : 0);
        model.setyNameList(dataSetReader.isContainsY() ? "y" : null);
        model.setyPositiveSampleCount(dataRowConsumer.getPositiveExampleCount());
        model.setyPositiveSampleRatio(dataRowConsumer.getPositiveExampleRatio());

    }

    /**
     * sort headers, move column y to the second column.
     */
    private List<String> sortHeaders(List<String> headers) {
        if (!headers.contains("y")) {
            return headers;
        }

        // A new list must be opened here, and the original column header cannot be modified.
        List<String> list = new ArrayList<>();
        for (String name : headers) {
            if ("y".equals(name)) {
                continue;
            }
            list.add(name);
        }
        list.add(1, "y");
        return list;
    }

}
