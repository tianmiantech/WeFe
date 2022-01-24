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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.service.predicter.Predicter;

import java.util.Map;


/**
 * @author hunter.zhao
 */
@Api(
        path = "predict/debug",
        name = "模型预测"
        , login = false
)
public class DebugApi extends AbstractApi<DebugApi.Input, PredictResult> {

    @Override
    protected ApiResult<PredictResult> handle(Input input) {

        try {
            PredictResult result = Predicter.debug(
                    input.getModelId(),
                    input.getUserId(),
                    input.getFeatureData(),
                    input.getParams() == null ? null : new JSONObject(input.getParams()),
                    input.getFeatureSource(),
                    input.getMyRole()
            );

            return success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return fail("predict error : " + e.getMessage());
        }
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "模型唯一标识")
        private String modelId;
        @Check(require = true, name = "用户 id")
        private String userId;

        @Check(name = "预测入参")
        private Map<String, Object> featureData;

        @Check(name = "其他参数")
        private Map<String, Object> params;

        @Check(name = "特征来源类型")
        private PredictFeatureDataSource featureSource;

        @Check(name = "我的角色")
        private JobMemberRole myRole;


        //region getter/setter

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }


        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Map<String, Object> getFeatureData() {
            return featureData;
        }

        public void setFeatureData(Map<String, Object> featureData) {
            this.featureData = featureData;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        public PredictFeatureDataSource getFeatureSource() {
            return featureSource;
        }

        public void setFeatureSource(PredictFeatureDataSource featureSource) {
            this.featureSource = featureSource;
        }

        public JobMemberRole getMyRole() {
            return myRole;
        }

        public void setMyRole(JobMemberRole myRole) {
            this.myRole = myRole;
        }

        //endregion
    }

}
