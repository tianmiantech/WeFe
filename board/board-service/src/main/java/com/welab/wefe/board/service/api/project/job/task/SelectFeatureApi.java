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

import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author lonnie
 */
@Api(
        path = "/flow/job/task/select",
        name = "filter features",
        desc = "Through the passed cv/iv value and feature rate, select the features that meet the conditions"
)
public class SelectFeatureApi extends AbstractApi<SelectFeatureApi.Input, JObject> {

    @Autowired
    private TaskResultService taskResultService;

    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException {

        return success(taskResultService.selectFeature(input));
    }

    public enum SelectType {
        /**
         * cv/iv
         */
        cv_iv,

        /**
         *
         */
        miss_rate,

        /**
         */
        manual,
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "job的id")
        private String jobId;

        @Check(name = "流程id", require = true)
        private String flowId;

        @Check(name = "流程节点id", require = true)
        private String flowNodeId;

        private double cv;

        private double iv;

        private double missRate;

        @Check(name = "筛选方式", require = true)
        private SelectType selectType;

        @Check(name = "成员信息", require = true)
        private List<MemberModel> members;

        public static class MemberModel extends AbstractCheckModel {
            private String memberId;

            private String memberName;

            private JobMemberRole memberRole;

            private double cv;

            private double iv;

            private double missRate;

            private String name;

            public String getMemberId() {
                return memberId;
            }

            public void setMemberId(String memberId) {
                this.memberId = memberId;
            }

            public String getMemberName() {
                return memberName;
            }

            public void setMemberName(String memberName) {
                this.memberName = memberName;
            }

            public JobMemberRole getMemberRole() {
                return memberRole;
            }

            public void setMemberRole(JobMemberRole memberRole) {
                this.memberRole = memberRole;
            }

            public double getCv() {
                return cv;
            }

            public void setCv(double cv) {
                this.cv = cv;
            }

            public double getIv() {
                return iv;
            }

            public void setIv(double iv) {
                this.iv = iv;
            }

            public double getMissRate() {
                return missRate;
            }

            public void setMissRate(double missRate) {
                this.missRate = missRate;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

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

        public double getCv() {
            return cv;
        }

        public void setCv(double cv) {
            this.cv = cv;
        }

        public double getIv() {
            return iv;
        }

        public void setIv(double iv) {
            this.iv = iv;
        }

        public double getMissRate() {
            return missRate;
        }

        public void setMissRate(double missRate) {
            this.missRate = missRate;
        }

        public SelectType getSelectType() {
            return selectType;
        }

        public void setSelectType(SelectType selectType) {
            this.selectType = selectType;
        }

        public List<MemberModel> getMembers() {
            return members;
        }

        public void setMembers(List<MemberModel> members) {
            this.members = members;
        }

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (StringUtil.isEmpty(jobId)) {
                throw new StatusCodeWithException("没有任务id，请先执行流程，后进行筛选。", StatusCode.PARAMETER_VALUE_INVALID);
            }
        }
    }

}
