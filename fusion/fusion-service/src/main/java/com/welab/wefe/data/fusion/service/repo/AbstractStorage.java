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

package com.welab.wefe.data.fusion.service.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.sql.*;

/**
 * @author hunter.zhao
 */
public abstract class AbstractStorage extends Storage {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    @Qualifier("storageDataSource")
    @Autowired
    protected DataSource dataSource;


    /**
     * Calculate the number of nodes based on the node size
     */
    public int getCountByByteSize(String dbName, String tbName, long byteSize) throws Exception {
        return 1000;
    }

    /**
     * For a link
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
     * Check create table
     *
     * @param dbName
     * @param tbName
     * @throws SQLException
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

}
