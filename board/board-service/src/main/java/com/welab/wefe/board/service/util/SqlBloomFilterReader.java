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
import com.welab.wefe.common.jdbc.JdbcClient;
import com.welab.wefe.common.jdbc.base.JdbcScanner;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Used to read bloom_filter in Sql format
 *
 * @author jacky.jiang
 */
public class SqlBloomFilterReader extends AbstractBloomFilterReader {
    protected static final Logger LOG = LoggerFactory.getLogger(SqlBloomFilterReader.class);
    private long totalRowCount = -1;
    private List<String> headers;
    private final JdbcClient jdbcClient;
    private final String sql;
    private JdbcScanner scanner;

    public SqlBloomFilterReader(List<BloomFilterColumnInputModel> metadataList, JdbcClient jdbcClient, String sql) throws Exception {
        super(metadataList);
        this.jdbcClient = jdbcClient;
        this.sql = sql;
        this.scanner = jdbcClient.createScanner(sql);

    }

    @Override
    protected List<String> doGetHeader() throws Exception {
        if (!CollectionUtils.isEmpty(this.headers)) {
            return this.headers;
        }

        this.headers = jdbcClient.getHeaders(sql);
        return this.headers;
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

    protected LinkedHashMap<String, Object> readOneRow() throws Exception {
        return scanner.readOneRow();
    }

    @Override
    public void close() throws IOException {
        if (scanner != null) {
            scanner.close();
        }
    }
}
