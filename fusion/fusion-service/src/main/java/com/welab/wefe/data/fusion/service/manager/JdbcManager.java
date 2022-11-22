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

package com.welab.wefe.data.fusion.service.manager;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.DatabaseType;

/**
 * @author Johnny.lin
 * @Description: JDBC Management Tool
 * @date 2020/9/17
 */
public class JdbcManager {
    protected static final Logger LOG = LoggerFactory.getLogger(JdbcManager.class);

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
    public static Connection getConnection(DatabaseType databaseType, String host, Integer port, String userName
            , String password, String dbName) throws StatusCodeWithException {

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
            LOG.error("Database connection failure", e);
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
    public static Connection getConnection(DatabaseType databaseType, String url, String userName
            , String password) throws StatusCodeWithException {

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

            LOG.info("url: " + url);
            conn = DriverManager.getConnection(url, userName, password);
        } catch (Exception e) {
            LOG.error("数据库连接失败", e);
            throw new StatusCodeWithException(StatusCode.DATABASE_LOST, "数据库连接失败");
        }

        return conn;
    }

    public static boolean testQuery(Connection conn) throws StatusCodeWithException {
        return testQuery(conn, "select 1", false);
    }

    public static boolean testQuery(Connection conn, String sql, boolean judgeFieldNum) throws StatusCodeWithException {
        long start = System.currentTimeMillis();
        LOG.info("JdbcManager testQuery start: " + start);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
//            ps.setFetchSize(1);
//            ps.setMaxRows(1);
            if (!rs.next()) {
                return false;
            }

            // Determine the number of column fields
            if (judgeFieldNum) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (columnCount < 2) {
                    throw new StatusCodeWithException("列字段数必须大于1！", StatusCode.ILLEGAL_REQUEST);
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);

//            if (e instanceof MySQLSyntaxErrorException) {
//                throw new StatusCodeWithException(StatusCode.SQL_SYNTAX_ERROR);
//            }

            return false;
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            LOG.info("JdbcManager testQuery duration: " + duration);
        }

        return true;
    }

    /**
     * Traversal reads the specified column from the database
     */
    public static void readWithSelectRow(Connection conn, String sql, Consumer<Map<String, Object>> dataRowConsumer, List<String> rows) {
        readWithSelectRow(conn, sql, dataRowConsumer, -1, rows);
    }

    /**
     * Iterate over the data read from the database
     */
    public static void readWithSelectRow(Connection conn, String sql, Consumer<Map<String, Object>> dataRowConsumer, long maxReadLineCount, List<String> rows) {
        long start = System.currentTimeMillis();
        LOG.info("JdbcManager readWithSelectRow4 start: " + start);
        PreparedStatement ps = null;
        ResultSet rs = null;
        long readLineCount = 0;

        try {
//            ps = conn.prepareStatement(sql);
            // 使用流式获取数据
            ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            if (maxReadLineCount > 0) {
                ps.setLargeMaxRows(maxReadLineCount);
            }
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
            LOG.error(e.getMessage(), e);
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            LOG.info("JdbcManager readWithSelectRow4 duration: " + duration);
        }
    }
    
    /**
     * Iterate over the data read from the database
     */
    public static void readWithFieldRow(Connection conn, String sql, Consumer<Map<String, Object>> dataRowConsumer, long maxReadLineCount, List<String> rowsList) {
        long start = System.currentTimeMillis();
        LOG.info("JdbcManager readWithFieldRow1 start: " + start);
        PreparedStatement ps = null;
        ResultSet rs = null;
        long readLineCount = 0;

        try {
            sql = sql.replace(";","");
            if (!sql.contains("limit")) {
                sql = sql + " limit 10";
            }
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                // Data loading. One map corresponds to one row of data
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                for (String row : rowsList
                ) {
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
            LOG.error(e.getMessage(), e);
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            LOG.info("JdbcManager readWithFieldRow1 duration: " + duration);
        }
    }

    
    /**
     * 获取查询数据的总记录数
     */
    public static long count(Connection conn, String sql) throws Exception {
        long start = System.currentTimeMillis();
        LOG.info("JdbcManager count start: " + start);
        PreparedStatement ps = null;
        ResultSet rs = null;
        long totalCount = 0;

        try {
            ps = conn.prepareStatement("select count(*) from (" + sql + ") t");
            rs = ps.executeQuery();
            while (rs.next()) {
                totalCount = rs.getLong(1);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } finally {
            close(ps, rs);
            long duration = System.currentTimeMillis() - start;
            LOG.info("JdbcManager count duration: " + duration);
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
    public static List<String> getRowHeaders(Connection conn, String sql) {
        long start = System.currentTimeMillis();
        LOG.info("JdbcManager getRowHeaders start: " + start);
        List<String> headers = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            sql = sql.replace(";","");
            if (!sql.contains("limit")) {
                sql = sql + " limit 1";
            }
            ps = conn.prepareStatement(sql);
            // 务必加上这两个设置，否则默认取全量数据内存会炸。
            ps.setFetchSize(1);
            ps.setMaxRows(1);
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
            LOG.error(e.getMessage(), e);
        } finally {
            close(ps, rs);
            long duration = System.currentTimeMillis() - start;
            LOG.info("JdbcManager getRowHeaders duration: " + duration);
        }

        return headers;
    }

    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        if (rs != null) try {
            rs.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        if (ps != null) try {
            ps.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        if (conn != null) try {
            conn.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static void close(PreparedStatement ps, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
