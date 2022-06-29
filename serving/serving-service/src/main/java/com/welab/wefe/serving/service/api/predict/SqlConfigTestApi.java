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
package com.welab.wefe.serving.service.api.predict;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.feature.SqlFeatureDataHandler;

/**
 * @author hunter.zhao
 * @date 2022/6/28
 */
@Api(
        path = "predict/sql_config_test",
        name = "sql脚本测试i"
//        domain = Caller.Customer
)
public class SqlConfigTestApi extends AbstractApi<SqlConfigTestApi.Input, Object> {

    @Override
    protected ApiResult<Object> handle(Input input) throws Exception {
        return success(
                SqlFeatureDataHandler.debug(
                        input.getDataSourceId(),
                        input.getSqlScript(),
                        input.getSqlConditionField(),
                        input.getUserId())
        );
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "数据源ID")
        private String dataSourceId;

        @Check(require = true, name = "SQL脚本")
        private String sqlScript;

        @Check(require = true, name = "查询条件字段")
        private String sqlConditionField;

        @Check(require = true, name = "样本ID")
        private String userId;

        //region getter/setter

        public String getDataSourceId() {
            return dataSourceId;
        }

        public void setDataSourceId(String dataSourceId) {
            this.dataSourceId = dataSourceId;
        }

        public String getSqlScript() {
            return sqlScript;
        }

        public void setSqlScript(String sqlScript) {
            this.sqlScript = sqlScript;
        }

        public String getSqlConditionField() {
            return sqlConditionField;
        }

        public void setSqlConditionField(String sqlConditionField) {
            this.sqlConditionField = sqlConditionField;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }


        //endregion
    }
}
