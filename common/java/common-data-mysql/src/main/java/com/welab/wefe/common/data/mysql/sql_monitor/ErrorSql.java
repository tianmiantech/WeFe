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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author zane
 * @date 2022/3/23
 */
public class ErrorSql {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorSql.class);

    public String sql;
    /**
     * 首次捕获时间
     */
    public Date firstCatchTime = new Date();
    /**
     * sql 语句的 hashCode
     */
    public int sqlHash;
    /**
     * 捕获次数
     */
    public int catchCount;
    public String message;


    public ErrorSql(String sql) {
        this.sql = sql;
        this.sqlHash = sql.hashCode();
    }

    /**
     * 捕获一次
     */
    public void catchOnce(Throwable error) {
        LOG.error("catch sql error: {}", sql, error);

        if (catchCount >= Integer.MAX_VALUE) {
            return;
        }

        catchCount++;

        if (message != null) {
            return;
        }

        StringBuilder str = new StringBuilder();
        str.append(error.getClass().getSimpleName())
                .append(" ")
                .append(error.getMessage())
                .append(System.lineSeparator());

        for (StackTraceElement stackTraceElement : error.getStackTrace()) {
            str.append(stackTraceElement.toString());
            str.append(System.lineSeparator());
        }

        message = str.toString();
    }
}
