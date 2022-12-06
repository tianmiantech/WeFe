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

import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnInputModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.jdbc.JdbcClient;
import com.welab.wefe.common.jdbc.base.JdbcScanner;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Used to read data sets in Sql format
 *
 * @author jacky.jiang
 */
public class SqlTableDataSetReader extends AbstractTableDataSetReader {
    protected static final Logger LOG = LoggerFactory.getLogger(SqlTableDataSetReader.class);

    private long totalRowCount = -1;
    private final JdbcClient jdbcClient;
    private final String sql;
    private JdbcScanner scanner;

    public SqlTableDataSetReader(List<DataSetColumnInputModel> metadataList, JdbcClient jdbcClient, String sql) throws Exception {
        super(metadataList);
        this.jdbcClient = jdbcClient;
        this.sql = sql;
        this.scanner = jdbcClient.createScanner(sql, 0);

    }

    @Override
    protected List<String> doGetHeader() throws Exception {
        if (!CollectionUtils.isEmpty(this.header)) {
            return this.header;
        }

        this.header = jdbcClient.getHeaders(sql);
        return this.header;
    }

    @Override
    public long getTotalDataRowCount() {
        if (totalRowCount > -1) {
            return totalRowCount;
        }

        try {
            totalRowCount = jdbcClient.selectRowCount(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return totalRowCount;
    }

    @Override
    protected LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException {
        try {
            return scanner.readOneRow();
        } catch (Exception e) {
            StatusCode.SQL_ERROR.throwException(e.getClass().getSimpleName() + " " + e.getMessage());
            return null;
        }
    }


    @Override
    public void close() throws IOException {
        if (scanner != null) {
            scanner.close();
        }
    }
}
