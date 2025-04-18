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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.service.dto.MemberParams;
import com.welab.wefe.serving.service.service.ModelService;

/**
 * @author hunter.zhao
 */
@Api(
        path = "model_save",
        name = "保存模型信息",
        allowAccessWithSign = true,
        domain = Caller.Board
)
public class SaveModelApi extends AbstractNoneOutputApi<SaveModelApi.Input> {

    @Autowired
    private ModelService modelService;

    @Override
    protected ApiResult<?> handler(Input input) {
        modelService.save(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "模型ID")
        private String serviceId;
        @Check(require = true, name = "我的角色")
        private JobMemberRole myRole;
        @Check(name = "模型名称")
        private String name;
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
        @Check(name = "服务地址")
        private String url;
        @Check(name = "服务地址")
        private String scoresDistribution;
        @Check(name = "服务地址")
        private String scoreCardInfo;


        //region getter/setter

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public JobMemberRole getMyRole() {
            return myRole;
        }

        public void setMyRole(JobMemberRole myRole) {
            this.myRole = myRole;
        }


        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getScoresDistribution() {
            return scoresDistribution;
        }

        public void setScoresDistribution(String scoresDistribution) {
            this.scoresDistribution = scoresDistribution;
        }

        public String getScoreCardInfo() {
            return scoreCardInfo;
        }

        public void setScoreCardInfo(String scoreCardInfo) {
            this.scoreCardInfo = scoreCardInfo;
        }

        //endregion
    }
}
