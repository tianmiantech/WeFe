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
package com.welab.wefe.common.jdbc.base;

import com.welab.wefe.common.jdbc.JdbcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author zane.luo
 * @date 2022/11/21
 */
public abstract class JdbcScanner implements Closeable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected Connection conn;
    protected String sql;
    protected long maxReadLine;
    protected PreparedStatement statement = null;
    protected ResultSet resultSet = null;
    private final List<String> headers;


    protected abstract ResultSet execute() throws SQLException;


    public JdbcScanner(Connection conn, String sql, long maxReadLine) throws SQLException {
        this(conn, sql, maxReadLine, null);
    }

    public JdbcScanner(Connection conn, String sql, long maxReadLine, List<String> returnFields) throws SQLException {
        this.conn = conn;
        this.sql = sql;
        this.maxReadLine = maxReadLine;
        this.resultSet = execute();

        // 如果未指定返回字段，返回全部字段。
        if (CollectionUtils.isEmpty(returnFields)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            this.headers = JdbcClient.getHeaders(metaData);
        } else {
            this.headers = returnFields;
        }
    }


    public LinkedHashMap<String, Object> readOneRow() throws Exception {
        if (resultSet.next()) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            for (String header : headers) {
                map.put(header, resultSet.getObject(header));
            }
            return map;
        } else {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        JdbcClient.close(conn, statement, resultSet);
    }
}
