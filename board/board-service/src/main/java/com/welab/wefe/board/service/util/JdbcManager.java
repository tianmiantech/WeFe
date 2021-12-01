/**
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

package com.welab.wefe.board.service.util;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * jdbc management tool
 *
 * @author Johnny.lin
 */
public class JdbcManager {
    private static final Logger log = Logger.getLogger(JdbcManager.class);

    private JdbcManager() {

    }

    public static Connection getConnection(DatabaseType databaseType, String host, Integer port, String userName
            , String password, String dbName) throws StatusCodeWithException {

        Connection conn;
        try {
            String url;
            switch (databaseType) {
                case Hive:
                    Class.forName("org.apache.hive.jdbc.HiveDriver");
                    url = String.format("jdbc:hive2://%s:%d/%s", host, port, dbName);
                    break;
                case MySql:
                    Class.forName("com.mysql.jdbc.Driver");
                    url = String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
                    break;
                case Impala:
                    Class.forName("org.apache.hive.jdbc.HiveDriver");
                    url = String.format("jdbc:hive2://%s:%d/%s", host, port, dbName);
                    break;
                case Cassandra:
                case PgSql:
                default:
                    throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, databaseType.toString());
            }

            log.info("url: " + url);
            conn = DriverManager.getConnection(url, userName, password);
        } catch (Exception e) {
            log.error("数据库连接失败", e);
            throw new StatusCodeWithException("数据库连接失败：" + e.getMessage(), StatusCode.DATABASE_LOST);
        }

        return conn;
    }

    public static boolean testQuery(Connection conn) throws StatusCodeWithException {
        return testQuery(conn, "select 1", false);
    }

    public static boolean testQuery(Connection conn, String sql, boolean judgeFieldNum) throws StatusCodeWithException {
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
                    throw new StatusCodeWithException("列字段数量必须大于1", StatusCode.ILLEGAL_REQUEST);
                }
            }
        } catch (SQLException e) {
            log.error(e);
            return false;
        } finally {
            close(conn, ps, rs);
        }

        return true;
    }

    /**
     * Traverse the data read from the database
     */
    public static void readWithFieldRow(Connection conn, String sql, Consumer<LinkedHashMap<String, Object>> dataRowConsumer, long maxReadLineCount) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        long readLineCount = 0;

        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                // Data loading, a map corresponds to a row of data
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String header = metaData.getColumnName(i);
                    if (header.contains(".")) {
                        header = header.split("\\.")[1];
                    }
                    map.put(header, rs.getObject(i));
                }

                dataRowConsumer.accept(map);

                readLineCount++;
                // End the traversal after reading the specified number of rows
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
     * Get the total number of records of query data
     */
    public static long count(Connection conn, String sql) {
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
            log.error(e);
        } finally {
            close(ps, rs);
        }

        return totalCount;
    }

    /**
     * Get the column header name of the query sql data
     */
    public static List<String> getRowHeaders(Connection conn, String sql) {
        List<String> headers = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            if (rs.next()) {
                // Get all field names
                headers = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String header = metaData.getColumnName(i);
                    if (header.contains(".")) {
                        header = header.split("\\.")[1];
                    }
                    headers.add(header);
                }
            }
        } catch (SQLException e) {
            log.error(e);
        } finally {
            close(ps, rs);
        }

        return headers;
    }

    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
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
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    public static void close(PreparedStatement ps, ResultSet rs) {
        close(null, ps, rs);
    }
}
