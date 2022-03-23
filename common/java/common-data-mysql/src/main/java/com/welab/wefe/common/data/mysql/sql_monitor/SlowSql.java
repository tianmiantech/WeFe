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

import java.util.Date;

/**
 * @author zane
 * @date 2022/3/23
 */
public class SlowSql {
    /**
     * 记录到的慢 sql 中最快的一个
     */
    public static SlowSql FASTEST_SQL;

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
    /**
     * 平均耗时（ms）
     */
    public long avgSpend;
    /**
     * 最小耗时（ms）
     */
    public long minSpend;
    /**
     * 最大耗时（ms）
     */
    public long maxSpend;

    public SlowSql(String sql) {
        this.sql = sql;
        this.sqlHash = sql.hashCode();
    }

    /**
     * 捕获一次
     */
    public void catchOnce(long spend) {
        if (catchCount >= Integer.MAX_VALUE) {
            return;
        }

        catchCount++;
        maxSpend = Math.max(spend, maxSpend);
        minSpend = minSpend <= 0 ? spend : Math.min(spend, minSpend);
        avgSpend = (avgSpend * (catchCount - 1) + spend) / catchCount;

        // 记录全局最不慢的慢 sql
        if (FASTEST_SQL == null || avgSpend < FASTEST_SQL.avgSpend) {
            FASTEST_SQL = this;
        }
    }

}
