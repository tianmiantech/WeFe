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

package com.welab.wefe.board.service.api.datasource;

import com.welab.wefe.board.service.service.DataSourceService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Johnny.lin
 */
@Api(path = "data_source/add", name = "add a data source")
public class AddApi extends AbstractApi<AddApi.DataSourceAddInput, AddApi.DataSourceAddOutput> {
    @Autowired
    DataSourceService dataSourceService;

    @Override
    protected ApiResult<DataSourceAddOutput> handle(DataSourceAddInput input) throws StatusCodeWithException {
        return success(dataSourceService.add(input));
    }

    public static class DataSourceAddInput extends AbstractApiInput {

        @Check(name = "数据源名称", require = true, regex = "^.{4,30}$", messageOnInvalid = "数据源名称长度不能少于4，不能大于30", messageOnEmpty = "数据源名称不能为空")
        private String name;

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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

    public static class DataSourceAddOutput extends AbstractApiOutput {
        private String id;

        public DataSourceAddOutput() {

        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
