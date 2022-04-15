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

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author hunter.zhao
 */
@Api(path = "model/query", name = "Get model")
public class QueryApi extends AbstractApi<QueryApi.Input, PagingOutput<QueryApi.Output>> {
    @Autowired
    ModelService modelService;

    @Override
    protected ApiResult<PagingOutput<Output>> handle(Input input) {
        return success(modelService.query(input));
    }

    public static class Input extends PagingInput {
        @Check(name = "模型ID")
        private String modelId;

        @Check(name = "算法类型")
        private Algorithm algorithm;

        @Check(name = "联邦学习类型")
        private FederatedLearningType flType;

        @Check(name = "任务创建者")
        private String creator;


        //region getter/setter

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
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


        //endregion
    }

    public static class Output extends AbstractApiInput {

        private String id;

        private String modelId;

        private String name;

        private Algorithm algorithm;

        private FederatedLearningType flType;

        private String creator;

        private String modelParam;

        private Date createdTime;

        private Date updatedTime;

        private JobMemberRole myRole;

        private PredictFeatureDataSource featureSource;

        private boolean enable;
        //region getter/setter


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
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

        public String getModelParam() {
            return modelParam;
        }

        public void setModelParam(String modelParam) {
            this.modelParam = modelParam;
        }

        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
        }

        public JobMemberRole getMyRole() {
            return myRole;
        }

        public void setMyRole(JobMemberRole myRole) {
            this.myRole = myRole;
        }

        public PredictFeatureDataSource getFeatureSource() {
            return featureSource;
        }

        public void setFeatureSource(PredictFeatureDataSource featureSource) {
            this.featureSource = featureSource;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public Date getUpdatedTime() {
            return updatedTime;
        }

        public void setUpdatedTime(Date updatedTime) {
            this.updatedTime = updatedTime;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        //endregion
    }

}
