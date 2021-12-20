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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.project.job.QueryApi;
import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.repository.JobRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.job.JobListOutputModel;
import com.welab.wefe.board.service.dto.entity.job.JobOutputModel;
import com.welab.wefe.board.service.dto.vo.JobProgressOutput;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.JobStatus;
import com.welab.wefe.common.wefe.enums.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author seven.zeng
 */
@Service
public class FlowJobService extends AbstractService {
    @Autowired
    private JobRepository jobRepo;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProjectFlowService projectFlowService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private JobService jobService;

    /**
     * Paging query flow execution history record
     */
    public PagingOutput<JobListOutputModel> query(QueryApi.Input input) {
        Specification<JobMySqlModel> where = Where
                .create()
                .equal("flowId", input.getFlowId())
                .equal("jobId", input.getJobId())
                .equal("status", input.getStatus())
                .contains("name", input.getName())
                .build(JobMySqlModel.class);

        return jobRepo.paging(where, input, JobListOutputModel.class);
    }

    /**
     * get job details
     */
    public JobOutputModel detail(String flowId, String jobId, JobMemberRole role) {

        JobMySqlModel job;

        if (StringUtil.isEmpty(jobId)) {

            ProjectFlowMySqlModel flow = projectFlowService.findOne(flowId);
            ProjectMySqlModel project = projectService.findByProjectId(flow.getProjectId());
            job = jobRepo.findLastByFlowId(flowId, project.getMyRole().name());

        } else {
            job = jobRepo.findByJobId(jobId, role.name());
        }

        if (job == null) {
            return null;
        }

        return ModelMapper.map(job, JobOutputModel.class);
    }


    /**
     * get job execution progress
     */
    public JobProgressOutput getProgress(String jobId, JobMemberRole role) {

        JobMySqlModel job = jobService.findByJobId(jobId, role);

        if (job == null) {
            return null;
        }

        List<TaskMySqlModel> tasks = taskService.listByJobId(jobId, role);


        TaskMySqlModel task;
        if (tasks.isEmpty()) {
            task = null;
        }
        // If the task has ended successfully, the progress is the last node.
        else if (job.getStatus() == JobStatus.success) {
            task = tasks.get(tasks.size() - 1);
        }
        // If the task has not started to run, there is no current execution node.
        else if (job.getStatus() == JobStatus.wait_run) {
            task = null;
        }
        // If the task is interrupted or is being executed
        else {

            // Find the node being executed
            task = tasks
                    .stream()
                    .filter(x -> x.getStatus() == TaskStatus.running)
                    .findFirst()
                    .orElse(null);

            // Looking for abnormally interrupted nodes
            if (task == null) {
                task = tasks
                        .stream()
                        .filter(x -> x.getStatus() == TaskStatus.error)
                        .findFirst()
                        .orElse(null);
            }
            // Look for manually interrupted nodes
            if (task == null) {
                task = tasks
                        .stream()
                        .filter(x -> x.getStatus() == TaskStatus.stop)
                        .findFirst()
                        .orElse(null);
            }

            /**
             * Look for nodes that have not started running
             *
             * In theory, this is a situation where the process is abnormally stuck and does not run down,
             * because after the above conditions are eliminated, there should be no nodes to be executed.
             */
            if (task == null) {
                task = tasks
                        .stream()
                        .filter(x -> x.getStatus() == TaskStatus.wait_run)
                        .findFirst()
                        .orElse(null);
            }
        }

        return JobProgressOutput.success(CacheObjects.getMemberId(), job, task);

    }
}
