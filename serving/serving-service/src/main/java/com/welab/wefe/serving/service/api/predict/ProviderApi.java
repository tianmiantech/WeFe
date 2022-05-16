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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.service.manager.ModelManager;
import com.welab.wefe.serving.service.predicter.Predicter;
import com.welab.wefe.serving.service.service.CacheObjects;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Map;


/**
 * @author hunter.zhao
 */
@Api(
        path = "predict/provider",
        name = "Model to predict",
        login = false,
        rsaVerify = true,
        domain = Caller.Member
)
public class ProviderApi extends AbstractApi<ProviderApi.Input, PredictResult> {

    @Override
    protected ApiResult<PredictResult> handle(Input input) {
        try {

            if (!ModelManager.getModelEnable(input.getModelId())) {
                return fail("模型成员 " + CacheObjects.getMemberName() + " 未上线该模型");
            }

            /**
             * Batch prediction
             */
            if (input.getBatch()) {
                PredictResult result = Predicter.batchProviderPredict(
                        input.getSeqNo(),
                        input.getModelId(),
                        input.getMemberId(),
                        input.getPredictParams()
                );

                return success(result);
            }

            /**
             * Single prediction
             */
            PredictResult result = Predicter.provider(
                    input.getSeqNo(),
                    input.getModelId(),
                    input.getMemberId(),
                    input.getUserId(),
                    input.getFeatureData(),
                    input.getParams() == null ? null : new JSONObject(input.getParams())
            );
            return success(result);
        } catch (Exception e) {
            return fail("predict error : " + e.getMessage());
        }
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "流水号")
        private String seqNo;
        @Check(require = true, name = "模型唯一标识")
        private String modelId;
        @Check(require = true, name = "调用者身份 id")
        private String memberId;
        @Check(name = "用户 id")
        private String userId;


        @Check(name = "特征入参")
        private Map<String, Object> featureData;

        @Check(name = "其他参数")
        private Map<String, Object> params;

        @Check(name = "是否批量")
        private Boolean isBatch = false;

        @Check(name = "批量预测参数")
        private PredictParams predictParams;


        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (!isBatch) {
                if (StringUtil.isEmpty(userId)) {
                    throw new StatusCodeWithException("单条预测时，参数userId不能为空", StatusCode.PARAMETER_VALUE_INVALID);
                }
            } else {
                if (predictParams == null || CollectionUtils.isEmpty(predictParams.getUserIds())) {
                    throw new StatusCodeWithException("批量预测时，参数predictParamsList不能为空", StatusCode.PARAMETER_VALUE_INVALID);
                }
            }

        }

        //region getter/setter

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getSeqNo() {
            return seqNo;
        }

        public void setSeqNo(String seqNo) {
            this.seqNo = seqNo;
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

//        public PredictParams getPredictParams() {
//            return predictParams;
//        }
//
//        public void setPredictParams(PredictParams predictParams) {
//            this.predictParams = predictParams;
//        }

        public PredictParams getPredictParams() {
            return predictParams;
        }

        public void setPredictParams(PredictParams predictParams) {
            this.predictParams = predictParams;
        }


        //endregion
    }

}
