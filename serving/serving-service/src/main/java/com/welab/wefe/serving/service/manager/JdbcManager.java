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

package com.welab.wefe.serving.service.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
//import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;

/**
 * @author Johnny.lin
 * @Description: JDBC Management Tool
 * @date 2020/9/17
 */
public class JdbcManager {
	private static final Logger log = Logger.getLogger(JdbcManager.class);

	public JdbcManager() {

	}

	/**
	 * Incoming IP port connection
	 *
	 * @param databaseType
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param dbName
	 * @return
	 * @throws StatusCodeWithException
	 */
	public Connection getConnection(DatabaseType databaseType, String host, Integer port, String userName,
			String password, String dbName) throws StatusCodeWithException {

		Connection conn = null;
		try {
			String url = "";
			switch (databaseType) {
			case Hive:
				url = String.format("jdbc:hive2://%s:%d/%s", host, port, dbName);
				break;
			case MySql:
				url = String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
				break;
			case Impala:
				url = String.format("jdbc:hive2://%s:%d/%s", host, port, dbName);
				break;
			default:
				throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, databaseType.toString());
			}

			conn = getConnection(databaseType, url, userName, password);
		} catch (Exception e) {
			log.error("Database connection failure", e);
			throw new StatusCodeWithException(StatusCode.DATABASE_LOST, "Database connection failure");
		}

		return conn;
	}

	/**
	 * Path connection
	 *
	 * @param databaseType
	 * @param url
	 * @param userName
	 * @param password
	 * @return
	 * @throws StatusCodeWithException
	 */
	public Connection getConnection(DatabaseType databaseType, String url, String userName, String password)
			throws StatusCodeWithException {

		Connection conn = null;
		try {
			switch (databaseType) {
			case Hive:
				Class.forName("org.apache.hive.jdbc.HiveDriver");

				break;
			case MySql:
				Class.forName("com.mysql.jdbc.Driver");

				break;
			case Impala:
				Class.forName("org.apache.hive.jdbc.HiveDriver");
				break;
			default:
				throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, databaseType.toString());
			}

			log.info("url: " + url);
			conn = DriverManager.getConnection(url, userName, password);
		} catch (Exception e) {
			log.error("数据库连接失败", e);
			throw new StatusCodeWithException(StatusCode.DATABASE_LOST, "数据库连接失败");
		}

		return conn;
	}

	public boolean testQuery(Connection conn) throws StatusCodeWithException {
		return testQuery(conn, "select 1", false);
	}

	public List<String> queryTables(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> tables = new ArrayList<>();
		try {
			ps = conn.prepareStatement("show tables");
			rs = ps.executeQuery();

			if (!rs.next()) {
				return tables;
			}
			while (rs.next()) {
				String tableName = rs.getString(1);
				tables.add(tableName);
			}

		} catch (SQLException e) {
			log.error(e);
			return tables;
		} finally {
			close(conn, ps, rs);
		}
		return tables;
	}
	
	public Map<String, Object> query(Connection conn, String sql, List<String> returnFields) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String, Object> fieldMap = new LinkedHashMap<>();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				for (String field : returnFields) {
					String value = rs.getString(field);
					fieldMap.put(field, value);
				}
			}

		} catch (SQLException e) {
			log.error(e);
			return fieldMap;
		} finally {
			close(conn, ps, rs);
		}
		return fieldMap;
	}
	

	public Map<String, String> queryTableFields(Connection conn, String tableName) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String, String> fieldMap = new LinkedHashMap<>();
		try {
			ps = conn.prepareStatement("desc " + tableName);
			rs = ps.executeQuery();

			if (!rs.next()) {
				return fieldMap;
			}
			while (rs.next()) {
				String fieldName = rs.getString(1);
				String fieldType = rs.getString(2);
				fieldMap.put(fieldName, fieldType);
			}

		} catch (SQLException e) {
			log.error(e);
			return fieldMap;
		} finally {
			close(conn, ps, rs);
		}
		return fieldMap;
	}

	public boolean testQuery(Connection conn, String sql, boolean judgeFieldNum) throws StatusCodeWithException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			if (!rs.next()) {
				return false;
			}

			// Determine the number of column fields
			if (judgeFieldNum) {
				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();

				if (columnCount < 2) {
					throw new StatusCodeWithException("The number of column fields must be greater than 1",
							StatusCode.ILLEGAL_REQUEST);
				}
			}
		} catch (SQLException e) {
			log.error(e);

//            if (e instanceof MySQLSyntaxErrorException) {
//                throw new StatusCodeWithException(StatusCode.SQL_SYNTAX_ERROR);
//            }

			return false;
		} finally {
			close(conn, ps, rs);
		}

		return true;
	}

	/**
	 * Iterate over the data read from the database
	 */
	public void readWithFieldRow(Connection conn, String sql, Consumer<List<String>> headRowConsumer,
			Consumer<JSONObject> dataRowConsumer) {
		readWithFieldRow(conn, sql, headRowConsumer, dataRowConsumer, -1);
	}

	/**
	 * Iterate over the data read from the database
	 */
	public void readWithFieldRow(Connection conn, String sql, Consumer<Map<String, Object>> dataRowConsumer) {
		readWithFieldRow(conn, sql, dataRowConsumer, -1);
	}

	/**
	 * Traversal reads the specified column from the database
	 */
	public void readWithSelectRow(Connection conn, String sql, Consumer<Map<String, Object>> dataRowConsumer,
			List<String> rows) {
		readWithSelectRow(conn, sql, dataRowConsumer, -1, rows);
	}

	/**
	 * Iterate over the data read from the database
	 */
	public void readWithSelectRow(Connection conn, String sql, Consumer<Map<String, Object>> dataRowConsumer,
			long maxReadLineCount, List<String> rows) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		long readLineCount = 0;

		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			while (rs.next() & CommonThreadPool.TASK_SWITCH) {
				// Data loading. One map corresponds to one row of data
				LinkedHashMap<String, Object> map = new LinkedHashMap<>();
				for (int i = 1; i <= columnCount; i++) {

					if (rows.contains(metaData.getColumnName(i))) {
						map.put(metaData.getColumnName(i), rs.getObject(i));
					}
				}

				dataRowConsumer.accept(map);
				readLineCount++;
				// Completes the traversal after reading the specified number of rows
				if (maxReadLineCount > 0 && readLineCount == maxReadLineCount) {
					break;
				}
			}
		} catch (SQLException e) {
			log.error(e);
		} finally {
			close(conn, ps, rs);
		}
	}

	/**
	 * Iterate over the data read from the database
	 */
	public void readWithFieldRow(Connection conn, String sql, Consumer<List<String>> headRowConsumer,
			Consumer<JSONObject> dataRowConsumer, long maxReadLineCount) {
		List<String> heads = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		long readLineCount = 0;

		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			while (rs.next()) {
				// Gets all field names
				if (heads == null) {
					heads = new ArrayList<String>();
					for (int i = 1; i <= columnCount; i++) {
						heads.add(metaData.getColumnName(i));
					}

					headRowConsumer.accept(heads);
				}

				// Data loading, one item for each row of data
				JSONObject item = new JSONObject(new LinkedHashMap<>());
				for (int i = 1; i <= columnCount; i++) {
					item.put(metaData.getColumnName(i), rs.getObject(i));
				}

				dataRowConsumer.accept(item);

				readLineCount++;
				// Completes the traversal after reading the specified number of rows
				if (maxReadLineCount > 0 && readLineCount == maxReadLineCount) {
					break;
				}
			}
		} catch (SQLException e) {
			log.error(e);
		} finally {
			close(conn, ps, rs);
		}
	}

	/**
	 * Iterate over the data read from the database
	 */
	public void readWithFieldRow(Connection conn, String sql, Consumer<Map<String, Object>> dataRowConsumer,
			long maxReadLineCount) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		long readLineCount = 0;

		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			while (rs.next()) {
				// Data loading. One map corresponds to one row of data
				LinkedHashMap<String, Object> map = new LinkedHashMap<>();
				for (int i = 1; i <= columnCount; i++) {
					map.put(metaData.getColumnName(i), rs.getObject(i));
				}

				dataRowConsumer.accept(map);

				readLineCount++;
				// Completes the traversal after reading the specified number of rows
				if (maxReadLineCount > 0 && readLineCount == maxReadLineCount) {
					break;
				}
			}
		} catch (SQLException e) {
			log.error(e);
		} finally {
			close(conn, ps, rs);
		}
	}

	/**
	 * Iterate over the data read from the database
	 */
	public void readWithFieldRow(Connection conn, String sql, Consumer<Map<String, Object>> dataRowConsumer,
			long maxReadLineCount, List<String> rowsList) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		long readLineCount = 0;

		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			while (rs.next()) {
				// Data loading. One map corresponds to one row of data
				LinkedHashMap<String, Object> map = new LinkedHashMap<>();
				for (String row : rowsList) {
					map.put(row, rs.getObject(row));
				}

				dataRowConsumer.accept(map);

				readLineCount++;
				// Completes the traversal after reading the specified number of rows
				if (maxReadLineCount > 0 && readLineCount == maxReadLineCount) {
					break;
				}
			}
		} catch (SQLException e) {
			log.error(e);
		} finally {
			close(conn, ps, rs);
		}
	}

	/**
	 * 获取查询数据的总记录数
	 */
	public long count(Connection conn, String sql) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		long totalCount = 0;

		try {
			String s = sql.replace("*", "count(*)");
			ps = conn.prepareStatement(s);
			rs = ps.executeQuery();
			while (rs.next()) {
				totalCount = rs.getLong(1);
			}
		} catch (SQLException e) {
			log.error(e);
		} finally {
			close(ps, rs);
		}

		return totalCount;
	}

	/**
	 * Gets the column header name for the query SQL data
	 *
	 * @param conn
	 * @param sql
	 * @return
	 */
	public List<String> getRowHeaders(Connection conn, String sql) {
		List<String> headers = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			if (rs.next()) {
				// Gets all field names
				if (headers == null) {
					headers = new ArrayList<String>();
					for (int i = 1; i <= columnCount; i++) {
						headers.add(metaData.getColumnName(i));
					}
				}
			}
		} catch (SQLException e) {
			log.error(e);
		} finally {
			close(ps, rs);
		}

		return headers;
	}

	public void close(Connection conn, PreparedStatement ps, ResultSet rs) {
		if (rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
				log.error(e);
			}
		if (ps != null)
			try {
				ps.close();
			} catch (SQLException e) {
				log.error(e);
			}
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				log.error(e);
			}
	}

	public void close(PreparedStatement ps, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.error(e);
			}
		}
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				log.error(e);
			}
		}
	}
}
