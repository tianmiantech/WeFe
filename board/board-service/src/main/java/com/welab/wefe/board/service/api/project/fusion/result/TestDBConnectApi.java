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
package com.welab.wefe.board.service.api.project.fusion.result;


import com.welab.wefe.board.service.util.JdbcManager;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DatabaseType;

import java.sql.Connection;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/test_db_connect", name = "结果导出", desc = "结果导出")
public class TestDBConnectApi extends AbstractApi<TestDBConnectApi.Input, TestDBConnectApi.Output> {

    @Override
    protected ApiResult<TestDBConnectApi.Output> handle(Input input) throws Exception {
        Connection conn = JdbcManager.getConnection(input.getDatabaseType(), input.getHost(), input.getPort(), input.getUserName(), input.getPassword(), input.getDatabaseName());
        if (conn != null) {
            boolean success = JdbcManager.testQuery(conn);
            if (!success) {
                throw new StatusCodeWithException(StatusCode.DATABASE_LOST, "数据库连接失败");
            }
        }

        TestDBConnectApi.Output output = new TestDBConnectApi.Output();
        output.setResult(true);
        return success(output);
    }


    public static class Input extends AbstractApiInput {

        @Check(messageOnEmpty = "数据库类型不能为空", require = true)
        private DatabaseType databaseType;

        @Check(messageOnEmpty = "IP不能为空", require = true)
        private String host;

        @Check(messageOnEmpty = "端口不能为空", require = true)
        private Integer port;

        @Check(messageOnEmpty = "数据库名称不能为空", require = true)
        private String databaseName;

        @Check(name = "用户名")
        private String userName;

        @Check(name = "密码")
        private String password;

        public DatabaseType getDatabaseType() {
            return databaseType;
        }

        public void setDatabaseType(DatabaseType databaseType) {
            this.databaseType = databaseType;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Output extends AbstractApiOutput {
        private Boolean result;

        public Output() {

        }

        public Output(Boolean result) {
            this.result = result;
        }

        public Boolean getResult() {
            return result;
        }

        public void setResult(Boolean result) {
            this.result = result;
        }
    }
}
