/**
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

package com.welab.wefe.board.service.api.project.job.task;

import com.welab.wefe.board.service.dto.entity.MemberFeatureInfoModel;
import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author lonnie
 */
@Api(path = "/flow/job/task/feature", name = "get feature list", desc = "Get the feature column in the output result of feature calculation in the parent node")
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

        private boolean hasFeatureCalculation;
        
        private boolean hasFeatureBinning;

        List<MemberFeatureInfoModel> members;

        public boolean isHasFeatureStatistic() {
            return hasFeatureStatistic;
        }

        public void setHasFeatureStatistic(boolean hasFeatureStatistic) {
            this.hasFeatureStatistic = hasFeatureStatistic;
        }

        public boolean isHasFeatureCalculation() {
            return hasFeatureCalculation;
        }

        public void setHasFeatureCalculation(boolean hasFeatureCalculation) {
            this.hasFeatureCalculation = hasFeatureCalculation;
        }

        public List<MemberFeatureInfoModel> getMembers() {
            return members;
        }

        public void setMembers(List<MemberFeatureInfoModel> members) {
            this.members = members;
        }

        public boolean isHasFeatureBinning() {
            return hasFeatureBinning;
        }

        public void setHasFeatureBinning(boolean hasFeatureBinning) {
            this.hasFeatureBinning = hasFeatureBinning;
        }
    }
}
