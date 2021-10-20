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

package com.welab.wefe.data.fusion.service.service.dataset;


import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.api.dataset.AddApi;
import com.welab.wefe.data.fusion.service.database.entity.DataSetMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.DataSetRepository;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.enums.Progress;
import com.welab.wefe.data.fusion.service.manager.JdbcManager;
import com.welab.wefe.data.fusion.service.service.AbstractService;
import com.welab.wefe.data.fusion.service.service.DataSourceService;
import com.welab.wefe.data.fusion.service.utils.AbstractDataSetReader;
import com.welab.wefe.data.fusion.service.utils.CsvDataSetReader;
import com.welab.wefe.data.fusion.service.utils.ExcelDataSetReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.util.Date;
import java.util.List;


/**
 * New data set
 *
 * @author Jacky.jiang
 */
@Service
public class DataSetAddService extends AbstractService {

    @Autowired
    protected DataSetRepository dataSetRepository;
    @Autowired
    protected DataSourceService dataSourceService;

    @Value("${file.upload.dir}")
    private String fileUploadDir;

    @Value("${file.filter.dir}")
    private String filterDir;

    public AddApi.DataSetAddOutput addDataSet(AddApi.Input input) throws StatusCodeWithException, IOException {
        if (input.getRows().size() > 5 ) {
            throw new StatusCodeWithException("选择字段数量不宜超过5", StatusCode.PARAMETER_VALUE_INVALID);
        }

        if (dataSetRepository.countByName(input.getName()) > 0) {
            throw new StatusCodeWithException("This dataset name already exists, please change it to another dataset name", StatusCode.PARAMETER_VALUE_INVALID);
        }

        DataSetMySqlModel model = new DataSetMySqlModel();

        model.setUpdatedBy(CurrentAccount.id());
        model.setCreatedBy(CurrentAccount.id());
        model.setDescription(input.getDescription());
        model.setDataResourceSource(input.getDataResourceSource());
        model.setName(input.getName());
        model.setRows(StringUtil.join(input.getRows(), ','));

        int rowsCount = 0;
        model.setUsedCount(0);
        model.setRowCount(rowsCount);
        model.setUpdatedTime(new Date());
        dataSetRepository.save(model);

        File file = null;
        CommonThreadPool.TASK_SWITCH = true;
        if (DataResourceSource.Sql.equals(input.getDataResourceSource())) {
            model.setDataSourceId(input.getDataSourceId());
            DataSourceMySqlModel dataSourceMySqlModel = dataSourceService.getDataSourceById(input.getDataSourceId());
            String sql = "select * from " + dataSourceMySqlModel.getDatabaseName();

            rowsCount = readAndSaveFromDB(model, input.getDataSourceId(), input.getRows(), sql, input.isDeduplication());
            model.setStatement(input.getSql());
        } else {
            file = dataSourceService.getDataSetFile(input.getDataResourceSource(), input.getFilename());

            try {
                rowsCount = readAndSaveFromFile(model, file, input.getRows(), input.isDeduplication());
                model.setSourcePath(input.getFilename());
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "File reading failure");
            }
        }

        if (CommonThreadPool.TASK_SWITCH) {
            model.setUsedCount(0);
            model.setRowCount(rowsCount);
            model.setUpdatedTime(new Date());
            dataSetRepository.save(model);
        }

        AddApi.DataSetAddOutput output = new AddApi.DataSetAddOutput();
        output.setDataSourceId(model.getDataSourceId());
        output.setId(model.getId());
        return output;
    }

    /**
     * Parse the dataset file and save it to mysql
     *
     * @param deduplication Whether the data set needs to be deduplicated
     * @return Whether the data set needs to be deduplicated
     */
    private int readAndSaveFromFile(DataSetMySqlModel model, File file, List<String> rows, boolean deduplication) throws IOException, StatusCodeWithException {
        long start = System.currentTimeMillis();
        LOG.info("Start parsing the data set：" + model.getId());

        boolean isCsv = file.getName().endsWith("csv");

        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        reader.skip(Long.MAX_VALUE);
        int rowsCount = reader.getLineNumber() - 1;
        model.setRowCount(rowsCount);
        DataSetRepository dataSetRepository = Launcher.CONTEXT.getBean(DataSetRepository.class);
        dataSetRepository.updateById(model.getId(), "process", Progress.Ready, DataSetMySqlModel.class);
        dataSetRepository.updateById(model.getId(), "rowCount", rowsCount, DataSetMySqlModel.class);

        AbstractDataSetReader dataSetReader = isCsv
                ? new CsvDataSetReader(file)
                : new ExcelDataSetReader(file);

        // Gets the data set column header
        List<String> headers = dataSetReader.getHeader(rows);

        DataSetStorageHelper.createDataSetTable(model.getId(), rows);

        DataSetAddServiceDataRowConsumer dataRowConsumer = new DataSetAddServiceDataRowConsumer(model, deduplication, file, rows);

        dataSetReader.readAllWithSelectRow(dataRowConsumer, rows, 0);

        // Wait for the consumption queue to complete
        dataRowConsumer.waitForFinishAndClose();

        model.setStoraged(true);


        LOG.info("The dataset is parsed：" + model.getId() + " spend:" + ((System.currentTimeMillis() - start) / 1000) + "s");

        return rowsCount;
    }


    /**
     * Read data from the specified database according to SQL and save to mysql
     */
    private int readAndSaveFromDB(DataSetMySqlModel model, String dataSourceId, List<String> headers, String sql, boolean deduplication) throws StatusCodeWithException {
        long start = System.currentTimeMillis();
        LOG.info("Start parsing the data set：" + model.getId());

        DataSourceMySqlModel dsModel = dataSourceService.getDataSourceById(dataSourceId);
        if (dsModel == null) {
            throw new StatusCodeWithException("Data does not exist", StatusCode.DATA_NOT_FOUND);
        }

        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(dsModel.getDatabaseType(), dsModel.getHost(), dsModel.getPort()
                , dsModel.getUserName(), dsModel.getPassword(), dsModel.getDatabaseName());

        // The total number of rows based on the query statement
        long rowsCountFromDB = jdbcManager.count(conn, sql);
        int rowsCount = (int) rowsCountFromDB;
        model.setRowCount(rowsCount);
        DataSetRepository dataSetRepository = Launcher.CONTEXT.getBean(DataSetRepository.class);
        dataSetRepository.updateById(model.getId(), "process", Progress.Ready, DataSetMySqlModel.class);
        dataSetRepository.updateById(model.getId(), "rowCount", rowsCount, DataSetMySqlModel.class);

        DataSetStorageHelper.createDataSetTable(model.getId(), headers);

        DataSetAddServiceDataRowConsumer dataRowConsumer = new DataSetAddServiceDataRowConsumer(model, deduplication, rowsCount, headers);

        jdbcManager.readWithSelectRow(conn, sql, dataRowConsumer, headers);
//
//        // Wait for the consumption queue to complete
        dataRowConsumer.waitForFinishAndClose();
        model.setStoraged(true);

        LOG.info("The dataset is parsed：" + model.getId() + " spend:" + ((System.currentTimeMillis() - start) / 1000) + "s");

        return rowsCount;

    }
}
