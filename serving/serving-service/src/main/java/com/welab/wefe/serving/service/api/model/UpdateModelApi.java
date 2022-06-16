/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.api.model;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.service.service.ModelService;

/**
 * @author hunter.zhao
 */
@Api(path = "model/update", name = "Update model SQL configuration")
public class UpdateModelApi extends AbstractNoneOutputApi<UpdateModelApi.Input> {

    @Autowired
    private ModelService modelService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        modelService.updateConfig(
                input.getServiceId(),
                input.getFeatureSource(),
                input.getDataSourceId(),
                input.getSqlScript(),
                input.getSqlConditionField()
        );
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "模型ID")
        private String serviceId;
        @Check(require = true, name = "配置来源")
        private PredictFeatureDataSource featureSource;

        @Check(name = "数据源ID")
        private String dataSourceId;

        @Check(name = "sql语句")
        private String sqlScript;

        @Check(name = "sql查询条件字段")
        private String sqlConditionField;

        //region getter/setter

        public PredictFeatureDataSource getFeatureSource() {
            return featureSource;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public void setFeatureSource(PredictFeatureDataSource featureSource) {
            this.featureSource = featureSource;
        }

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

        //endregion
    }
}
