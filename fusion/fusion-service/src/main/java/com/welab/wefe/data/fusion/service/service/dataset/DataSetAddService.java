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

package com.welab.wefe.data.fusion.service.service.dataset;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.data.fusion.service.api.dataset.AddApi;
import com.welab.wefe.data.fusion.service.config.Config;
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
    @Autowired
    private Config config;

    public AddApi.DataSetAddOutput addDataSet(AddApi.Input input) throws Exception {
        try {
            if (StringUtils.isNotBlank(config.getSourceFilterDir())) {
                File file = Paths.get(config.getSourceFilterDir()).toFile();
                if (!file.exists()) {
                    file.mkdirs();
                }
            }
        } catch (Exception e) {
            LOG.error("mkdir " + config.getSourceFilterDir() + " error", e);
        }
        if (input.getRows().size() > 5) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "选择字段数量不宜超过5个");
        }

        if (dataSetRepository.countByName(input.getName()) > 0) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "数据集名称已存在, 请更改其他名称再尝试提交！");
        }
        
        DataSetMySqlModel model = new DataSetMySqlModel();
        model.setUpdatedBy(CurrentAccountUtil.get().getId());
        model.setCreatedBy(CurrentAccountUtil.get().getId());
        model.setDescription(input.getDescription());
        model.setDataResourceSource(input.getDataResourceSource());
        model.setName(input.getName());
        model.setRows(StringUtil.join(input.getRows(), ','));
        model.setUsedCount(0);
        model.setRowCount(0);
        model.setUpdatedTime(new Date());
        model.setStatement(input.getSql());
        model.setSourcePath(input.getFilename());
        model.setDataSourceId(input.getDataSourceId());
        dataSetRepository.save(model);

        CommonThreadPool.TASK_SWITCH = true;

        if (DataResourceSource.Sql.equals(input.getDataResourceSource())) {
            readAndSaveFromDB(model, input.getDataSourceId(), input.getRows(), input.getSql(), input.isDeduplication());
        } else {
            // Parse and save the dataset file
            try {
                File file = dataSourceService.getDataSetFile(input.getDataResourceSource(), input.getFilename());
                readAndSaveFromFile(model, file, input.getRows(), input.isDeduplication());
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                StatusCode.FILE_IO_READ_ERROR.throwException();
            }
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
        int rowsCount = 0;
        AbstractDataSetReader dataSetReader = isCsv ? new CsvDataSetReader(file) : new ExcelDataSetReader(file);
        if (isCsv) {
            long fileLength = file.length();
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
            lineNumberReader.skip(fileLength);
            rowsCount = lineNumberReader.getLineNumber() - 1; // 这个方法对于xlsx会有不准确
            lineNumberReader.close();
        } else {
            rowsCount = (int) dataSetReader.getRowCount(0) - 1; // 去除header
        }
        model.setRowCount(rowsCount);
        DataSetRepository dataSetRepository = Launcher.CONTEXT.getBean(DataSetRepository.class);
        dataSetRepository.updateById(model.getId(), "process", Progress.Ready, DataSetMySqlModel.class);
        dataSetRepository.updateById(model.getId(), "rowCount", rowsCount, DataSetMySqlModel.class);
        // Gets the data set column header
        List<String> headers = dataSetReader.getHeader();

        DataSetStorageHelper.createDataSetTable(model.getId(), rows);
        DataSetAddServiceDataRowConsumer dataRowConsumer = new DataSetAddServiceDataRowConsumer(model, deduplication, file, rows);

        CommonThreadPool.run(() -> {
            try {
                dataSetReader.readAllWithSelectRow(dataRowConsumer, rows, 0);
            } catch (StatusCodeWithException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }

        });

        // Wait for the consumption queue to complete
//        dataRowConsumer.waitForFinishAndClose();

        model.setStoraged(true);


        LOG.info("The dataset is parsed：" + model.getId() + " spend:" + ((System.currentTimeMillis() - start) / 1000) + "s");

        return rowsCount;
    }


    /**
     * Read data from the specified database according to SQL and save to mysql
     */
    private int readAndSaveFromDB(DataSetMySqlModel model, String dataSourceId, List<String> headers, String sql, boolean deduplication) throws Exception {
        long start = System.currentTimeMillis();
        LOG.info("Start parsing the data set：" + model.getId());

        DataSourceMySqlModel dsModel = dataSourceService.getDataSourceById(dataSourceId);
        if (dsModel == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "数据不存在！");
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

        CommonThreadPool.run(() -> {
            jdbcManager.readWithSelectRow(conn, sql, dataRowConsumer, headers);
        });

        model.setStoraged(true);

        LOG.info("The dataset is parsed：" + model.getId() + " spend:" + ((System.currentTimeMillis() - start) / 1000) + "s");

        return rowsCount;

    }
}
