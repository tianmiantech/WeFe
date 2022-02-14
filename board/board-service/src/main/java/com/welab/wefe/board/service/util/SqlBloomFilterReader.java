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

package com.welab.wefe.board.service.util;

import com.welab.wefe.board.service.dto.fusion.BloomFilterColumnInputModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Used to read bloom_filter in Sql format
 *
 * @author jacky.jiang
 */
public class SqlBloomFilterReader extends AbstractBloomFilterReader {
    protected static final Logger LOG = LoggerFactory.getLogger(SqlBloomFilterReader.class);
    private long totalRowCount;
    private List<String> headers;
    private final Connection conn;
    private final String sql;
    private PreparedStatement ps = null;
    private ResultSet rs = null;
    private ResultSetMetaData metaData;
    private int columnCount;

    public SqlBloomFilterReader(Connection conn, String sql) throws StatusCodeWithException {
        this(null, conn, sql);
    }

    public SqlBloomFilterReader(List<BloomFilterColumnInputModel> metadataList, Connection conn, String sql) throws StatusCodeWithException {
        super(metadataList);
        this.conn = conn;
        this.sql = sql;

        try {
            this.ps = conn.prepareStatement(sql);
            this.rs = ps.executeQuery();
            this.metaData = rs.getMetaData();
            this.columnCount = metaData.getColumnCount();
        } catch (SQLException e) {
            StatusCode.SQL_ERROR.throwException(e);
        }

    }

    @Override
    protected List<String> doGetHeader() throws Exception {
        if (!CollectionUtils.isEmpty(this.headers)) {
            return this.headers;
        }

        this.headers = JdbcManager.getRowHeaders(conn, sql);
        return this.headers;
    }

    @Override
    public long getTotalDataRowCount() {
        if (totalRowCount > 0) {
            return totalRowCount;
        }

        totalRowCount = JdbcManager.count(conn, sql);
        return totalRowCount;
    }

    @Override
    protected LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException {
        try {
            if (!rs.next()) {
                return null;
            }

            // Data loading, a map corresponds to a row of data
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                if (rs.getObject(i) == null) {
                    map.put(metaData.getColumnName(i), "");
                } else {
                    map.put(metaData.getColumnName(i), rs.getObject(i));
                }
            }
            return map;
        } catch (SQLException e) {
            StatusCode.SQL_ERROR.throwException(e.getClass().getSimpleName() + " " + e.getMessage());
            return null;
        }
    }


    @Override
    public void close() throws IOException {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOG.error("ResultSet is null" + e);
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                LOG.error("PreparedStatement is null" + e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOG.error("Connection is null" + e);
            }
        }
    }
}
