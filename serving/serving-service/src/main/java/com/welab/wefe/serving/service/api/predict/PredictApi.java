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
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.service.manager.ModelManager;
import com.welab.wefe.serving.service.predicter.Predictor;
import com.welab.wefe.serving.service.service.CacheObjects;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 */
@Api(
        path = "predict",
        name = "模型预测",
        allowAccessWithSign = true,
        domain = Caller.Customer
)
public class PredictApi extends AbstractApi<PredictApi.Input, PredictResult> {


    @Override
    protected ApiResult<PredictResult> handle(Input input) throws Exception {
        if (!ModelManager.getModelEnable(input.getServiceId())) {
            return fail(CacheObjects.getMemberName() + " 未上线该模型");
        }

        /**
         * batch prediction
         */
        if (CollectionUtils.isNotEmpty(input.getUserIds())) {
            PredictResult result = Predictor.batch(
                    input.getServiceId(),
                    input.getRequestId(),
                    input.getUserIds(),
                    input.getFeatureDataMap()
            );
            return success(result);
        }

        /**
         * Single prediction
         */
        PredictResult result = Predictor.predict(
                input.getRequestId(),
                input.getServiceId(),
                input.getUserId(),
                input.getFeatureData()
        );

        return success(result);
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "流水号")
        private String requestId;

        @Check(require = true, name = "模型唯一标识")
        private String serviceId;

        @Check(require = true, name = "调用者身份 id")
        private String partnerCode;

        @Check(name = "用户 id")
        private String userId;

        @Check(name = "特征参数")
        private Map<String, Object> featureData;

        @Check(name = "其他参数")
        private Map<String, Object> params;

        @Check(name = "用户 id")
        private List<String> userIds;

        @Check(name = "批量预测参数")
        private Map<String, Map<String, Object>> featureDataMap;

        //region getter/setter


        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getPartnerCode() {
            return partnerCode;
        }

        public void setPartnerCode(String partnerCode) {
            this.partnerCode = partnerCode;
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

        public Map<String, Map<String, Object>> getFeatureDataMap() {
            return featureDataMap;
        }

        public void setFeatureDataMap(Map<String, Map<String, Object>> featureDataMap) {
            this.featureDataMap = featureDataMap;
        }

        public List<String> getUserIds() {
            return userIds;
        }

        public void setUserIds(List<String> userIds) {
            this.userIds = userIds;
        }

        //endregion
    }
}
