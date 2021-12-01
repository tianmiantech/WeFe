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

package com.welab.wefe.board.service.service.dataset;

import com.welab.wefe.board.service.constant.DataSetAddMethod;
import com.welab.wefe.board.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.DataSetTaskMysqlModel;
import com.welab.wefe.board.service.database.repository.DataSetRepository;
import com.welab.wefe.board.service.database.repository.DataSetTaskRepository;
import com.welab.wefe.board.service.dto.vo.data_set.TableDataSetAddInputModel;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.DataSetColumnService;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.util.*;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
public class DataSetAddService extends AbstractService {

    @Autowired
    protected DataSetRepository repo;
    @Autowired
    protected DataSetService dataSetService;
    @Autowired
    protected DataSetStorageService dataSetStorageService;
    @Autowired
    protected DataSetColumnService dataSetColumnService;
    @Autowired
    protected UnionService unionService;
    @Autowired
    protected DataSetTaskService dataSetTaskService;
    @Autowired
    protected DataSetTaskRepository repository;

    /**
     * Asynchronous execution of data writing
     *
     * @param userInfo Since this method is executed in an asynchronous thread,
     *                 the CurrentAccount information cannot be obtained, so it needs to be passed.
     */
    @Async
    public void add(TableDataSetAddInputModel input, DataSetTaskMysqlModel dataSetTask, CurrentAccount.Info userInfo) {

        DataSetMysqlModel model = new ModelMapper().map(input, DataSetMysqlModel.class);
        model.setId(dataSetTask.getDataSetId());
        model.setCreatedBy(userInfo.id);
        model.setTags(dataSetService.standardizeTags(input.getTags()));
        dataSetService.handlePublicMemberList(model);

        // Parse and save the original data set
        try {
            AbstractDataSetReader dataSetReader = createDataSetReader(input);
            readAllToStorage(model, dataSetReader, input.isDeduplication());
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            dataSetTaskService.onError(dataSetTask.getDataSetId(), e);
            return;
        }


        model.setNamespace(DataSetStorageService.DATABASE_NAME);
        model.setTableName(dataSetStorageService.createRawDataSetTableName(model.getId()));

        // save data set info to database
        repo.save(model);

        // save data set column info to database
        dataSetColumnService.update(model.getId(), input.getMetadataList(), userInfo);

        // Mark upload task completed
        dataSetTaskService.complete(dataSetTask.getDataSetId());

        // Delete files uploaded by HttpUpload
        try {
            if (input.getDataSetAddMethod().equals(DataSetAddMethod.HttpUpload)) {
                File file = dataSetService.getDataSetFile(input.getDataSetAddMethod(), input.getFilename());
                FileUtils.deleteQuietly(file);
            }
        } catch (StatusCodeWithException e) {
            super.log(e);
        }

        // Synchronize information to union
        try {
            unionService.uploadTableDataSet(model);
        } catch (StatusCodeWithException e) {
            super.log(e);
        }

        // Refresh the data set tag list
        CacheObjects.refreshTableDataSetTags();

    }

    /**
     * create AbstractDataSetReader
     */
    private AbstractDataSetReader createDataSetReader(TableDataSetAddInputModel input) throws StatusCodeWithException {
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
    private AbstractDataSetReader createFileDataSetReader(TableDataSetAddInputModel input) throws StatusCodeWithException {
        try {
            File file = dataSetService.getDataSetFile(input.getDataSetAddMethod(), input.getFilename());
            boolean isCsv = file.getName().endsWith("csv");
            return isCsv
                    ? new CsvDataSetReader(input.getMetadataList(), file)
                    : new ExcelDataSetReader(input.getMetadataList(), file);

        } catch (IOException e) {
            StatusCode.FILE_IO_ERROR.throwException(e);
            return null;
        }
    }

    /**
     * create SqlDataSetReader
     */
    private SqlDataSetReader createSqlDataSetReader(TableDataSetAddInputModel input) throws StatusCodeWithException {
        DataSourceMySqlModel dataSource = dataSetService.getDataSourceById(input.getDataSourceId());
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

        return new SqlDataSetReader(input.getMetadataList(), conn, input.getSql());
    }

    /**
     * Parse the dataset file and save it to lmdb/clickhouse
     *
     * @param deduplication Do you need to de-duplicate the data set
     */
    private void readAllToStorage(DataSetMysqlModel model, AbstractDataSetReader dataSetReader, boolean deduplication) throws StatusCodeWithException {
        long start = System.currentTimeMillis();
        LOG.info("开始解析数据集：" + model.getId());

        // update data set upload task info
        DataSetTaskMysqlModel dataSetTask = dataSetTaskService.findByDataSetId(model.getId());
        dataSetTaskService.update(dataSetTask, x -> x.setTotalRowCount(dataSetReader.getTotalDataRowCount()));

        // get data set headers
        List<String> rawHeaders = dataSetReader.getHeader();
        // order headers
        List<String> sortedHeaders = sortHeaders(rawHeaders);

        // save headers info to storage
        dataSetStorageService.saveHeaderRow(model.getId(), sortedHeaders);
        // data row consumption method
        DataSetAddServiceDataRowConsumer dataRowConsumer = new DataSetAddServiceDataRowConsumer(model.getId(), deduplication, dataSetReader);

        // read all data rows of the raw data set
        dataSetReader.readAll(dataRowConsumer);

        // wait for the consumption queue to finish
        dataRowConsumer.waitForFinishAndClose();

        LOG.info("数据集解析完毕：" + model.getId() + " spend:" + ((System.currentTimeMillis() - start) / 1000) + "s");

        // fill model
        model.setContainsY(dataSetReader.isContainsY());
        model.setPrimaryKeyColumn(rawHeaders.get(0));
        model.setRowCount(dataSetReader.getReadDataRows() - dataRowConsumer.getRepeatDataCount());
        model.setColumnCount(rawHeaders.size());
        model.setColumnNameList(StringUtil.join(sortedHeaders, ','));
        model.setFeatureCount(dataSetReader.isContainsY() ? rawHeaders.size() - 2 : rawHeaders.size() - 1);
        model.setFeatureNameList(StringUtil.join(rawHeaders.stream().filter(x -> !model.getPrimaryKeyColumn().equals(x) && !"y".equals(x)).collect(Collectors.toList()), ','));
        model.setyCount(dataSetReader.isContainsY() ? 1 : 0);
        model.setyNameList(dataSetReader.isContainsY() ? "y" : null);
        model.setyPositiveExampleCount(dataRowConsumer.getPositiveExampleCount());
        model.setyPositiveExampleRatio(dataRowConsumer.getPositiveExampleRatio());

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
