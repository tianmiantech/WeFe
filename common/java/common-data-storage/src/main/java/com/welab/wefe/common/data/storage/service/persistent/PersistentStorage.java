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
package com.welab.wefe.common.data.storage.service.persistent;

import com.alibaba.druid.pool.DruidDataSource;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.data.storage.service.persistent.base.DataSourceConfig;
import com.welab.wefe.common.data.storage.service.persistent.clickhouse.ClickhouseConfig;
import com.welab.wefe.common.data.storage.service.persistent.clickhouse.ClickhouseStorage;
import com.welab.wefe.common.data.storage.service.persistent.mysql.MysqlConfig;
import com.welab.wefe.common.data.storage.service.persistent.mysql.MysqlStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

/**
 * 持久化存储（persistent storage）：默认以 clickhouse 实现，用于持久化储存数据集。
 *
 * @author zane
 * @date 2022/5/24
 */
public abstract class PersistentStorage {
    protected static Logger log = LoggerFactory.getLogger(PersistentStorage.class);


    // region abstract method

    public abstract void put(String dbName, String tbName, DataItemModel model) throws Exception;

    public abstract <K, V> void putAll(String dbName, String tbName, List<DataItemModel<K, V>> data) throws Exception;

    public abstract DataItemModel get(String dbName, String tbName, String key) throws Exception;

    public abstract List<DataItemModel> collect(String dbName, String tbName) throws Exception;

    public abstract void delete(String dbName, String tbName, String key) throws Exception;

    public abstract List<DataItemModel<byte[], byte[]>> collectBytes(String dbName, String tbName) throws Exception;

    public abstract List<DataItemModel> take(String dbName, String tbName, int size) throws Exception;


    public abstract PageOutputModel getPage(String dbName, String tbName, PageInputModel pageInputModel) throws Exception;

    public abstract PageOutputModel<byte[], byte[]> getPageBytes(String dbName, String tbName, PageInputModel pageInputModel) throws Exception;

    public abstract int count(String dbName, String tbName) throws Exception;

    public abstract void dropTB(String dbName, String tbName) throws Exception;

    public abstract void dropDB(String dbName) throws Exception;

    public abstract int getAddBatchSize(int columnCount);

    public int getCountByByteSize(String dbName, String tbName, long byteSize) throws Exception {
        return 1000;
    }

    public abstract boolean isExists(String dbName, String tbName) throws SQLException;

    protected abstract String validationQuery();
    // endregion

    private static PersistentStorage storage;

    protected PersistentStorage() {
    }

    public DruidDataSource dataSource;

    public volatile boolean inited = false;


    /**
     * 初始化对象
     * <p>
     * 当配置信息变化时，重新初始化即可刷新对象。
     */
    public synchronized static boolean init(ClickhouseConfig config) throws SQLException {
        try {
            if (storage != null && storage.dataSource != null) {
                storage.dataSource.close();
                storage.inited = false;
            }
            storage = new ClickhouseStorage(config);
            storage.dataSource = buildDruidDataSource(config);
            storage.checkConnection();
            storage.inited = true;
        } catch (SQLException e) {
            log.error("storage clickhouse init failed", e);
        }
        return storage.inited;
    }

    public synchronized static boolean init(MysqlConfig config) throws SQLException {
        try {
            if (storage != null && storage.dataSource != null) {
                storage.dataSource.close();
                storage.inited = false;
            }

            if (config == null) {
                return false;
            }

            storage = new MysqlStorage(config);
            storage.dataSource = buildDruidDataSource(config);
            storage.checkConnection();
            storage.inited = true;
        } catch (SQLException e) {
            log.error("storage mysql init failed", e);
        }
        return storage.inited;
    }

    public static boolean inited() {
        return storage.inited;
    }

    public static PersistentStorage getInstance() {
        return storage;
    }

    public static void main(String[] args) throws Exception {
        PersistentStorage.init(new ClickhouseConfig("127.0.0.1", 8123, "user", "pasdword"));
        PersistentStorage.getInstance().put("wefe", "test", new DataItemModel("a", "123"));
        List<DataItemModel> list = PersistentStorage.getInstance().collect("wefe", "test");
    }


    static DruidDataSource buildDruidDataSource(DataSourceConfig dataSourceConfig) {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(dataSourceConfig.getUrl());
        datasource.setDriverClassName(dataSourceConfig.getDriverClassName());
        datasource.setInitialSize(dataSourceConfig.getInitialSize());
        datasource.setMinIdle(dataSourceConfig.getMinIdle());
        datasource.setMaxActive(dataSourceConfig.getMaxActive());
        datasource.setMaxWait(dataSourceConfig.getMaxWait());
        datasource.setPassword(dataSourceConfig.getPassword());
        datasource.setUsername(dataSourceConfig.getUsername());
        datasource.setTestWhileIdle(dataSourceConfig.isTestWhileIdle());
        datasource.setValidationQuery(dataSourceConfig.getValidationQuery());
        datasource.setTimeBetweenEvictionRunsMillis(dataSourceConfig.getTimeBetweenEvictionRunsMillis());
        datasource.setMinEvictableIdleTimeMillis(dataSourceConfig.getMinEvictableIdleTimeMillis());
        datasource.setRemoveAbandoned(dataSourceConfig.isRemoveAbandoned());
        datasource.setRemoveAbandonedTimeout(dataSourceConfig.getRemoveAbandonedTimeout());
        datasource.setLogAbandoned(dataSourceConfig.isLogAbandoned());
        return datasource;

    }


    /**
     * get database Connection
     */
    protected Connection getConnection() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return conn;
    }

    /**
     * Release resources
     */
    protected void close(Statement stat, Connection conn) {
        close(null, stat, conn);
    }

    /**
     * Release resources
     */
    protected void close(ResultSet rs, Statement stat, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (stat != null) {
            try {
                stat.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    protected String formatTableName(String dbName, String tbName) {
        return String.format("`%s`.`%s`", dbName, tbName);
    }

    /**
     * Check and create table
     */
    protected void checkTB(String dbName, String tbName) throws SQLException {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getConnection();
            String sql = "CREATE TABLE IF NOT EXISTS " + formatTableName(dbName, tbName) + "(`eventDate` Date, `k` String, `v` String, `id` String) ENGINE = MergeTree() PARTITION BY toDate(eventDate) ORDER BY (id) SETTINGS index_granularity = 8192";
            statement = conn.prepareStatement(sql);
            statement.execute();
        } finally {
            close(statement, conn);
        }
    }


    protected void checkConnection() throws SQLException {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getConnection();
            String sql = validationQuery();
            statement = conn.prepareStatement(sql);
            statement.execute();
        } finally {
            close(statement, conn);
        }
    }
}
