/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.api.project.job;

import com.welab.wefe.board.service.api.gateway.GetMemberJobProgressApi;
import com.welab.wefe.board.service.dto.entity.job.JobMemberOutputModel;
import com.welab.wefe.board.service.dto.vo.JobProgressOutput;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.FlowJobService;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.board.service.service.JobMemberService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zane.luo
 */
@Api(path = "flow/job/get_progress", name = "Get job execution progress of all members", login = false)
public class GetJobProgressApi extends AbstractApi<GetJobProgressApi.Input, List<JobProgressOutput>> {

    @Autowired
    private FlowJobService flowJobService;
    @Autowired
    private JobMemberService jobMemberService;
    @Autowired
    private GatewayService gatewayService;

    @Override
    protected ApiResult<List<JobProgressOutput>> handle(Input input) throws StatusCodeWithException {

        List<JobProgressOutput> output = new ArrayList<>();

        for (JobMemberOutputModel member : jobMemberService.list(input.jobId)) {
            JobProgressOutput progress = null;

            // get progress in local
            if (CacheObjects.getMemberId().equals(member.getMemberId())) {
                progress = flowJobService.getProgress(input.jobId, member.getJobRole());

            }
            // get progress in remote
            else {

                try {
                    progress = gatewayService.callOtherMemberBoard(
                            member.getMemberId(),
                            GetMemberJobProgressApi.class,
                            new GetMemberJobProgressApi.Input(input.jobId, member.getJobRole()),
                            JobProgressOutput.class
                    );
                } catch (Exception e) {
                    progress = JobProgressOutput.fail(member, e);
                }
            }

            if (progress != null) {
                output.add(progress);
            }
        }

        return success(output);
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "任务ID", require = true)
        private String jobId;

        //region getter/setter

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }


        //endregion
    }

}
