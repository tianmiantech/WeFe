/*
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
package com.welab.wefe.common.jdbc;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.jdbc.base.DatabaseType;
import com.welab.wefe.common.jdbc.base.JdbcScanner;
import com.welab.wefe.common.jdbc.hive.HiveScanner;
import com.welab.wefe.common.jdbc.mysql.MysqlScanner;
import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 可复用的 jdbc client
 * 使用时无需关注 connection 等资源的关闭与创建
 *
 * @author zane.luo
 * @date 2022/11/21
 */
public class JdbcClient {
    protected static final Logger LOG = LoggerFactory.getLogger(JdbcClient.class);

    public static JdbcClient create(DatabaseType databaseType, String host, Integer port, String userName, String password, String dbName) {
        return new JdbcClient(databaseType, host, port, userName, password, dbName);
    }


    private DatabaseType databaseType;
    private String host;
    private Integer port;
    private String userName;
    private String password;
    private String dbName;

    public JdbcClient(DatabaseType databaseType, String host, Integer port, String userName, String password, String dbName) {
        this.databaseType = databaseType;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.dbName = dbName;
    }

    public <T> void saveBatch(String sql, List<T> models, Function<T, Object[]> model2ArrayFunc) throws Exception {
        List<Object[]> list = new ArrayList<>();
        for (T item : models) {
            list.add(model2ArrayFunc.apply(item));
        }
        saveBatch(sql, list);
    }

    /**
     * 批量写入数据
     *
     * @param sql e.g: insert into table(id,name) values(?,?)
     */
    public void saveBatch(String sql, List<Object[]> rows) throws Exception {
        long start = System.currentTimeMillis();
        Connection conn = createConnection(true);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            int count = 0;
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Object[] row = rows.get(rowIndex);
                for (int i = 0; i < row.length; i++) {
                    ps.setObject(i + 1, row[i]);
                }
                count++;
                ps.addBatch();
                if (rowIndex % 50000 == 0) {
                    ps.executeBatch();
                    ps.clearBatch();
                    LOG.info("JdbcClient saveBatch count: " + count + ", rows size = " + rows.size());
                }
            }

            ps.executeBatch();
            ps.clearBatch();
        } catch (SQLException e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        } finally {
            close(conn, ps, null);
            LOG.info("saveBatch spend：" + rows.size() + "rows " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    /**
     * @see {{@link #scan(String, Consumer, long, List)}}
     */
    public void scan(String sql, Consumer<LinkedHashMap<String, Object>> consumer) throws Exception {
        scan(sql, consumer, 0);
    }

    /**
     * @see {{@link #scan(String, Consumer, long, List)}}
     */
    public void scan(String sql, Consumer<LinkedHashMap<String, Object>> consumer, List<String> returnFields) throws Exception {
        scan(sql, consumer, returnFields);
    }

    /**
     * @see {{@link #scan(String, Consumer, long, List)}}
     */
    public void scan(String sql, Consumer<LinkedHashMap<String, Object>> consumer, long maxReadLine) throws Exception {
        scan(sql, consumer, maxReadLine, null);
    }

    /**
     * 执行查询，并流式读取。
     *
     * @param maxReadLine  最大读取行数，为 0 表示不指定。
     * @param returnFields 需要返回的字段列表，为空表示不指定。
     */
    public void scan(String sql, Consumer<LinkedHashMap<String, Object>> consumer, long maxReadLine, List<String> returnFields) throws Exception {

        JdbcScanner scanner = createScanner(sql, maxReadLine, returnFields);

        try {
            while (true) {
                LinkedHashMap<String, Object> row = scanner.readOneRow();
                if (row == null) {
                    break;
                }
                consumer.accept(row);
            }
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        } finally {
            scanner.close();
        }
    }

    public JdbcScanner createScanner(String sql) throws Exception {
        return createScanner(sql, 0);
    }

    public JdbcScanner createScanner(String sql, long maxReadLine) throws Exception {
        return createScanner(sql, maxReadLine, null);
    }

    /**
     * 创建 Scanner 对象，使用流式获取数据。
     *
     * @param sql          查询语句
     * @param maxReadLine  需要获取的最大数据量，默认为0，表示不指定。
     * @param returnFields 需要返回的字段列表，默认为 null，表示返回所有字段。
     */
    public JdbcScanner createScanner(String sql, long maxReadLine, List<String> returnFields) throws Exception {
        Connection conn = createConnection();
        switch (databaseType) {
            case MySql:
                return new MysqlScanner(conn, sql, maxReadLine, returnFields);
            case Hive:
            case Impala:
                return new HiveScanner(conn, sql, maxReadLine, returnFields);
            default:
                LOG.error("不支持的枚举项：" + databaseType);
                throw new RuntimeException("不支持的枚举项：" + databaseType);
        }
    }

    /**
     * 对于 hive，由于权限问题，有可能获取失败。
     */
    public long selectRowCount(String sql) throws Exception {
        sql = StringUtil.trim(sql, ' ', ';');

        PreparedStatement ps = null;
        ResultSet rs = null;
        long totalCount = 0;
        Connection conn = createConnection();
        try {
            ps = conn.prepareStatement("select count(*) from (" + sql + ") t");
            rs = ps.executeQuery();
            while (rs.next()) {
                totalCount = rs.getLong(1);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            close(conn, ps, rs);
        }

        return totalCount;
    }

    /**
     * Get the column header name of the query sql data
     */
    public List<String> getHeaders(String sql) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = createConnection();
        try {
            ps = conn.prepareStatement(sql);
            // 务必加上这两个设置，否则默认取全量数据内存会炸。
            ps.setFetchSize(1);
            ps.setMaxRows(1);
            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            return getHeaders(metaData);

        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            close(conn, ps, rs);
        }

        return null;
    }

    /**
     * @see {{@link #queryList(String, List)}}
     */
    public List<Map<String, Object>> queryList(String sql) throws Exception {
        return queryList(sql, null);
    }

    /**
     * 执行查询，并获取全量的查询结果。
     *
     * @param returnFields 指定需要返回的字段列表
     */
    public List<Map<String, Object>> queryList(String sql, List<String> returnFields) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        scan(sql, list::add, returnFields);
        return list;
    }

    /**
     * @see {{@link #queryOne(String, List)}}
     */
    public Map<String, Object> queryOne(String sql) throws Exception {
        return queryOne(sql, null);
    }

    /**
     * 执行查询，并获取第一条查询结果。
     *
     * @param returnFields 指定需要返回的字段列表
     */
    public Map<String, Object> queryOne(String sql, List<String> returnFields) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();

        scan(sql, list::add, 1, returnFields);

        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }


    /**
     * 获取指定表的字段列表
     */
    public List<String> listTableFields(String tableName) throws Exception {
        return getHeaders("select * from `" + tableName + "` limit 1");
    }

    public static List<String> getHeaders(ResultSetMetaData metaData) throws SQLException {
        int columnCount = metaData.getColumnCount();
        List<String> list = new ArrayList<>();

        for (int i = 1; i <= columnCount; i++) {
            String header = metaData.getColumnName(i);
            if (header.contains(".")) {
                header = header.split("\\.")[1];
            }
            list.add(header);
        }
        return list;
    }

    /**
     * 检查连接是否可用
     */
    public String test() throws StatusCodeWithException {
        return testSql("select 1");
    }

    /**
     * 检查 sql 是否正确
     */
    public String testSql(String sql) {
        try {
            execute(sql);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return e.getClass().getSimpleName() + ":" + e.getMessage();
        }
        return null;
    }

    public boolean execute(String sql) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection connection = createConnection();
        try {
            ps = connection.prepareStatement(sql);
            // 务必加上这两个设置，否则默认取全量数据内存会炸。
            ps.setFetchSize(1);
            ps.setMaxRows(1);
            return ps.execute();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } finally {
            close(connection, ps, rs);
        }
    }

    /**
     * 获取当前数据库中的所有表
     */
    public List<String> listTables() throws Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> tables = new ArrayList<>();
        Connection connection = createConnection();
        try {

            ps = connection.prepareStatement("show tables");
            rs = ps.executeQuery();
            while (rs.next()) {
                tables.add(rs.getString(1));
            }

        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            return tables;
        } finally {
            close(connection, ps, rs);
        }
        return tables;
    }

    protected Connection createConnection() throws Exception {
        return createConnection(false);
    }

    protected Connection createConnection(boolean batchModel) throws Exception {

        Connection conn;
        try {
            String url = null;
            switch (databaseType) {
                case MySql:
                    Class.forName("com.mysql.jdbc.Driver");
                    url = String.format(
                            "jdbc:mysql://%s:%d/%s"
                                    +
                                    (
                                            batchModel
                                                    ? "?rewriteBatchedStatements=true"
                                                    : ""
                                    )
                            , host, port, dbName);
                    break;
                case Hive:
                case Impala:
                    Class.forName("org.apache.hive.jdbc.HiveDriver");
                    url = String.format("jdbc:hive2://%s:%d/%s", host, port, dbName);
                    break;
                case Cassandra:
                case PgSql:
                default:
                    StatusCode.UNEXPECTED_ENUM_CASE.throwExWithFormatMsg(databaseType);
            }

            conn = DriverManager.getConnection(url, userName, password);
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            throw new StatusCodeWithException(StatusCode.DATABASE_LOST, "创建链接失败：" + e.getMessage());
        }

        return conn;
    }

    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                rs = null;
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                ps = null;
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
