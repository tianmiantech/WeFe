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
package com.welab.wefe.serving.service.feature.sql;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

/**
 * @author hunter.zhao
 * @date 2022/7/12
 */
public class SqlRuleUtil {

    private static final String DISABLE_SYMBOLS = "--";

    public static void checkQueryContext(String sqlScript) throws StatusCodeWithException {
        try {
            Statement statement = CCJSqlParserUtil.parse(sqlScript);
            if (!(statement instanceof Select)) {
                StatusCode.ILLEGAL_REQUEST.throwException("请输入正确的查询脚本再尝试！");
            }

            if (StringUtil.contains(sqlScript, DISABLE_SYMBOLS)) {
                StatusCode.ILLEGAL_REQUEST.throwException("请输入正确的查询脚本再尝试！");
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
            StatusCode.ILLEGAL_REQUEST.throwException("请输入正确的查询脚本再尝试！");
        }
    }

    public static void main(String[] args) throws StatusCodeWithException {
        String sql = "SELECT * FROM wefe_board_4.task_progress\n" +
                "-- drop table tables;\n";

        checkQueryContext(sql);
    }
}
