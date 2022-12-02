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
package com.welab.wefe.common.jdbc.mysql;

import com.welab.wefe.common.jdbc.base.JdbcScanner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/*
 *     protected boolean createStreamingResultSet() {
 *         return ((this.query.getResultType() == Type.FORWARD_ONLY) && (this.resultSetConcurrency == java.sql.ResultSet.CONCUR_READ_ONLY)
 *                 && (this.query.getResultFetchSize() == Integer.MIN_VALUE));
 *     }
 *
 *     研究驱动源码可知，想要使用流式获取数据，需要满足以上条件。
 *
 * @author zane.luo
 * @date 2022/11/21
 */
public class MysqlScanner extends JdbcScanner {
    public MysqlScanner(Connection conn, String sql, long maxReadLine) throws SQLException {
        super(conn, sql, maxReadLine);
    }

    public MysqlScanner(Connection conn, String sql, long maxReadLine, List<String> returnFields) throws SQLException {
        super(conn, sql, maxReadLine, returnFields);
    }

    @Override
    protected ResultSet execute() throws SQLException {
        statement = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(Integer.MIN_VALUE);
        if (maxReadLine > 0) {
            statement.setLargeMaxRows(maxReadLine);
        }

        return statement.executeQuery();
    }
}
