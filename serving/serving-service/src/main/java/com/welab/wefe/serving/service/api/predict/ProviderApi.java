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

package com.welab.wefe.serving.service.api.predict;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.service.manager.ModelManager;
import com.welab.wefe.serving.service.predicter.Predictor;
import com.welab.wefe.serving.service.service.CacheObjects;

import java.util.Map;


/**
 * @author hunter.zhao
 */
@Api(
        path = "predict/provider",
        name = "Model to predict",
        allowAccessWithSign = true,
        domain = Caller.Member
)
public class ProviderApi extends AbstractApi<ProviderApi.Input, PredictResult> {

    @Override
    protected ApiResult<PredictResult> handle(Input input) {
        try {

            if (!ModelManager.getModelEnable(input.getModelId())) {
                return fail(CacheObjects.getMemberName() + " 未上线该模型");
            }

            /**
             * Batch prediction
             */
//            if (input.getBatch()) {
//                PredictResult result = Predictor.batchProviderPredict(
//                        input.getRequestId(),
//                        input.getModelId(),
//                        input.getMemberId(),
//                        input.getPredictParams()
//                );
//
//                return success(result);
//            }

            /**
             * Single prediction
             */
            PredictResult result = Predictor.predict(
                    input.getRequestId(),
                    input.getModelId(),
                    input.getUserId(),
                    input.getFeatureData()
            );

            return success(result);
        } catch (Exception e) {
            return fail("协作方 " + CacheObjects.getMemberName() + "错误" + e.getMessage());
        }
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "流水号")
        private String requestId;
        @Check(require = true, name = "模型唯一标识")
        private String modelId;
        @Check(require = true, name = "调用者身份 id")
        private String partnerCode;
        @Check(name = "用户 id")
        private String userId;

        @Check(name = "特征入参")
        private Map<String, Object> featureData;

        @Check(name = "其他参数")
        private Map<String, Object> params;

        @Check(name = "是否批量")
        private Boolean isBatch = false;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (!isBatch) {
                if (StringUtil.isEmpty(userId)) {
                    throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "单条预测时，参数userId不能为空");
                }
            }

        }

        //region getter/setter


        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
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

        public Boolean getBatch() {
            return isBatch;
        }

        public void setBatch(Boolean batch) {
            isBatch = batch;
        }

        //endregion
    }

}
