package com.welab.wefe.board.service.api.project.fusion.result;

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


import com.welab.wefe.board.service.service.fusion.FusionResultService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/result/export", name = "结果导出", desc = "结果导出", login = false)
public class ResultExportApi extends AbstractApi<ResultExportApi.Input, String> {

    @Autowired
    FusionResultService fusionResultService;


    @Override
    protected ApiResult<String> handle(Input input) throws StatusCodeWithException {
        return success(fusionResultService.export(input));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "指定操作的businessId", require = true)
        private String businessId;

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


        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

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


    public static class Output {

        private String tableName;

        public Output(String tableName) {
            this.tableName = tableName;
        }

        //region getter/setter

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }


        //endregion
    }
}
