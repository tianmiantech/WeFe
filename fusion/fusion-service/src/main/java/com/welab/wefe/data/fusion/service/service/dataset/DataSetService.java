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
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.data.fusion.service.api.dataset.DeleteApi;
import com.welab.wefe.data.fusion.service.api.dataset.QueryApi;
import com.welab.wefe.data.fusion.service.database.entity.DataSetMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.DataSetRepository;
import com.welab.wefe.data.fusion.service.database.repository.DataSourceRepository;
import com.welab.wefe.data.fusion.service.dto.base.PagingOutput;
import com.welab.wefe.data.fusion.service.dto.entity.dataset.DataSetOutputModel;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.manager.JdbcManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author hunter.zhao
 */
@Service
public class DataSetService {

    @Autowired
    DataSourceRepository dataSourceRepo;


    @Autowired
    DataSetRepository dataSetRepository;

    @Value("${file.upload.dir}")
    private String fileUploadDir;

    @Value("${db.mysql.url}")
    private String mySqlUrl;

    @Value("${db.mysql.username}")
    private String mySqlUsername;

    @Value("${db.mysql.password}")
    private String mySqlPassword;

    private final String TABLE_HEADER = "data_fusion_";


    /**
     * @param id
     */
    public void increment(String id) {
        DataSetMySqlModel model = dataSetRepository.findOne("id", id, DataSetMySqlModel.class);
        model.setUsedCount(model.getUsedCount() + 1);
        model.setUpdatedTime(new Date());
        dataSetRepository.save(model);
    }


    public DataSetMySqlModel findById(String id) {
        return dataSetRepository.getOne(id);
    }

    /**
     * Find DataSource based on id
     *
     * @param dataSourceId
     * @return
     */
    public DataSourceMySqlModel getDataSourceById(String dataSourceId) {
        return dataSourceRepo.findById(dataSourceId).orElse(null);
    }

    /**
     * Test whether SQL can be queried normally
     */
    public boolean testSqlQuery(String dataSourceId, String sql) throws StatusCodeWithException {
        DataSourceMySqlModel model = getDataSourceById(dataSourceId);
        if (model == null) {
            throw new StatusCodeWithException("Data does not exist", StatusCode.DATA_NOT_FOUND);
        }

        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort()
                , model.getUserName(), model.getPassword(), model.getDatabaseName());
        boolean result = jdbcManager.testQuery(conn, sql, true);

        return result;
    }

    /**
     * Get the uploaded file
     */
    public File getDataSetFile(DataResourceSource method, String filename) throws StatusCodeWithException {
        File file = null;
        switch (method) {
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
            throw new StatusCodeWithException("File not found：" + file.getPath(), StatusCode.PARAMETER_VALUE_INVALID);
        }

        return file;
    }

    /**
     * Query the list of datasets
     */
    public List<DataSetMySqlModel> list() {
        return dataSetRepository.findAll();
    }

    /**
     * Look up the table by ID and iterate
     */
    public List<String> list(String dataSetId) throws StatusCodeWithException {
        String sql = "select " + "id" + " from " + TABLE_HEADER + dataSetId;

        List<String> result = new ArrayList<>();
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {

            conn = jdbcManager.getConnection(DatabaseType.MySql, mySqlUrl, mySqlUsername, mySqlPassword);

            statement = conn.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StatusCodeWithException(e.getMessage(), StatusCode.PARAMETER_VALUE_INVALID);
        } finally {
            jdbcManager.close(conn, statement, resultSet);
        }

        return result;
    }

    /**
     * Paging query data sets
     */
    public PagingOutput<DataSetOutputModel> query(QueryApi.Input input) {
        Specification<DataSetMySqlModel> where = Where
                .create()
                .equal("id", input.getId())
                .contains("name", input.getName())
                .build(DataSetMySqlModel.class);

        return dataSetRepository.paging(where, input, DataSetOutputModel.class);

    }

    /**
     * Delete filter
     *
     * @param input
     */
    public void delete(DeleteApi.Input input) {
        DataSetMySqlModel model = dataSetRepository.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        int ThreadCount = CommonThreadPool.actionThreadCount();
        System.out.println("ThreadCount:" + ThreadCount);
        CommonThreadPool.stop();


        dataSetRepository.deleteById(input.getId());
    }


    /**
     * Paging query data sets
     */
    public List<JObject> paging(List<String> columnList, String dataSetId, int pageIndex, int pageSize) throws StatusCodeWithException {

        StringBuilder columns = new StringBuilder();
        columnList.forEach(x -> columns.append(x + ","));

        String sql = "select " + columns.substring(0, columns.length() - 1) + " from " + TABLE_HEADER + dataSetId + " limit " + pageIndex * pageSize + "," + pageSize;


        List<JObject> result = new ArrayList<>();
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {

            conn = jdbcManager.getConnection(DatabaseType.MySql, mySqlUrl, mySqlUsername, mySqlPassword);

            statement = conn.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                JObject data = JObject.create();
                result.add(data);
                for (int i = 0; i < columnList.size(); i++) {
                    data.append(columnList.get(i), resultSet.getString(i + 1));
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StatusCodeWithException(e.getMessage(), StatusCode.PARAMETER_VALUE_INVALID);
        } finally {
            jdbcManager.close(conn, statement, resultSet);
        }

        return result;
    }
}
