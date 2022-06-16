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
package com.welab.wefe.common.web.api.dev;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.sql_monitor.ErrorSql;
import com.welab.wefe.common.data.mysql.sql_monitor.SqlMonitor;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;

import java.util.Collection;

/**
 * @author zane
 * @date 2022/3/23
 */
@Api(path = "error_sql/list", name = "执行失败的 sql 列表")
public class ErrorSqlList extends AbstractNoneInputApi<ErrorSqlList.Output> {

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        if (!CurrentAccount.isAdmin()) {
            StatusCode.PERMISSION_DENIED.throwException("普通用户无法进行此操作。");
        }

        return success(new Output(SqlMonitor.getErrorSqlList()));
    }

    public static class Output {
        public Collection<ErrorSql> list;

        public Output() {
        }

        public Output(Collection<ErrorSql> list) {
            this.list = list;
        }
    }
}
