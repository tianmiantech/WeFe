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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.shaded.json.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.jdbc.base.DatabaseType;


/**
 * jdbc management tool
 *
 * @deprecated 建议使用 JdbcClient
 * @author Johnny.lin
 */
@Deprecated
public class JdbcManager {
    private static final Logger log = LoggerFactory.getLogger(JdbcManager.class);

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
            case Doris:
            case MySql:
                url = String.format(
                        "jdbc:mysql://%s:%d/%s?characterEncoding=utf8&useSSL=false&rewriteBatchedStatements=true", host,
                        port, dbName);
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
            case Doris:
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

    public List<String> queryTables(Connection conn) {
        long start = System.currentTimeMillis();
        log.info("JdbcManager queryTables start: " + start);
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> tables = new ArrayList<>();
        try {
            ps = conn.prepareStatement("show tables");
            rs = ps.executeQuery();
            while (rs.next()) {
                String tableName = rs.getString(1);
                tables.add(tableName);
            }

        } catch (SQLException e) {
            log.error("queryTables error", e);
            return tables;
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager queryTables duration: " + duration);
        }
        return tables;
    }

    public Map<String, String> batchQuerySql(Connection conn, Map<String, String> sqlMap, List<String> returnFields) {
        long start = System.currentTimeMillis();
        log.info("JdbcManager query start: " + start);
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, String> resultMap = new LinkedHashMap<>();
        try {
            for (Map.Entry<String, String> sqlMapEntry : sqlMap.entrySet()) {
                Map<String, String> fieldMap = new LinkedHashMap<>();
                String key = sqlMapEntry.getKey();
                ps = conn.prepareStatement(sqlMapEntry.getValue());
                rs = ps.executeQuery();
                while (rs.next()) {
                    for (String field : returnFields) {
                        String value = rs.getString(field);
                        fieldMap.put(field, value);
                    }
                }
                if (fieldMap == null || fieldMap.isEmpty()) {
                    fieldMap = new HashMap<>();
                    fieldMap.put("rand", "thisisemptyresult");
                }
                resultMap.put(key, JSONObject.toJSONString(fieldMap));
            }
        } catch (SQLException e) {
            log.error("query error", e);
            return resultMap;
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager query duration: " + duration);
        }
        return resultMap;
    }
    
    public Map<String, String> queryOne(Connection conn, String sql, List<String> returnFields) {
        long start = System.currentTimeMillis();
        log.info("JdbcManager query start: " + start);
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, String> fieldMap = new LinkedHashMap<>();
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
            log.error("query error", e);
            return fieldMap;
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager query duration: " + duration);
        }
        return fieldMap;
    }

    // sql SELECT xxx FROM MY_TABLE WHERE a=? and b=? limit 1;
    public List<Map<String, String>> queryListByConditions(Connection conn, String sql,
            List<Map<String, Object>> conditionFieldValues, List<String> returnFields) {
        long start = System.currentTimeMillis();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, String>> result = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            int times = conditionFieldValues.size();
            for (int i = 0; i < times; i++) {
                Map<String, Object> conditionFieldValue = conditionFieldValues.get(i);
                int count = 1;
                for (Map.Entry<String, Object> entry : conditionFieldValue.entrySet()) {
                    ps.setObject(count, entry.getValue());
                    count++;
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, String> fieldMap = new LinkedHashMap<>();
                    for (String field : returnFields) {
                        String value = rs.getString(field);
                        fieldMap.put(field, value);
                    }
                    result.add(fieldMap);
                }
            }
        } catch (SQLException e) {
            log.error("queryListByIds error", e);
            return result;
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager queryListByIds duration: " + duration);
        }
        return result;
    }

    // sql SELECT xxx FROM MY_TABLE WHERE ID IN (?,?,?,?,?,?,?,?,?,?))
    public List<Map<String, String>> queryListByIds(Connection conn, String sql, List<String> ids,
            List<String> returnFields) {
        long start = System.currentTimeMillis();
        // 每10个ID批量查询一次
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, String>> result = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            int index = 0;// ID的下标
            int times = (ids.size() - 1) / 10 + 1; // 外循环次数
            for (int i = 0; i < times; i++) {
                for (int j = 0; j < 10; j++) {
                    // 如果不足10个（最后一次时）ID，以不可能的ID凑数
                    ps.setString(j + 1, (index < ids.size() ? ids.get(index) : -j + ""));
                    index++;
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, String> fieldMap = new LinkedHashMap<>();
                    for (String field : returnFields) {
                        String value = rs.getString(field);
                        fieldMap.put(field, value);
                    }
                    result.add(fieldMap);
                }
            }
        } catch (SQLException e) {
            log.error("queryListByIds error", e);
            return result;
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager queryListByIds duration: " + duration);
        }
        return result;
    }

    public List<Map<String, String>> queryList(Connection conn, String sql, List<String> returnFields) throws StatusCodeWithException {
        long start = System.currentTimeMillis();
        log.info("JdbcManager queryList sql: " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, String>> result = new ArrayList<>();
        try {
            // 使用流式获取数据
            ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            ps.setFetchDirection(ResultSet.FETCH_REVERSE);
            ps.setQueryTimeout(10 * 60); // 10 min
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> fieldMap = new LinkedHashMap<>();
                for (String field : returnFields) {
                    String value = rs.getString(field);
                    fieldMap.put(field, value);
                }
                result.add(fieldMap);
            }
        } catch (SQLException e) {
            log.error("queryList error", e);
            throw new StatusCodeWithException(StatusCode.SQL_ERROR, e.getMessage());
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager queryList duration: " + duration);
        }
        return result;
    }

    public Map<String, String> queryTableFields(Connection conn, String tableName) {
        long start = System.currentTimeMillis();
        log.info("JdbcManager queryTableFields start: " + start);
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, String> fieldMap = new LinkedHashMap<>();
        try {
            ps = conn.prepareStatement("desc " + tableName);
            rs = ps.executeQuery();
            while (rs.next()) {
                String fieldName = rs.getString(1);
                String fieldType = rs.getString(2);
                fieldMap.put(fieldName, fieldType);
            }

        } catch (SQLException e) {
            log.error("queryTableFields error", e);
            return fieldMap;
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager queryTableFields duration: " + duration);
        }
        return fieldMap;
    }

    public boolean execute(Connection conn, String sql) throws StatusCodeWithException {
        long start = System.currentTimeMillis();
        log.info("JdbcManager execute start: " + start);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            close(conn, ps, null);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager execute duration: " + duration);
        }
        return true;
    }

    public void batchInsert(Connection conn, String sql, Set<String> ids) throws SQLException {
        long start = System.currentTimeMillis();
        log.info("JdbcManager batchInsert ids size = " + ids.size());
        conn.setAutoCommit(false);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql); // 批量插入时ps对象必须放到for循环外面
            int count = 0;
            for (String s : ids) {
                ps.setString(1, s);
                ps.addBatch();
                count++;
                // 每1000条记录插入一次
                if (count % 50000 == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                    log.info("JdbcManager batchInsert count: " + count + ", ids size = " + ids.size());
                }
            }
            // 剩余数量不足1000
            ps.executeBatch();
            conn.commit();
            ps.clearBatch();
        } catch (SQLException e) {
            log.error("batchInsert error", e);
        } finally {
            close(conn, ps, null);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager batchInsert duration: " + duration);
        }
    }

    public boolean update(Connection conn, String sql) throws StatusCodeWithException {
        long start = System.currentTimeMillis();
        log.info("JdbcManager update start: " + start);
        PreparedStatement ps = null;
        int rs = 0;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeUpdate();
            return rs != 0;
        } catch (SQLException e) {
            log.error("testQuery error", e);
            return false;
        } finally {
            close(conn, ps, null);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager testQuery duration: " + duration);
        }
    }

    public boolean testQuery(Connection conn, String sql) throws StatusCodeWithException {
        long start = System.currentTimeMillis();
        log.info("JdbcManager testQuery start: " + start);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setFetchSize(1);
            ps.setMaxRows(1);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return false;
            }
        } catch (SQLException e) {
            log.error("testQuery error", e);
            return false;
        } finally {
            close(conn, ps, rs);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager testQuery duration: " + duration);
        }

        return true;
    }

    /**
     * 获取查询数据的总记录数
     */
    public long count(Connection conn, String sql) {
        long start = System.currentTimeMillis();
        log.info("JdbcManager count start");
        PreparedStatement ps = null;
        ResultSet rs = null;
        long totalCount = 0;

        try {
            String s = sql.replace("*", "count(*)");
            ps = conn.prepareStatement(s);
            ps.setFetchSize(1);
            ps.setMaxRows(1);
            rs = ps.executeQuery();
            while (rs.next()) {
                totalCount = rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error("count error", e);
        } finally {
            close(ps, rs);
            long duration = System.currentTimeMillis() - start;
            log.info("JdbcManager count duration: " + duration);
        }

        return totalCount;
    }

    public void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        if (rs != null)
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("rs.close error", e);
            }
        if (ps != null)
            try {
                ps.close();
            } catch (SQLException e) {
                log.error("ps.close error", e);
            }
        if (conn != null)
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("conn.close error", e);
            }
    }

    public void close(PreparedStatement ps, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("rs close error", e);
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                log.error("ps close error", e);
            }
        }
    }
}
