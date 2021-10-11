/**
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

package com.welab.wefe.serving.service.api.model;

import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.enums.PredictFeatureDataSource;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ModelSqlConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "model/update_sql_config", name = "Update model SQL configuration")
public class UpdateModelSqlConfigApi extends AbstractNoneOutputApi<UpdateModelSqlConfigApi.Input> {

    @Autowired
    private ModelSqlConfigService modelSqlConfigService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        modelSqlConfigService.updateConfig(
                input.getModelId(),
                input.getFeatureSource(),
                input.getType(),
                input.getUrl(),
                input.getUsername(),
                input.getPassword(),
                input.getSqlContext()
        );
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "模型ID")
        private String modelId;
        @Check(require = true, name = "配置来源")
        private PredictFeatureDataSource featureSource;


        @Check(name = "db类型")
        private DatabaseType type;

        @Check(name = "路径")
        private String url;

        @Check(name = "用户名")
        private String username;

        @Check(name = "密码")
        private String password;

        @Check(name = "sql语句")
        private String sqlContext;

        //region getter/setter

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public PredictFeatureDataSource getFeatureSource() {
            return featureSource;
        }

        public void setFeatureSource(PredictFeatureDataSource featureSource) {
            this.featureSource = featureSource;
        }

        public DatabaseType getType() {
            return type;
        }

        public void setType(DatabaseType type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSqlContext() {
            return sqlContext;
        }

        public void setSqlContext(String sqlContext) {
            this.sqlContext = sqlContext;
        }

        //endregion
    }
}
