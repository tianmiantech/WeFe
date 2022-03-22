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
package com.welab.wefe.common.data.mysql.config;

import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.*;
import com.alibaba.druid.sql.SQLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * sql 监视器
 *
 * @author zane
 * @date 2022/3/22
 */
public class SqlMonitor extends FilterEventAdapter {
    protected static final Logger LOG = LoggerFactory.getLogger(SqlMonitor.class);
    private SQLUtils.FormatOption statementSqlFormatOption = new SQLUtils.FormatOption(false, true);

    protected void statementLog(String message) {
        LOG.info(message);
    }

    protected void statementLog(String message, Throwable error) {
        LOG.error(message, error);
    }


    /**
     * 在 statement.execute() 之后被调用
     */
    @Override
    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean firstResult) {
        statement.setLastExecuteTimeNano();
        double nanos = statement.getLastExecuteTimeNano();
        double millis = nanos / (1000 * 1000);

        statementLog("{conn-" + statement.getConnectionProxy().getId() + ", " + stmtId(statement) + "} executed. "
                + millis + " millis. " + sql);
    }

    @Override
    protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet) {
        statement.setLastExecuteTimeNano();
        double nanos = statement.getLastExecuteTimeNano();
        double millis = nanos / (1000 * 1000);

        statementLog("{conn-" + statement.getConnectionProxy().getId() + ", " + stmtId(statement) + ", rs-"
                + resultSet.getId() + "} query executed. " + millis + " millis. " + sql);

    }

    @Override
    protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {
        statement.setLastExecuteTimeNano();
        double nanos = statement.getLastExecuteTimeNano();
        double millis = nanos / (1000 * 1000);

        statementLog("{conn-" + statement.getConnectionProxy().getId() + ", " + stmtId(statement)
                + "} update executed. effort " + updateCount + ". " + millis + " millis. " + sql);
    }

    @Override
    protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {
        String sql;
        if (statement instanceof PreparedStatementProxy) {
            sql = ((PreparedStatementProxy) statement).getSql();
        } else {
            sql = statement.getBatchSql();
        }

        statement.setLastExecuteTimeNano();
        double nanos = statement.getLastExecuteTimeNano();
        double millis = nanos / (1000 * 1000);

        statementLog("{conn-" + statement.getConnectionProxy().getId() + ", " + stmtId(statement)
                + "} batch executed. " + millis + " millis. " + sql);
    }

    @Override
    protected void statement_executeErrorAfter(StatementProxy statement, String sql, Throwable error) {
        int parametersSize = statement.getParametersSize();
        if (parametersSize > 0) {
            List<Object> parameters = new ArrayList<Object>(parametersSize);
            for (int i = 0; i < parametersSize; ++i) {
                JdbcParameter jdbcParam = statement.getParameter(i);
                parameters.add(jdbcParam != null
                        ? jdbcParam.getValue()
                        : null);
            }
            String dbType = statement.getConnectionProxy().getDirectDataSource().getDbType();
            String formattedSql = SQLUtils.format(sql, dbType, parameters, this.statementSqlFormatOption);
            statementLog("{conn-" + statement.getConnectionProxy().getId()
                            + ", " + stmtId(statement)
                            + "} execute error. " + formattedSql
                    , error);
        } else {
            statementLog("{conn-" + statement.getConnectionProxy().getId() + ", " + stmtId(statement)
                    + "} execute error. " + sql, error);
        }
    }

    private String stmtId(ResultSetProxy resultSet) {
        return stmtId(resultSet.getStatementProxy());
    }

    private String stmtId(StatementProxy statement) {
        StringBuffer buf = new StringBuffer();
        if (statement instanceof CallableStatementProxy) {
            buf.append("cstmt-");
        } else if (statement instanceof PreparedStatementProxy) {
            buf.append("pstmt-");
        } else {
            buf.append("stmt-");
        }
        buf.append(statement.getId());

        return buf.toString();
    }
}
