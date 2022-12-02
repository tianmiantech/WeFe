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
package com.welab.wefe.common.jdbc.hive;

import com.welab.wefe.common.Convert;
import com.welab.wefe.common.jdbc.base.JdbcScanner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zane.luo
 * @date 2022/11/21
 */
public class HiveScanner extends JdbcScanner {

    public HiveScanner(Connection conn, String sql, long maxReadLine) throws SQLException {
        super(conn, sql, maxReadLine);
    }

    @Override
    protected ResultSet execute() throws SQLException {
        statement = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(1000);
        if (maxReadLine > 0) {
            // hive 不支持 setLargeMaxRows
            statement.setMaxRows(Convert.toInt(maxReadLine));
        }

        return statement.executeQuery();
    }
}
