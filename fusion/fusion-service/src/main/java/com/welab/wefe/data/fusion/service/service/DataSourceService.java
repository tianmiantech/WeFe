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
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.data.fusion.service.api.datasource.AddApi;
import com.welab.wefe.data.fusion.service.api.datasource.DeleteApi;
import com.welab.wefe.data.fusion.service.api.datasource.QueryApi;
import com.welab.wefe.data.fusion.service.api.datasource.TestDBConnectApi;
import com.welab.wefe.data.fusion.service.api.datasource.*;
import com.welab.wefe.data.fusion.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.DataSetRepository;
import com.welab.wefe.data.fusion.service.database.repository.DataSourceRepository;
import com.welab.wefe.data.fusion.service.database.repository.BloomFilterRepository;
import com.welab.wefe.data.fusion.service.dto.base.PagingOutput;
import com.welab.wefe.data.fusion.service.dto.entity.DataSourceOverviewOutput;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.manager.JdbcManager;
import com.welab.wefe.data.fusion.service.utils.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Johnny.lin
 * @Description: Data source service class
 * @date 2020/9/16
 */
@Service
public class DataSourceService extends AbstractService {
    @Autowired
    DataSourceRepository dataSourceRepo;

    @Value("${file.upload.dir}")
    private String fileUploadDir;

    @Autowired
    DataSetRepository dataSetRepository;

    @Autowired
    BloomFilterRepository bloomFilterRepository;


    public AddApi.DataSourceAddOutput add(AddApi.DataSourceAddInput input) throws StatusCodeWithException {

        if (dataSourceRepo.countByName(input.getName()) > 0) {
            throw new StatusCodeWithException("This data source name already exists, please change the data source name", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // 测试连接
        testDBConnect(input.getDatabaseType(), input.getHost(), input.getPort(), input.getUserName(), input.getPassword(), input.getDatabaseName());
        DataSourceMySqlModel model = ModelMapper.map(input, DataSourceMySqlModel.class);
        model.setCreatedBy(CurrentAccount.id());
        model.setUpdatedBy(CurrentAccount.id());
        model.setCreatedTime(new Date());
        model.setUpdatedTime(new Date());
        dataSourceRepo.save(model);

        AddApi.DataSourceAddOutput output = new AddApi.DataSourceAddOutput();
        output.setId(model.getId());
        return output;
    }

    public UpdateApi.DataSourceUpdateOutput update(UpdateApi.DataSourceUpdateInput input) throws StatusCodeWithException {
        // Test the connection
        testDBConnect(input.getDatabaseType(), input.getHost(), input.getPort(), input.getUserName(), input.getPassword(), input.getDatabaseName());
        Map<String, Object> params = new HashMap<>(16);
        params.put("id", input.getId());
        params.put("name", input.getName());
        params.put("databaseType", input.getDatabaseType());
        params.put("databaseName", input.getDatabaseName());
        params.put("host", input.getHost());
        params.put("port", input.getPort());
        params.put("userName", input.getName());
        params.put("password", input.getPassword());
        params.put("updatedBy", CurrentAccount.id());
        params.put("updatedTime", new Date());
        dataSourceRepo.updateById(input.getId(), params, DataSourceMySqlModel.class);

        UpdateApi.DataSourceUpdateOutput output = new UpdateApi.DataSourceUpdateOutput();
        DataSourceMySqlModel model = ModelMapper.map(input, DataSourceMySqlModel.class);
        output.setId(model.getId());
        return output;
    }

    /**
     * Deleting a Data source
     *
     * @param input
     */
    public void delete(DeleteApi.Input input) {
        DataSourceMySqlModel model = dataSourceRepo.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        dataSourceRepo.deleteById(input.getId());
    }

    /**
     * Paging query data source
     *
     * @param input
     * @return
     */
    public PagingOutput<QueryApi.Output> query(QueryApi.Input input) {
        Specification<DataSourceMySqlModel> where = Where.create()
                .equal("name", input.getName())
                .build(DataSourceMySqlModel.class);

        return dataSourceRepo.paging(where, input, QueryApi.Output.class);
    }

    /**
     * Testing the database connection
     * @param databaseType
     * @param host
     * @param port
     * @param userName
     * @param password
     * @param databaseName
     * @return
     * @throws StatusCodeWithException
     */
    public TestDBConnectApi.Output testDBConnect(DatabaseType databaseType, String host, int port, String userName, String password, String databaseName) throws StatusCodeWithException {
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(databaseType, host, port, userName, password, databaseName);
        if (conn != null) {
            boolean success = jdbcManager.testQuery(conn);
            if (!success) {
                throw new StatusCodeWithException(StatusCode.DATABASE_LOST, "Database connection failure");
            }
        }

        TestDBConnectApi.Output output = new TestDBConnectApi.Output();
        output.setResult(true);
        return output;
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
            throw new StatusCodeWithException("未找到文件：" + file.getPath(), StatusCode.PARAMETER_VALUE_INVALID);
        }

        return file;
    }

    public DataSourceOverviewOutput overview() {
        Long dataSetCount = dataSetRepository.count();
        Long bloomFilterCount = bloomFilterRepository.count();
        return DataSourceOverviewOutput.of(dataSetCount, bloomFilterCount);
    }

}
