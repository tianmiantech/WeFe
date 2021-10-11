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

import com.welab.wefe.board.service.api.project.job.UpdateJobStatusApi;
import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.repository.DataSetRepository;
import com.welab.wefe.board.service.database.repository.JobMemberRepository;
import com.welab.wefe.board.service.database.repository.JobRepository;
import com.welab.wefe.board.service.database.repository.TaskRepository;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.JobStatus;
import com.welab.wefe.common.enums.TaskStatus;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author seven.zeng
 */
@Service
public class JobService extends AbstractService {
    @Autowired
    JobRepository jobRepo;
    @Autowired
    TaskRepository taskRepo;
    @Autowired
    JobMemberRepository jobMemberRepo;
    @Autowired
    DataSetRepository dataSetRepository;
    @Autowired
    UnionService unionService;
    @Autowired
    JobMemberService jobMemberService;
    @Autowired
    private ProjectFlowService projectFlowService;
    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;
    @Autowired
    private ProjectService projectService;

    public void updateJob(JobMySqlModel model, Function<JobMySqlModel, JobMySqlModel> func) {

        if (model == null) {
            return;
        }

        JobStatus oldStatus = model.getStatus();

        model = func.apply(model);
        model.setUpdatedBy(CurrentAccount.id());
        if (model.getStatus() != oldStatus) {
            model.setStatusUpdatedTime(new Date());
        }

        jobRepo.save(model);
    }

    /**
     * Get the job under the specified role with the specified jobId
     * <p>
     * Note: Due to the existence of arbiter, on the promoter side, one jobId can find two jobs.
     */
    public JobMySqlModel findByJobId(String jobId, JobMemberRole role) {
        Specification<JobMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("myRole", role)
                .build(JobMySqlModel.class);

        return jobRepo.findOne(where).orElse(null);
    }

    /**
     * query jobs by job_id
     * <p>
     * Note: Due to the existence of arbiter,
     * as well as the existence of itself and its own modeling scene, multiple pieces of data will be found here.
     */
    public List<JobMySqlModel> listByJobId(String jobId) {
        Specification<JobMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .build(JobMySqlModel.class);

        return jobRepo.findAll(where);
    }


    /**
     * create FlowGraph instance
     */
    public FlowGraph createFlowGraph(String flowId) throws StatusCodeWithException {
        ProjectFlowMySqlModel flow = projectFlowService.findOne(flowId);
        ProjectMySqlModel project = projectService.findByProjectId(flow.getProjectId());

        JobMySqlModel lastJob = jobRepo.findLastByFlowId(flow.getFlowId(), project.getMyRole().name());
        return new FlowGraph(flow.getFederatedLearningType(), lastJob, projectFlowNodeService.findNodesByFlowId(flowId));
    }

    @Autowired
    private TaskService taskService;

    /**
     * Set the HasCacheResult of each node in the graph
     *
     * @param useCache Whether to use the execution result of the last task as a cache
     */
    public void setGraphHasCacheResult(FlowGraph graph, boolean useCache) throws StatusCodeWithException {

        // If the cache is not used, or if the cache does not exist,
        // the task needs to be run from beginning to end.
        if (!useCache || graph.getLastJob() == null) {

            // Mark that all nodes have no cache available
            graph
                    .getAllJobSteps()
                    .stream()
                    .forEach(x -> x.setHasCacheResult(false));

            return;
        }

        // Check the availability of each node's cache
        checkCacheEnableStatus(graph, graph.getLastJob());


        // Set all child nodes of the node without cache to be available without cache
        while (true) {
            List<FlowGraphNode> before = graph
                    .getAllJobSteps()
                    .stream()
                    .filter(x -> !x.getHasCacheResult())
                    .collect(Collectors.toList());

            // Set all child nodes of the node without cache to be available without cache
            before.forEach(x -> x.getChildren().forEach(y -> y.setHasCacheResult(false)));

            // Count the number of non-cached nodes after marking
            long afterCount = graph
                    .getAllJobSteps()
                    .stream()
                    .filter(x -> !x.getHasCacheResult())
                    .count();

            // If the caching of more nodes is not disabled, the traversal is over
            if (before.size() == afterCount) {
                break;
            }
        }
    }

    /**
     * Check the availability of each node's cache
     * <p>
     * 1. If the node has been edited after the task is created, the cache is not available.
     * 2. The status of the corresponding task of the node is not success is unavailable
     */
    private void checkCacheEnableStatus(FlowGraph graph, JobMySqlModel lastJob) {

        // Based on the time when the task was created,
        // nodes that have been edited after this time cannot use the cache.
        long lastJobCreateTime = lastJob.getCreatedTime().getTime();

        // Temporarily mark all nodes as cache available
        graph.getAllJobSteps().forEach(x -> x.setHasCacheResult(true));

        // Find the nodes whose version number is greater than the base time.
        // These nodes have been edited after the task is created, and the cache cannot be used.
        graph
                .getAllJobSteps()
                .stream()
                .filter(x -> x.getParamsVersion() >= lastJobCreateTime)
                .forEach(x -> x.setHasCacheResult(false));


        // Check whether the cache of the task corresponding to the node is available.
        // If the task status is incorrect, the cache is not available.
        // A node may correspond to multiple tasks
        List<TaskMySqlModel> taskMysqlList = taskService.listByJobId(lastJob.getJobId(), lastJob.getMyRole());
        Map<String, List<TaskMySqlModel>> taskMap = new HashMap<>();
        for (TaskMySqlModel model : taskMysqlList) {
            List<TaskMySqlModel> taskList = taskMap.get(model.getFlowNodeId());
            if (taskList == null) {
                taskList = new ArrayList<>();
            }
            taskList.add(model);
            taskMap.put(model.getFlowNodeId(), taskList);
        }

        graph.getAllJobSteps().stream().filter(x -> x.getHasCacheResult()).filter(x -> {
            List<TaskMySqlModel> taskList = taskMap.get(x.getNodeId());
            if (taskList == null) {
                return true;
            } else {
                for (TaskMySqlModel m : taskList) {
                    if (m.getStatus() != TaskStatus.success) {
                        return true;
                    }
                }
            }
            return false;
        }).forEach(x -> x.setHasCacheResult(false));
    }

    /**
     * update job status
     */
    public void updateJobStatus(UpdateJobStatusApi.Input input) {
        JobMySqlModel job = jobRepo.findById(input.getId()).orElse(null);

        if (job != null) {
            updateJob(job, x -> {
                x.setStatus(input.getJobStatus());
                x.setMessage(input.getMessage());
                return x;
            });
        }
    }

}
