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

package com.welab.wefe.common.data.storage.repo;

import com.welab.wefe.common.data.storage.model.DataItemModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * @author yuxin.zhang
 */
public abstract class AbstractStorage extends Storage {

    @Qualifier("storageDataSource")
    @Autowired
    protected DataSource dataSource;


    @Override
    public int getCountByByteSize(String dbName, String tbName, long byteSize) throws Exception {
        return 1000;
    }

    /**
     * get database Connection
     */
    protected Connection getConnection() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
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
                e.printStackTrace();
            }
        }
        if (stat != null) {
            try {
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
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

    @Override
    public <K, V> void putAll(List<DataItemModel<K, V>> list, Map<String, Object> args) {

    }
}
