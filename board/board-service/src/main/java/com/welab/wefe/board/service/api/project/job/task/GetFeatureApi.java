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

package com.welab.wefe.board.service.api.project.job.task;

import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.board.service.dto.entity.MemberFeatureInfoModel;
import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

/**
 * @author lonnie
 */
@Api(path = "flow/job/task/feature", name = "get feature list", desc = "Get the feature column in the output result of feature calculation in the parent node")
public class GetFeatureApi extends AbstractApi<GetFeatureApi.Input, GetFeatureApi.Output> {

    @Autowired
    TaskResultService taskResultService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        return success(taskResultService.getResultFeature(input));
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "job的id")
        private String jobId;

        @Check(name = "流程的id", require = true)
        private String flowId;

        @Check(name = "流程节点id", require = true)
        private String flowNodeId;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public String getFlowNodeId() {
            return flowNodeId;
        }

        public void setFlowNodeId(String flowNodeId) {
            this.flowNodeId = flowNodeId;
        }
    }

    public static class Output {

        private boolean hasFeatureStatistic;

        private boolean hasCV;

        private boolean hasIV;

        private boolean hasLossRate;

        List<MemberFeatureInfoModel> members;

        public void putMissingRate(String memberId, JobMemberRole role, String featureName, double missingValue) {

            MemberFeatureInfoModel.Feature feature = findFeature(memberId, role, featureName);
            if (feature == null) {
                return;
            }

            feature.missingRate = missingValue;
        }

        public void putCv(String memberId, JobMemberRole role, String featureName, double cv) {

            MemberFeatureInfoModel.Feature feature = findFeature(memberId, role, featureName);
            if (feature == null) {
                return;
            }

            feature.cv = cv;
        }

        public void putIv(String memberId, JobMemberRole role, String featureName, double iv) {

            MemberFeatureInfoModel.Feature feature = findFeature(memberId, role, featureName);
            if (feature == null) {
                return;
            }

            feature.iv = iv;
        }


        /**
         * 找到指定的特征对象
         */
        @JSONField(serialize = false)
        private MemberFeatureInfoModel.Feature findFeature(String memberId, JobMemberRole role, String featureName) {
            MemberFeatureInfoModel member = members.stream()
                    .filter(x -> x.getMemberId().equals(memberId) && x.getMemberRole() == role)
                    .findAny()
                    .orElse(null);

            if (member == null) {
                return null;
            }

            return member.getFeatures().parallelStream()
                    .filter(x -> x.getName().equals(featureName))
                    .findAny()
                    .orElse(null);

        }


        public boolean isHasFeatureStatistic() {
            return hasFeatureStatistic;
        }

        public void setHasFeatureStatistic(boolean hasFeatureStatistic) {
            this.hasFeatureStatistic = hasFeatureStatistic;
        }

        public List<MemberFeatureInfoModel> getMembers() {
            return members;
        }

        public void setMembers(List<MemberFeatureInfoModel> members) {
            this.members = members;
        }

        public boolean isHasCV() {
            return hasCV;
        }

        public void setHasCV(boolean hasCV) {
            this.hasCV = hasCV;
        }

        public boolean isHasIV() {
            return hasIV;
        }

        public void setHasIV(boolean hasIV) {
            this.hasIV = hasIV;
        }

        public boolean isHasLossRate() {
            return hasLossRate;
        }

        public void setHasLossRate(boolean hasLossRate) {
            this.hasLossRate = hasLossRate;
        }

    }
}
