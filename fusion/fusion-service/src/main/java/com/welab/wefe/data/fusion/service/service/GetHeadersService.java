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

package com.welab.wefe.data.fusion.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.data.fusion.service.api.dataset.GetHeadersApi;
import com.welab.wefe.data.fusion.service.database.entity.DataSetMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.DataSetRepository;
import com.welab.wefe.data.fusion.service.database.repository.DataSourceRepository;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.manager.JdbcManager;
import com.welab.wefe.data.fusion.service.utils.AbstractDataSetReader;
import com.welab.wefe.data.fusion.service.utils.CsvDataSetReader;
import com.welab.wefe.data.fusion.service.utils.ExcelDataSetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * @author jacky.jiang
 */
@Service
public class GetHeadersService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected DataSetRepository repo;

    @Autowired
    DataSourceRepository dataSourceRepo;

    @Value("${file.upload.dir}")
    private String fileUploadDir;

    public List<String> getHeaders(GetHeadersApi.Input input) throws StatusCodeWithException {
        List<String> headers;
        DataSetMySqlModel model = ModelMapper.map(input, DataSetMySqlModel.class);
        model.setId(new DataSetMySqlModel().getId());
        model.setCreatedBy(CurrentAccount.id());

        if (DataResourceSource.Sql.equals(input.getDataResourceSource())) {
            headers = getHeadersFromDB(model, input.getSql());
        } else {
            File file = getDataSetFile(input.getDataResourceSource(), input.getFilename());
            try {
                headers = getHeadersFromFile(model, file);
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "File reading failure");
            }
        }

        return headers;
    }

    /**
     * Read data characteristic fields optionally from the specified database according to SQL
     *
     * @param model
     * @param sql
     * @throws StatusCodeWithException
     */
    private List<String> getHeadersFromDB(DataSetMySqlModel model, String sql) throws StatusCodeWithException {
        LOG.info("Start parsing the data set：" + model.getId());

        DatabaseType databaseType = DatabaseType.Hive;
        String host = "";
        int port = 1;
        String username = "";
        String password = "";
        String databaseName = "";

        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(databaseType, host, port
                , username, password, databaseName);

        // Gets the data set column header
        List<String> headers = jdbcManager.getRowHeaders(conn, sql);
        return headers;
    }

    /**
     * Read data characteristic fields optionally from the specified database based on the specified file
     *
     * @param model
     * @param file
     * @throws StatusCodeWithException, IOException
     */
    private List<String> getHeadersFromFile(DataSetMySqlModel model, File file) throws StatusCodeWithException, IOException {
        LOG.info("Start parsing the data set：" + model.getId());

        boolean isCsv = file.getName().endsWith("csv");

        AbstractDataSetReader dataSetReader = isCsv
                ? new CsvDataSetReader(file)
                : new ExcelDataSetReader(file);

        // Gets the data set column header
        List<String> headers = dataSetReader.getHeader();
        return headers;
    }

    /**
     * Get the uploaded file
     */
    private File getDataSetFile(DataResourceSource source, String filename) throws StatusCodeWithException {
        File file = null;
        switch (source) {
            case UploadFile:
                file = new File(fileUploadDir, filename);
                break;
            case LocalFile:
                file = new File(filename);
                break;
            case Sql:

                break;
            default:
        }

        if (null == file || !file.exists()) {
            throw new StatusCodeWithException("未找到文件：" + file.getPath(), StatusCode.PARAMETER_VALUE_INVALID);
        }

        return file;
    }

}
