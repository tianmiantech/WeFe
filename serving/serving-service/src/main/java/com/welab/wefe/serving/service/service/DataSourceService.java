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

package com.welab.wefe.serving.service.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.DatabaseType;
import com.welab.wefe.serving.service.api.datasource.*;
import com.welab.wefe.serving.service.api.datasource.QueryTableFieldsApi.FieldOutput;
import com.welab.wefe.serving.service.api.datasource.QueryTablesApi.Output;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.repository.DataSourceRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.manager.JdbcManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Johnny.lin
 * @Description: Data source service class
 * @date 2020/9/16
 */
@Service
public class DataSourceService {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DataSourceRepository dataSourceRepo;

    public AddApi.DataSourceAddOutput add(AddApi.DataSourceAddInput input) throws StatusCodeWithException {

        if (dataSourceRepo.countByName(input.getName()) > 0) {
            throw new StatusCodeWithException("数据源名称已存在，请更改后再试", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // 测试连接
        testDBConnect(input.getDatabaseType(), input.getHost(), input.getPort(), input.getUserName(),
                input.getPassword(), input.getDatabaseName());
        DataSourceMySqlModel model = ModelMapper.map(input, DataSourceMySqlModel.class);
        model.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        model.setCreatedBy(CurrentAccountUtil.get().getId());
        model.setUpdatedBy(CurrentAccountUtil.get().getId());
        model.setCreatedTime(new Date());
        model.setUpdatedTime(new Date());
        dataSourceRepo.save(model);

        AddApi.DataSourceAddOutput output = new AddApi.DataSourceAddOutput();
        output.setId(model.getId());
        return output;
    }

    public UpdateApi.DataSourceUpdateOutput update(UpdateApi.DataSourceUpdateInput input)
            throws StatusCodeWithException {
        DataSourceMySqlModel model = dataSourceRepo.findById(input.getId()).orElse(null);
        if (model == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
        }
        // Test the connection
        testDBConnect(input.getDatabaseType(), input.getHost(), input.getPort(), input.getUserName(),
                input.getPassword().equalsIgnoreCase(DataSourceMySqlModel.PASSWORD_MASK) ? model.getPassword()
                        : input.getPassword(),
                input.getDatabaseName());
        Map<String, Object> params = new HashMap<>(16);
        params.put("id", input.getId());
        params.put("name", input.getName());
        params.put("databaseType", input.getDatabaseType());
        params.put("databaseName", input.getDatabaseName());
        params.put("host", input.getHost());
        params.put("port", input.getPort());
        params.put("userName", input.getUserName());
        params.put("password",
                input.getPassword().equalsIgnoreCase(DataSourceMySqlModel.PASSWORD_MASK) ? model.getPassword()
                        : input.getPassword());
        params.put("updatedBy", CurrentAccountUtil.get().getId());
        params.put("updatedTime", new Date());
        dataSourceRepo.updateById(input.getId(), params, DataSourceMySqlModel.class);

        UpdateApi.DataSourceUpdateOutput output = new UpdateApi.DataSourceUpdateOutput();
        output.setId(input.getId());
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

        Where where = Where.create();
        if (StringUtils.isNotBlank(input.getId())) {
            where.equal("id", input.getId());
        }
        if (StringUtils.isNotBlank(input.getName())) {
            where.contains("name", input.getName());
        }
        Specification<DataSourceMySqlModel> spe = where.build(DataSourceMySqlModel.class);

        return dataSourceRepo.paging(spe, input, QueryApi.Output.class);
    }

    /**
     * Testing the database connection
     *
     * @param databaseType
     * @param host
     * @param port
     * @param userName
     * @param password
     * @param databaseName
     * @return
     * @throws StatusCodeWithException
     */
    public TestDBConnectApi.Output testDBConnect(DatabaseType databaseType, String host, int port, String userName,
                                                 String password, String databaseName) throws StatusCodeWithException {
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(databaseType, host, port, userName, password, databaseName);
        if (conn != null) {
            boolean success = jdbcManager.testQuery(conn, "select 1");
            if (!success) {
                throw new StatusCodeWithException(StatusCode.DATABASE_LOST, "数据库连接失败");
            }
        }

        TestDBConnectApi.Output output = new TestDBConnectApi.Output();
        output.setResult(true);
        return output;
    }

    public void createTable(String sql, DatabaseType databaseType, String host, int port, String userName,
                            String password, String databaseName) throws StatusCodeWithException {
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(databaseType, host, port, userName, password, databaseName);
        if (conn != null) {
            boolean success = jdbcManager.execute(conn, sql);
            if (!success) {
                throw new StatusCodeWithException(StatusCode.SQL_ERROR, "SQL 执行报错");
            }
        }
    }

    public void batchInsert(String sql, DatabaseType databaseType, String host, int port, String userName,
                            String password, String databaseName, Set<String> ids) throws StatusCodeWithException {
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(databaseType, host, port, userName, password, databaseName);
        if (conn != null) {
            try {
                jdbcManager.batchInsert(conn, sql, ids);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }
        throw new StatusCodeWithException(StatusCode.SQL_ERROR, "SQL 执行报错");
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
    
    public boolean update(DataSourceMySqlModel model, String sql) throws StatusCodeWithException {
        if (model == null) {
            throw new StatusCodeWithException("数据源不存在", StatusCode.DATA_NOT_FOUND);
        }
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort(),
                model.getUserName(), model.getPassword(), model.getDatabaseName());
        boolean result = jdbcManager.update(conn, sql);
        return result;
    }

    public Map<String, String> queryOne(DataSourceMySqlModel model, String sql, List<String> returnFields)
            throws StatusCodeWithException {
        if (model == null) {
            throw new StatusCodeWithException("数据源不存在", StatusCode.DATA_NOT_FOUND);
        }
        LOG.info("dataSourceModel = " + JSONObject.toJSONString(model));
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort(),
                model.getUserName(), model.getPassword(), model.getDatabaseName());
        return jdbcManager.queryOne(conn, sql, returnFields);
    }

    public long count(DataSourceMySqlModel model, String sql) throws StatusCodeWithException {
        if (model == null) {
            throw new StatusCodeWithException("数据源不存在", StatusCode.DATA_NOT_FOUND);
        }
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort(),
                model.getUserName(), model.getPassword(), model.getDatabaseName());
        return jdbcManager.count(conn, sql);
    }

    public List<Map<String, String>> queryList(DataSourceMySqlModel model, String sql, List<String> returnFields)
            throws StatusCodeWithException {
        if (model == null) {
            throw new StatusCodeWithException("数据源不存在", StatusCode.DATA_NOT_FOUND);
        }
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort(),
                model.getUserName(), model.getPassword(), model.getDatabaseName());
        return jdbcManager.queryList(conn, sql, returnFields);
    }

    public Output queryTables(String dataSourceId) throws StatusCodeWithException {
        DataSourceMySqlModel model = getDataSourceById(dataSourceId);
        Output out = new Output();
        if (model == null) {
            throw new StatusCodeWithException("数据源不存在", StatusCode.DATA_NOT_FOUND);
        }
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort(),
                model.getUserName(), model.getPassword(), model.getDatabaseName());
        List<String> tables = jdbcManager.queryTables(conn);
        out.setDatabaseType(model.getDatabaseType());
        out.setDatabaseName(model.getDatabaseName());
        out.setTables(tables);
        return out;
    }

    public com.welab.wefe.serving.service.api.datasource.QueryTableFieldsApi.Output queryTableFields(
            com.welab.wefe.serving.service.api.datasource.QueryTableFieldsApi.Input input)
            throws StatusCodeWithException {

        com.welab.wefe.serving.service.api.datasource.QueryTableFieldsApi.Output out = new com.welab.wefe.serving.service.api.datasource.QueryTableFieldsApi.Output();
        DataSourceMySqlModel model = getDataSourceById(input.getId());
        if (model == null) {
            throw new StatusCodeWithException("数据源不存在", StatusCode.DATA_NOT_FOUND);
        }
        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort(),
                model.getUserName(), model.getPassword(), model.getDatabaseName());
        Map<String, String> fieldMap = jdbcManager.queryTableFields(conn, input.getTableName());
        out.setDatabaseName(model.getDatabaseName());
        out.setDatabaseType(model.getDatabaseType());
        out.setTableName(input.getTableName());
        List<FieldOutput> fields = new ArrayList<>();
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            FieldOutput foutput = new FieldOutput();
            foutput.setName(entry.getKey());
            foutput.setType(entry.getValue());
            fields.add(foutput);
        }
        out.setFields(fields);
        return out;
    }

    public DataSourceMySqlModel findById(String dataSourceId) {
        return dataSourceRepo.findOne("id", dataSourceId, DataSourceMySqlModel.class);
    }
}
