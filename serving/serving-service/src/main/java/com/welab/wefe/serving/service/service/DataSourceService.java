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

package com.welab.wefe.serving.service.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.serving.service.api.datasource.AddApi;
import com.welab.wefe.serving.service.api.datasource.DeleteApi;
import com.welab.wefe.serving.service.api.datasource.QueryApi;
import com.welab.wefe.serving.service.api.datasource.QueryTableFieldsApi.FieldOutput;
import com.welab.wefe.serving.service.api.datasource.QueryTablesApi.Input;
import com.welab.wefe.serving.service.api.datasource.QueryTablesApi.Output;
import com.welab.wefe.serving.service.api.datasource.TestDBConnectApi;
import com.welab.wefe.serving.service.api.datasource.UpdateApi;
import com.welab.wefe.serving.service.database.serving.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.DataSourceRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.manager.JdbcManager;
import com.welab.wefe.serving.service.utils.ModelMapper;

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
			throw new StatusCodeWithException(
					"This data source name already exists, please change the data source name",
					StatusCode.PARAMETER_VALUE_INVALID);
		}

		// 测试连接
		testDBConnect(input.getDatabaseType(), input.getHost(), input.getPort(), input.getUserName(),
				input.getPassword(), input.getDatabaseName());
		DataSourceMySqlModel model = ModelMapper.map(input, DataSourceMySqlModel.class);
		model.setId(UUID.randomUUID().toString().replaceAll("-", ""));
		model.setCreatedBy(CurrentAccount.id());
		model.setUpdatedBy(CurrentAccount.id());
		model.setCreatedTime(new Date());
		model.setUpdatedTime(new Date());
		dataSourceRepo.save(model);

		AddApi.DataSourceAddOutput output = new AddApi.DataSourceAddOutput();
		output.setId(model.getId());
		return output;
	}

	public UpdateApi.DataSourceUpdateOutput update(UpdateApi.DataSourceUpdateInput input)
			throws StatusCodeWithException {
		// Test the connection
		testDBConnect(input.getDatabaseType(), input.getHost(), input.getPort(), input.getUserName(),
				input.getPassword(), input.getDatabaseName());
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
		Specification<DataSourceMySqlModel> where = Where.create().equal("id", input.getId())
				.build(DataSourceMySqlModel.class);

		return dataSourceRepo.paging(where, input, QueryApi.Output.class);
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
			boolean success = jdbcManager.testQuery(conn, "select 1", false);
			if (!success) {
				throw new StatusCodeWithException(StatusCode.DATABASE_LOST, "Database connection failure");
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
				throw new StatusCodeWithException(StatusCode.SQL_ERROR, "execute sql error");
			}
		}
	}

	public void batchInsert(String sql, DatabaseType databaseType, String host, int port, String userName,
			String password, String databaseName, List<String> ids) throws StatusCodeWithException {
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
		throw new StatusCodeWithException(StatusCode.SQL_ERROR, "execute sql error");
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
		Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort(),
				model.getUserName(), model.getPassword(), model.getDatabaseName());
		boolean result = jdbcManager.testQuery(conn, sql, true);

		return result;
	}

	public Map<String, String> queryOne(String dataSourceId, String sql, List<String> returnFields)
			throws StatusCodeWithException {
		DataSourceMySqlModel model = getDataSourceById(dataSourceId);
		if (model == null) {
			throw new StatusCodeWithException("Data does not exist", StatusCode.DATA_NOT_FOUND);
		}
		LOG.info("dataSourceModel = " + JSONObject.toJSONString(model));
		JdbcManager jdbcManager = new JdbcManager();
		Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort(),
				model.getUserName(), model.getPassword(), model.getDatabaseName());
		return jdbcManager.query(conn, sql, returnFields);
	}

	public List<Map<String, String>> queryList(String dataSourceId, String sql, List<String> returnFields)
			throws StatusCodeWithException {
		DataSourceMySqlModel model = getDataSourceById(dataSourceId);
		if (model == null) {
			throw new StatusCodeWithException("Data does not exist", StatusCode.DATA_NOT_FOUND);
		}
		JdbcManager jdbcManager = new JdbcManager();
		Connection conn = jdbcManager.getConnection(model.getDatabaseType(), model.getHost(), model.getPort(),
				model.getUserName(), model.getPassword(), model.getDatabaseName());
		return jdbcManager.queryList(conn, sql, returnFields);
	}

	public Output queryTables(Input input) throws StatusCodeWithException {
		Output out = new Output();
		DataSourceMySqlModel model = getDataSourceById(input.getId());
		if (model == null) {
			throw new StatusCodeWithException("Data does not exist", StatusCode.DATA_NOT_FOUND);
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
			throw new StatusCodeWithException("Data does not exist", StatusCode.DATA_NOT_FOUND);
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

}
