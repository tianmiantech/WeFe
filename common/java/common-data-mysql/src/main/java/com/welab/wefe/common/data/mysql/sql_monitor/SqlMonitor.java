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
package com.welab.wefe.common.data.mysql.sql_monitor;

import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sql 监视器
 *
 * @author zane
 * @date 2022/3/22
 */
public class SqlMonitor extends FilterEventAdapter {
    private SQLUtils.FormatOption statementSqlFormatOption = new SQLUtils.FormatOption(false, true);

    /**
     * 慢 sql 的判定标准
     */
    private static final int SLOW_SQL_LIMIT = 100;
    /**
     * 慢 sql 的最大捕获数量
     */
    private static final int MAX_SLOW_SQL_CATCH_COUNT = 100;
    /**
     * 慢 sql 的集合
     */
    public static ConcurrentHashMap<Integer, SlowSql> SLOW_SQL_MAP = new ConcurrentHashMap<>();
    /**
     * 执行失败 sql 的最大捕获数量
     */
    private static final int MAX_ERROR_SQL_CATCH_COUNT = 100;
    /**
     * 执行失败 sql 的集合
     */
    public static LinkedHashMap<Integer, ErrorSql> ERROR_SQL_MAP = new LinkedHashMap<>();

    /**
     * 获取执行失败的 sql 列表
     */
    public static Collection<ErrorSql> getErrorSqlList() {
        return ERROR_SQL_MAP.values();
    }

    /**
     * 获取慢 sql 列表
     */
    public static List<SlowSql> getSlowSqlList() {
        ArrayList<SlowSql> list = new ArrayList<>(SLOW_SQL_MAP.values());
        list.sort(Comparator.comparingLong(x -> x.avgSpend));
        Collections.reverse(list);

        return list;
    }

    /**
     * 捕获执行失败的 sql
     */
    private void catchErrorSql(String sql, Throwable error) {
        int sqlHash = sql.hashCode();
        ErrorSql errorSql;
        if (ERROR_SQL_MAP.containsKey(sqlHash)) {
            errorSql = ERROR_SQL_MAP.get(sqlHash);
        } else {

            // 如果执行失败的 sql 数量超过上限，则不再捕获。
            if (ERROR_SQL_MAP.size() >= MAX_ERROR_SQL_CATCH_COUNT) {
                return;
            }

            errorSql = new ErrorSql(sql);
            ERROR_SQL_MAP.put(sqlHash, errorSql);
        }
        errorSql.catchOnce(error);
    }

    /**
     * 捕获慢 sql
     */
    private void catchSlowSql(StatementProxy statement, String sql) {
        statement.setLastExecuteTimeNano();
        long spend = statement.getLastExecuteTimeNano() / (1000 * 1000);

        // 如果未达到慢 sql 判定标准，则不捕获。
        if (spend < SLOW_SQL_LIMIT) {
            return;
        }

        int sqlHash = sql.hashCode();

        SlowSql slowSql;
        if (SLOW_SQL_MAP.containsKey(sqlHash)) {
            slowSql = SLOW_SQL_MAP.get(sqlHash);
        } else {

            // 如果捕获的 sql 数量超过上限
            if (SLOW_SQL_MAP.size() > MAX_SLOW_SQL_CATCH_COUNT) {
                // 且当前 sql 耗时不够慢，不捕获。
                if (SlowSql.FASTEST_SQL.avgSpend >= spend) {
                    return;
                } else {
                    // 移除最不慢的 sql 记录，确保慢 sql 集合不会超过上限。
                    SLOW_SQL_MAP.remove(SlowSql.FASTEST_SQL.sqlHash);
                }
            }

            slowSql = new SlowSql(sql);
            SLOW_SQL_MAP.put(sqlHash, slowSql);
        }

        slowSql.catchOnce(spend);

    }

    /**
     * 在 statement.execute() 之后被调用
     */
    @Override
    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean firstResult) {
        catchSlowSql(statement, sql);
    }

    @Override
    protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet) {
        catchSlowSql(statement, sql);
    }

    @Override
    protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {
        catchSlowSql(statement, sql);
    }

    @Override
    protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {
        String sql;
        if (statement instanceof PreparedStatementProxy) {
            sql = ((PreparedStatementProxy) statement).getSql();
        } else {
            sql = statement.getBatchSql();
        }

        catchSlowSql(statement, sql);
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
            sql = SQLUtils.format(sql, dbType, parameters, this.statementSqlFormatOption);

        }
        catchErrorSql(sql, error);
    }


    @Override
    protected void statementExecuteBefore(StatementProxy statement, String sql) {
        statement.setLastExecuteStartNano();
    }


    @Override
    protected void statementExecuteBatchBefore(StatementProxy statement) {
        statement.setLastExecuteStartNano();
    }

    @Override
    protected void statementExecuteQueryBefore(StatementProxy statement, String sql) {
        statement.setLastExecuteStartNano();
    }

    @Override
    protected void statementExecuteUpdateBefore(StatementProxy statement, String sql) {
        statement.setLastExecuteStartNano();
    }
}
