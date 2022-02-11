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

package com.welab.wefe.board.service.api.gateway;

import com.welab.wefe.board.service.dto.vo.JobProgressOutput;
import com.welab.wefe.board.service.service.FlowJobService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "gateway/get_job_progress", name = "get job progress", login = false, rsaVerify = true)
public class GetMemberJobProgressApi extends AbstractApi<GetMemberJobProgressApi.Input, JobProgressOutput> {

    @Autowired
    private FlowJobService flowJobService;

    @Override
    protected ApiResult<JobProgressOutput> handle(Input input) throws StatusCodeWithException {
        JobProgressOutput progress = flowJobService.getProgress(input.jobId, input.memberRole);

        return success(progress);
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "任务ID", require = true)
        private String jobId;

        @Check(name = "角色", require = true)
        private JobMemberRole memberRole;

        public Input() {
        }

        public Input(String jobId, JobMemberRole memberRole) {
            this.jobId = jobId;
            this.memberRole = memberRole;
        }

        //region getter/setter

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public JobMemberRole getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(JobMemberRole memberRole) {
            this.memberRole = memberRole;
        }


        //endregion
    }

}
