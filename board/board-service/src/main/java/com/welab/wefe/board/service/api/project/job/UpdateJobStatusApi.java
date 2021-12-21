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

package com.welab.wefe.board.service.api.project.job;

import com.welab.wefe.board.service.service.JobService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lonnie
 */
@Api(path = "project/job/update/status", name = "Update the status of the job")
public class UpdateJobStatusApi extends AbstractNoneOutputApi<UpdateJobStatusApi.Input> {

    @Autowired
    private JobService jobService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        jobService.updateJobStatus(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        public Input() {
        }

        public Input(String id, JobStatus jobStatus, String message) {
            this.id = id;
            this.jobStatus = jobStatus;
            this.message = message;
        }

        @Check(name = "id唯一标识", require = true)
        private String id;

        @Check(name = "任务状态", require = true)
        private JobStatus jobStatus;

        @Check(name = "备注信息")
        private String message;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public JobStatus getJobStatus() {
            return jobStatus;
        }

        public void setJobStatus(JobStatus jobStatus) {
            this.jobStatus = jobStatus;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
