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

package com.welab.wefe.serving.service.api;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.serving.service.dto.MemberParams;
import com.welab.wefe.serving.service.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 */
@Api(
        path = "model_save",
        name = "保存模型信息",
        login = false,
        rsaVerify = true,
        domain = Caller.Board
)
public class SaveModelApi extends AbstractNoneOutputApi<SaveModelApi.Input> {

    @Autowired
    private ModelService modelService;

    @Override
    protected ApiResult<?> handler(Input input) {
        modelService.save(
                input.getModelId(),
                input.getAlgorithm(),
                input.getFlType(),
                input.getModelParam(),
                input.getMemberParams());

        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "流水号")
        private String modelId;
        @Check(require = true, name = "算法")
        private Algorithm algorithm;
        @Check(require = true, name = "联邦学习类型")
        private FederatedLearningType flType;
        @Check(require = true, name = "模型唯一标识")
        private String modelParam;
        @Check(require = true, name = "成员入参")
        private List<MemberParams> memberParams;

        @Check(name = "特征工程参数")
        Map<Integer, Object> featureEngineerMap;


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

        public String getModelParam() {
            return modelParam;
        }

        public void setModelParam(String modelParam) {
            this.modelParam = modelParam;
        }

        public List<MemberParams> getMemberParams() {
            return memberParams;
        }

        public void setMemberParams(List<MemberParams> memberParams) {
            this.memberParams = memberParams;
        }

        public Map<Integer, Object> getFeatureEngineerMap() {
            return featureEngineerMap;
        }

        public void setFeatureEngineerMap(Map<Integer, Object> featureEngineerMap) {
            this.featureEngineerMap = featureEngineerMap;
        }

        //endregion
    }
}
