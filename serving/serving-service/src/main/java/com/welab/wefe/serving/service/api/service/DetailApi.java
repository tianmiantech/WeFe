/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.service.dto.ModelSqlConfigOutput;
import com.welab.wefe.serving.service.dto.ModelStatusOutput;
import com.welab.wefe.serving.service.dto.TreeNode;
import com.welab.wefe.serving.service.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Api(path = "service/detail", name = "服务详情")
public class DetailApi extends AbstractApi<DetailApi.Input, DetailApi.Output> {

    @Autowired
    private ServiceService service;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
        try {
            return success(service.detail(input));
        } catch (Exception e) {
            e.printStackTrace();
            return fail(e.getMessage());
        }
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "主键id", require = true)
        private String id;

        @Check(name = "服务类型", require = true)
        private int serviceType = 0;

        // region getter/setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getServiceType() {
            return serviceType;
        }

        public void setServiceType(int serviceType) {
            this.serviceType = serviceType;
        }

        // endregion
    }

    public static class Output extends AbstractApiOutput {
        private String id;
        private String name;
        private String url;
        private int serviceType;
        private List<String> queryParams;// json
        private JSONArray queryParamsConfig;// json
        private JSONObject dataSource;// json
        private JSONArray serviceConfig;
        private String createdBy;
        private String updatedBy;
        private Date createdTime;
        private Date updatedTime;
        private int status;

        private String serviceId;
        private Algorithm algorithm;
        private List<JobMemberRole> myRole;
        private FederatedLearningType flType;
        private String creator;
        private JObject modelParam;
        private PredictFeatureDataSource featureSource;
        private ModelSqlConfigOutput modelSqlConfig;
        private String processor;
        private List<TreeNode> xgboostTree;
        private List<ModelStatusOutput> modelStatus;

        private JSONObject preview;

        private String sqlScript;

        private String sqlConditionField;

        private String dataSourceId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getServiceType() {
            return serviceType;
        }

        public void setServiceType(int serviceType) {
            this.serviceType = serviceType;
        }

        public List<String> getQueryParams() {
            return queryParams;
        }

        public void setQueryParams(List<String> queryParams) {
            this.queryParams = queryParams;
        }

        public JSONObject getDataSource() {
            return dataSource;
        }

        public void setDataSource(JSONObject dataSource) {
            this.dataSource = dataSource;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
        }

        public Date getUpdatedTime() {
            return updatedTime;
        }

        public void setUpdatedTime(Date updatedTime) {
            this.updatedTime = updatedTime;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public JSONArray getServiceConfig() {
            return serviceConfig;
        }

        public void setServiceConfig(JSONArray serviceConfig) {
            this.serviceConfig = serviceConfig;
        }

        public JSONObject getPreview() {
            return preview;
        }

        public void setPreview(JSONObject preview) {
            this.preview = preview;
        }

        public JSONArray getQueryParamsConfig() {
            return queryParamsConfig;
        }

        public void setQueryParamsConfig(JSONArray queryParamsConfig) {
            this.queryParamsConfig = queryParamsConfig;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        public List<JobMemberRole> getMyRole() {
            return myRole;
        }

        public void setMyRole(List<JobMemberRole> myRole) {
            this.myRole = myRole;
        }

        public FederatedLearningType getFlType() {
            return flType;
        }

        public void setFlType(FederatedLearningType flType) {
            this.flType = flType;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public JObject getModelParam() {
            return modelParam;
        }

        public void setModelParam(JObject modelParam) {
            this.modelParam = modelParam;
        }

        public PredictFeatureDataSource getFeatureSource() {
            return featureSource;
        }

        public void setFeatureSource(PredictFeatureDataSource featureSource) {
            this.featureSource = featureSource;
        }

        public ModelSqlConfigOutput getModelSqlConfig() {
            return modelSqlConfig;
        }

        public void setModelSqlConfig(ModelSqlConfigOutput modelSqlConfig) {
            this.modelSqlConfig = modelSqlConfig;
        }

        public String getProcessor() {
            return processor;
        }

        public void setProcessor(String processor) {
            this.processor = processor;
        }

        public List<TreeNode> getXgboostTree() {
            return xgboostTree;
        }

        public void setXgboostTree(List<TreeNode> xgboostTree) {
            this.xgboostTree = xgboostTree;
        }

        public List<ModelStatusOutput> getModelStatus() {
            return modelStatus;
        }

        public void setModelStatus(List<ModelStatusOutput> modelStatus) {
            this.modelStatus = modelStatus;
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

        public String getDataSourceId() {
            return dataSourceId;
        }

        public void setDataSourceId(String dataSourceId) {
            this.dataSourceId = dataSourceId;
        }


    }

}
