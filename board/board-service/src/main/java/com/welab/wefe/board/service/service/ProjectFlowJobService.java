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

package com.welab.wefe.board.service.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.project.flow.StartFlowApi;
import com.welab.wefe.board.service.api.project.job.ResumeJobApi;
import com.welab.wefe.board.service.api.project.job.StopJobApi;
import com.welab.wefe.board.service.component.Components;
import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.component.OotComponent;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.dto.AbstractDataIOParam;
import com.welab.wefe.board.service.component.base.dto.AbstractDataSetItem;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.*;
import com.welab.wefe.board.service.database.repository.*;
import com.welab.wefe.board.service.dto.entity.data_resource.output.DataResourceOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.TableDataSetOutputModel;
import com.welab.wefe.board.service.dto.kernel.Member;
import com.welab.wefe.board.service.dto.kernel.machine_learning.Env;
import com.welab.wefe.board.service.dto.kernel.machine_learning.JobDataSet;
import com.welab.wefe.board.service.dto.kernel.machine_learning.KernelJob;
import com.welab.wefe.board.service.dto.kernel.machine_learning.Project;
import com.welab.wefe.board.service.dto.vo.JobArbiterInfo;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.data_resource.DataResourceService;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.wefe.checkpoint.dto.MemberAvailableCheckOutput;
import com.welab.wefe.common.wefe.enums.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author winter.zou
 */
@Service
public class ProjectFlowJobService extends AbstractService {
    @Autowired
    private JobService jobService;
    @Autowired
    private JobRepository jobRepo;
    @Autowired
    private TaskResultService taskResultService;
    @Autowired
    private JobMemberRepository jobMemberRepo;
    @Autowired
    private TaskResultRepository taskResultRepository;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FlowActionQueueService flowActionQueueService;
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private ProjectFlowRepository projectFlowRepo;
    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private ProjectFlowService projectFlowService;
    @Autowired
    protected TaskRepository taskRepository;
    @Autowired
    private ProjectDataSetService projectDataSetService;
    @Autowired
    private ServiceCheckService serviceCheckService;
    @Autowired
    private TableDataSetService tableDataSetService;


    public static final int MIX_FLOW_PROMOTER_NUM = 2;

    /**
     * start flow
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized String startFlow(StartFlowApi.Input input) throws StatusCodeWithException {

        ProjectFlowMySqlModel flow = projectFlowRepo.findOne("flowId", input.getFlowId(), ProjectFlowMySqlModel.class);
        if (flow == null) {
            throw new StatusCodeWithException("未找到相应的流程！", StatusCode.ILLEGAL_REQUEST);
        }

        ProjectMySqlModel project = projectService.findByProjectId(flow.getProjectId());

        if (!input.fromGateway() && (!isCreator(flow, project))) {
            throw new StatusCodeWithException("只有 创建者 才能启动任务！", StatusCode.ILLEGAL_REQUEST);
        }

        if (!input.fromGateway()) {
            input.setJobId(UUID.randomUUID().toString().replaceAll("-", ""));
        }

        JobMySqlModel lastJob = jobRepo.findLastByFlowId(flow.getFlowId(), project.getMyRole().name());
        if (lastJob != null && !lastJob.getStatus().finished()) {
            throw new StatusCodeWithException("请稍等，当前任务尚未结束，请等待其结束后重试。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        JobArbiterInfo jobArbiterInfo = calcArbiterInfo(flow, input, project);
        boolean isOotMode = StringUtils.isNotEmpty(input.getOotJobId());

        // save job members
        List<JobMemberMySqlModel> jobMembers = listJobMembers(project.getProjectId(), input.getFlowId(),
                input.getJobId(), jobArbiterInfo, isOotMode);

        if (!input.fromGateway()) {
            if (jobMembers.stream().noneMatch(x -> CacheObjects.getMemberId().equals(x.getMemberId()))) {
                throw new StatusCodeWithException("当前任务不包含我方数据集，无法启动。", StatusCode.PARAMETER_VALUE_INVALID);
            }
        }
        long memberCount = jobMembers.stream().filter(x -> x.getJobRole() != JobMemberRole.arbiter).count();
        if (memberCount < MIX_FLOW_PROMOTER_NUM && !isOotMode) {
            throw new StatusCodeWithException("需要在【" + ComponentType.DataIO.getLabel() + "】中选择两个或两个以上的数据集", StatusCode.PARAMETER_VALUE_INVALID);
        }
        long promoterMemberCount = jobMembers.stream().filter(x -> x.getJobRole() == JobMemberRole.promoter).count();
        if (promoterMemberCount >= MIX_FLOW_PROMOTER_NUM && !flow.getFederatedLearningType().equals(FederatedLearningType.mix)) {
            throw new StatusCodeWithException("【选择数据集】组件参数错误，请先移除再重新添加", StatusCode.PARAMETER_VALUE_INVALID);
        }
        for (JobMemberMySqlModel jobMember : jobMembers) {
            if (!CacheObjects.getMemberId().equals(jobMember.getMemberId())) {
                continue;
            }

            lastJob = jobRepo.findLastByFlowId(flow.getFlowId(), jobMember.getJobRole().name());

            // create new job
            JobMySqlModel job = createJob(flow, input.getJobId(), jobMember.getJobRole());

            // create Graph
            FlowGraph graph = new FlowGraph(job, lastJob, jobMembers, projectFlowNodeService.findNodesByFlowId(job.getFlowId()));

            // check
            if (jobMember.getJobRole() == JobMemberRole.promoter) {
                checkBeforeStartFlow(graph, project, isOotMode);
            }
            // create task
            createJobTasks(project, graph, input.isUseCache(), input.getEndNodeId(), flow.getFederatedLearningType());

        }

        gatewayService.syncToOtherJobMembers(input.getJobId(), input, StartFlowApi.class);

        flowActionQueueService.runJob(input, input.getJobId(), project.getProjectType());

        //update flow
        projectFlowService.updateFlowStatus(flow.getFlowId(), ProjectFlowStatus.running);

        return input.getJobId();

    }

    public boolean isCreator(ProjectFlowMySqlModel flow, ProjectMySqlModel project) {
        return JobMemberRole.promoter == project.getMyRole()
                && CacheObjects.isCurrentMemberAccount(flow.getCreatedBy());
    }

    public JobArbiterInfo calcArbiterInfo(ProjectFlowMySqlModel flow, StartFlowApi.Input input,
                                          ProjectMySqlModel project) {
        JobArbiterInfo info = new JobArbiterInfo();
        info.setHasArbiter(false);

        // 深度学习没有 arbiter 角色
        if (project.getProjectType() == ProjectType.DeepLearning) {
            return info;
        }

        if (flow.getFederatedLearningType() == FederatedLearningType.horizontal) {
            if (project.getMyRole() == JobMemberRole.promoter) {
                info.setHasArbiter(true);
            }
        }
        if (flow.getFederatedLearningType() == FederatedLearningType.mix) {
            if (!input.fromGateway() && project.getMyRole() == JobMemberRole.promoter) {
                info.setHasArbiter(true);
            }
        }
        return info;
    }

    /**
     * Check the effectiveness of the task before starting the task.
     */
    private void checkBeforeStartFlow(FlowGraph graph, ProjectMySqlModel project, boolean isOotMode) throws StatusCodeWithException {

        if (CollectionUtils.isEmpty(graph.getStartNodes())) {
            throw new StatusCodeWithException("流程中没有起始节点，无法执行该流程。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        boolean hasDataSet = graph.getStartNodes()
                .stream()
                .anyMatch(x ->
                        x.getComponentType() == ComponentType.DataIO
                                || x.getComponentType() == ComponentType.Oot
                                || x.getComponentType() == ComponentType.ImageDataIO
                );
        if (!hasDataSet) {
            throw new StatusCodeWithException("流程起点必须包含数据集加载，否则无法执行流程。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        if (isOotMode) {
            if (graph.allNodes.size() > 1 || graph.allNodes.get(0).getComponentType() != ComponentType.Oot) {
                throw new StatusCodeWithException("只允许只有[" + ComponentType.Oot.getLabel() + "]组件", StatusCode.PARAMETER_VALUE_INVALID);
            }
        }

        // Check whether the services of each member are available
        for (JobMemberMySqlModel member : graph.getMembers()) {
            MemberAvailableCheckOutput status = serviceCheckService.getMemberAvailableInfo(member.getMemberId());
            if (!status.available) {
                StatusCode.REMOTE_SERVICE_ERROR.throwException(
                        "成员 "
                                + CacheObjects.getMemberName(member.getMemberId())
                                + " 的 " + status.errorServiceType.name() + " 服务不可用："
                                + status.message
                );
            }
        }

        // Check the validity of the dataset
        for (JobMemberMySqlModel member : graph.getMembers()) {
            // arbiter pass
            if (member.getJobRole() == JobMemberRole.arbiter) {
                continue;
            }

            String memberName = CacheObjects.getMemberName(member.getMemberId());
            if (memberName == null) {
                memberName = "未知成员";
            }

            // mine
            if (CacheObjects.getMemberId().equals(member.getMemberId())) {
                ProjectDataSetMySqlModel projectDataSet = projectDataSetService.findOne(project.getProjectId(), member.getDataSetId(), member.getJobRole());

                if (projectDataSet == null) {
                    throw new StatusCodeWithException("成员【" + memberName + " - " + member.getJobRole().name() + "】的数据集 " + member.getDataSetId() + " 不存在，可能已删除或移除了授权。", StatusCode.PARAMETER_VALUE_INVALID);
                }

            }
            // other
            else {
                List<ProjectDataSetMySqlModel> projectDataSetList = projectDataSetService
                        .listByDataSetId(project.getProjectId(), member.getDataSetId(), member.getJobRole());

                for (ProjectDataSetMySqlModel projectDataSet : projectDataSetList) {


                    if (projectDataSet.getSourceType() != null) {
                        continue;
                    } else {
                        DataResourceOutputModel resource = dataResourceService.findDataResourceFromLocalOrUnion(projectDataSet);
                        if (resource == null) {
                            throw new StatusCodeWithException("成员【" + memberName + "】的数据集 " + member.getDataSetId() + " 不存在，可能已被删除或不可见。", StatusCode.PARAMETER_VALUE_INVALID);
                        }
                    }

                    if (projectDataSet.getAuditStatus() != AuditStatus.agree) {
                        throw new StatusCodeWithException("成员【" + memberName + "】的数据集 " + member.getDataSetId() + " 尚未授权，不可使用。", StatusCode.PARAMETER_VALUE_INVALID);
                    }
                }
            }

        }
    }


    @Transactional(rollbackFor = Exception.class)
    public synchronized void resumeJob(ResumeJobApi.Input input) throws StatusCodeWithException {

        List<JobMySqlModel> jobs = jobService.listByJobId(input.getJobId());
        if (jobs.isEmpty()) {
            throw new StatusCodeWithException("未找到相应的任务！", StatusCode.ILLEGAL_REQUEST);
        }

        JobMySqlModel job = jobs.get(0);

        ProjectMySqlModel project = projectService.findByProjectId(job.getProjectId());

        if (!input.fromGateway() && JobMemberRole.promoter != project.getMyRole()) {
            throw new StatusCodeWithException("只有 promoter 才能继续任务！", StatusCode.ILLEGAL_REQUEST);
        }

        if (job.getStatus() != JobStatus.stop_on_running && job.getStatus() != JobStatus.error_on_running) {
            throw new StatusCodeWithException("当前状态不允许进行继续任务操作！", StatusCode.ILLEGAL_REQUEST);
        }

        // 如果是深度学习的任务，把之前任务的配置改为继续，就可以实现续跑。
        if (project.getProjectType() == ProjectType.DeepLearning) {
            List<TaskMySqlModel> tasks = taskService.listByJobId(job.getJobId(), job.getMyRole());
            tasks
                    .stream()
                    .filter(x -> x.getTaskType() == ComponentType.PaddleClassify || x.getTaskType() == ComponentType.PaddleDetection)
                    .forEach(x -> {
                        com.welab.wefe.board.service.dto.kernel.deep_learning.KernelJob kernelJob = JSONObject.parseObject(x.getTaskConf()).toJavaObject(com.welab.wefe.board.service.dto.kernel.deep_learning.KernelJob.class);
                        kernelJob.env.resume = true;
                        x.setTaskConf(JSON.toJSONString(kernelJob));

                        if (x.getStatus() != TaskStatus.success) {
                            x.setStatus(TaskStatus.wait_run);
                        }
                        taskRepository.save(x);
                    });

        }


        jobs.forEach(y ->
                jobService.updateJob(y, (x) -> {
                    x.setUpdatedBy(input);
                    x.setStatus(JobStatus.wait_run);
                    return x;
                })
        );

        flowActionQueueService.runJob(input, input.getJobId(), project.getProjectType());

        gatewayService.syncToOtherJobMembers(job.getJobId(), input, ResumeJobApi.class);

    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized void stopFlowJob(StopJobApi.Input input) throws StatusCodeWithException {


        List<JobMySqlModel> jobs = jobService.listByJobId(input.getJobId());
        if (jobs.isEmpty()) {
            throw new StatusCodeWithException("未找到相应的任务！", StatusCode.ILLEGAL_REQUEST);
        }
        int finishedJobCount = 0;
        for (JobMySqlModel job : jobs) {
            if (job.getStatus().onStoping() || job.getStatus().finished()) {
                finishedJobCount++;
            }
        }
        // all finished
        if (finishedJobCount == jobs.size()) {
            return;
        }
        JobMySqlModel job = jobs.get(0);
        ProjectMySqlModel project = projectService.findByProjectId(job.getProjectId());

        if (!input.fromGateway() && JobMemberRole.promoter != project.getMyRole()) {
            throw new StatusCodeWithException("只有 promoter 才能暂停任务！", StatusCode.ILLEGAL_REQUEST);
        }

        jobs.forEach(y ->
                jobService.updateJob(y, (x) -> {
                    x.setUpdatedBy(input);
                    x.setStatus(JobStatus.wait_stop);
                    return x;
                })
        );

        projectFlowService.updateFlowStatus(job.getFlowId(), ProjectFlowStatus.stop_on_running);

        flowActionQueueService.stopJob(input, input.getJobId(), project.getProjectType());

        gatewayService.syncToOtherJobMembers(job.getJobId(), input, StopJobApi.class);

    }


    private JobMySqlModel createJob(ProjectFlowMySqlModel flow, String jobId, JobMemberRole myRole) {
        JobMySqlModel job = new JobMySqlModel();
        job.setFederatedLearningType(flow.getFederatedLearningType());
        job.setMyRole(myRole);
        job.setJobId(jobId);
        job.setCreatedBy(CurrentAccount.id());
        job.setName(flow.getFlowName());
        job.setProgress(0);
        job.setStatus(JobStatus.wait_run);
        job.setStatusUpdatedTime(new Date());
        job.setProjectId(flow.getProjectId());
        job.setFlowId(flow.getFlowId());
        job.setGraph(flow.getGraph());

        job = jobRepo.save(job);

        return job;

    }

    private List<TaskMySqlModel> createJobTasks(ProjectMySqlModel project, FlowGraph graph, boolean useCache, String endNodeId,
                                                FederatedLearningType federatedLearningType) throws StatusCodeWithException {

        List<FlowGraphNode> startNodes = graph.getStartNodes();

        if (CollectionUtils.isEmpty(startNodes)) {
            throw new StatusCodeWithException("当前流程没有起始节点，无法运行。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        jobService.setGraphHasCacheResult(graph, useCache);

        List<FlowGraphNode> noCacheNodes = graph
                .getJobSteps(endNodeId)
                .stream()
                .filter(x -> !x.getHasCacheResult())
                .collect(Collectors.toList());

        if (noCacheNodes.isEmpty()) {
            throw new StatusCodeWithException("创建任务失败：没有需要执行的节点，请尝试禁用缓存后重试。", StatusCode.PARAMETER_VALUE_INVALID);
        }

        List<TaskMySqlModel> cacheTasks = new ArrayList<>();
        if (useCache) {
            List<FlowGraphNode> hasCacheNodes = graph
                    .getJobSteps(endNodeId)
                    .stream()
                    .filter(FlowGraphNode::getHasCacheResult)
                    .collect(Collectors.toList());

            for (FlowGraphNode node : hasCacheNodes) {

                if (graph.getJob().getMyRole() == JobMemberRole.arbiter) {
                    if (!Components.needArbiterTask(node.getComponentType())) {
                        continue;
                    }
                }
                if (graph.getFederatedLearningType().equals(FederatedLearningType.mix)) {
                    List<TaskMySqlModel> newTasks = copyMixTaskInfoFromLastJob(graph.getLastJob(), graph.getJob(), node,
                            true);
                    cacheTasks.addAll(newTasks);

                } else {
                    TaskMySqlModel newTask = copyNodeInfoFromLastJob(graph.getLastJob(), graph.getJob(), node, true);
                    cacheTasks.add(newTask);
                }
            }

        }

        KernelJob kernelJob = createKernelJob(graph.getJob(), graph.getMembers(), graph.getJobSteps(endNodeId));

        List<TaskMySqlModel> tasks = new ArrayList<>();

        for (FlowGraphNode node : noCacheNodes) {

            AbstractComponent<?> component = Components.get(node.getComponentType());
            if (component == null) {
                continue;
            }
            if (graph.getJob().getMyRole() == JobMemberRole.arbiter) {
                // need arbiter task
                if (!Components.needArbiterTask(component.taskType())) {
                    continue;
                }
            }

            if (component.hasParams()) {
                if (StringUtil.isEmpty(node.getParams()) || "{}".equals(node.getParams())) {
                    throw new FlowNodeException(node, "该组件的参数尚未保存");
                }
            }

            try {
                addPreTasks(node, tasks, cacheTasks);
                if (federatedLearningType == FederatedLearningType.mix) {
                    List<TaskMySqlModel> subTasks = component.buildMixTask(graph, tasks, kernelJob, node);
                    if (subTasks != null && !subTasks.isEmpty()) {
                        tasks.addAll(subTasks);
                    }
                } else {
                    TaskMySqlModel task = component.buildTask(project, graph, tasks, kernelJob, node);
                    if (task != null) {
                        tasks.add(task);
                    }
                }
            } catch (FlowNodeException e) {
                throw e;
            } catch (Exception e) {
                super.log(e);
                throw new FlowNodeException(node, e.getClass() + " " + e.getMessage());
            }
        }

        /**
         * 如果第一个运行的节点是建模算法节点并且有可用的缓存，则需要将之前中断的任务结果复制到当前任务中。
         * 1. 参数指定使用缓存（useCache == true）
         * 2. 第一个 task 是建模节点
         * 3. last job 不为空，表示该流程之前已经运行过。
         * 4. 自上次创建 Job 后，建模节点未编辑。
         */
        FlowGraphNode firstNode = noCacheNodes.get(0);
        if (useCache && firstNode.getComponentType().isModeling() && graph.getLastJob() != null) {
            if (firstNode.getParamsVersion() < graph.getLastJob().getCreatedTime().getTime()) {
                copyIterationResult(graph.getLastJob(), graph.getJob(), firstNode);
            }
        }

        if (graph.getJob().getMyRole() != JobMemberRole.arbiter) {
            updateDataSetUsageCountInJob(kernelJob);
        }

        return tasks;
    }

    /**
     * Update dataset usage
     */
    private void updateDataSetUsageCountInJob(KernelJob kernelJob) throws StatusCodeWithException {

        HashSet<String> dataSetIds = new HashSet<>();
        for (JobDataSet dataSet : kernelJob.getDataSets()) {
            for (JobDataSet.Member member : dataSet.members) {
                dataSetIds.add(member.dataSetId);
            }
        }

        for (String dataSetId : dataSetIds) {
            dataResourceService.usageCountInJobIncrement(dataSetId);
        }
    }

    /**
     * Copy the result of the failed task to the new job
     */
    private void copyIterationResult(JobMySqlModel oldJob, JobMySqlModel newJob, FlowGraphNode node) {
        if (!node.getComponentType().isModeling()) {
            return;
        }
        if (oldJob.getFederatedLearningType().equals(FederatedLearningType.mix)) {
            copyMixTaskInfoFromLastJob(oldJob, newJob, node, false);
        } else {
            copyNodeInfoFromLastJob(oldJob, newJob, node, false);
        }

    }

    private KernelJob createKernelJob(JobMySqlModel job, List<JobMemberMySqlModel> memberList, List<FlowGraphNode> nodes) throws StatusCodeWithException {

        KernelJob jobInfo = new KernelJob();

        Project project = new Project();
        project.setProjectId(job.getProjectId());

        List<JobDataSet> dataSets = listJobDataSets(job, nodes);

        jobInfo.setFederatedLearningType(job.getFederatedLearningType());
        jobInfo.setProject(project);
        jobInfo.setMembers(Member.forMachineLearning(memberList));

        Member arbiter = jobInfo
                .getMembers()
                .stream()
                .filter(x -> x.getMemberRole() == JobMemberRole.arbiter)
                .findFirst()
                .orElse(null);

        if (arbiter == null) {
            if (job.getFederatedLearningType() == FederatedLearningType.horizontal
                    || job.getFederatedLearningType() == FederatedLearningType.mix) {
                Member promoter = jobInfo
                        .getMembers()
                        .stream()
                        .filter(x -> x.getMemberRole() == JobMemberRole.promoter)
                        .findFirst()
                        .orElse(null);

                if (promoter != null) {
                    arbiter = Member.forMachineLearning(promoter.getMemberId(), JobMemberRole.arbiter);
                    jobInfo.getMembers().add(arbiter);
                }
            }
        }

        jobInfo.setEnv(Env.get());
        jobInfo.setDataSets(dataSets);

        return jobInfo;
    }

    private void addPreTasks(FlowGraphNode node, List<TaskMySqlModel> tasks, List<TaskMySqlModel> cacheTasks) {

        // calculate the start index
        int start = tasks.isEmpty() ? Integer.MIN_VALUE : tasks.get(tasks.size() - 1).getPosition();

        // from cache tasks, find the tasks before the current node and add them to tasks.
        cacheTasks
                .stream()
                .filter(x -> x.getPosition() > start && x.getPosition() < node.getPosition())
                .forEach(tasks::add);

    }

    /**
     * Copy the task information from the previous job
     * <p>
     * 1. copy task
     * 2. copy task_result
     */
    private List<TaskMySqlModel> copyMixTaskInfoFromLastJob(JobMySqlModel oldJob, JobMySqlModel newJob, FlowGraphNode node, boolean copyTask) {
        if (newJob == null) {
            return null;
        }
        List<TaskMySqlModel> oldTasks = taskService.findAll(oldJob.getJobId(), node.getNodeId(), oldJob.getMyRole());
        List<TaskMySqlModel> newTasks = new ArrayList<>();
        for (TaskMySqlModel oldTask : oldTasks) {
            int count = Integer.parseInt(oldTask.getTaskId().split("_")[oldTask.getTaskId().split("_").length - 1]);
            // copy task
            if (copyTask) {
                TaskMySqlModel newTask = new TaskMySqlModel();
                BeanUtils.copyProperties(oldTask, newTask);
                newTask.setId(new TaskMySqlModel().getId());
                newTask.setRole(newJob.getMyRole());
                newTask.setJobId(newJob.getJobId());
                newTask.setDeep(node.getDeep());
                newTask.setPosition(node.getPosition());
                newTask.setTaskId(node.createTaskId(newJob, count));
                newTask.setParentTaskIdList(node.createParentTaskIds(newJob, count));
                taskRepository.save(newTask);
                if (newTask != null) {
                    newTasks.add(newTask);
                }
            }

            List<TaskResultMySqlModel> oldResults = taskResultService.listAllResult(oldTask.getTaskId());
            // copy task_result
            for (TaskResultMySqlModel oldResult : oldResults) {

                TaskResultMySqlModel newResult = new TaskResultMySqlModel();
                BeanUtils.copyProperties(oldResult, newResult);

                newResult.setId(new TaskResultMySqlModel().getId());
                newResult.setRole(newJob.getMyRole());
                newResult.setJobId(newJob.getJobId());
                newResult.setTaskId(node.createTaskId(newJob, count));
                taskResultRepository.save(newResult);
            }

            List<TableDataSetMysqlModel> dataSetModels = tableDataSetService.queryAll(oldJob.getJobId(),
                    node.getComponentType());

            if (CollectionUtils.isNotEmpty(dataSetModels)) {
                for (TableDataSetMysqlModel dataSetModel : dataSetModels) {
                    TableDataSetMysqlModel newDataSetModel = new TableDataSetMysqlModel();
                    BeanUtils.copyProperties(dataSetModel, newDataSetModel);
                    newDataSetModel.setId(new TableDataSetMysqlModel().getId());
                    newDataSetModel.setDerivedFromJobId(newJob.getJobId());
                    newDataSetModel.setDerivedFrom(node.getComponentType());
                    tableDataSetService.save(newDataSetModel);
                }
            }
        }
        return newTasks;
    }

    /**
     * Copy the task information from the previous job
     * <p>
     * 1. copy task
     * 2. copy task_result
     */
    private TaskMySqlModel copyNodeInfoFromLastJob(JobMySqlModel oldJob, JobMySqlModel newJob, FlowGraphNode node,
                                                   boolean copyTask) {
        if (newJob == null) {
            return null;
        }
        TaskMySqlModel oldTask = taskService.findOne(oldJob.getJobId(), node.getNodeId(), oldJob.getMyRole());
        TaskMySqlModel newTask = null;
        // copy task
        if (copyTask) {
            newTask = new TaskMySqlModel();
            BeanUtils.copyProperties(oldTask, newTask);
            newTask.setId(new TaskMySqlModel().getId());
            newTask.setRole(newJob.getMyRole());
            newTask.setJobId(newJob.getJobId());
            newTask.setDeep(node.getDeep());
            newTask.setPosition(node.getPosition());
            newTask.setTaskId(node.createTaskId(newJob));
            newTask.setParentTaskIdList(node.createParentTaskIds(newJob));
            taskRepository.save(newTask);
        }

        List<TaskResultMySqlModel> oldResults = taskResultService.listAllResult(oldTask.getTaskId());
        // copy task_result
        for (TaskResultMySqlModel oldResult : oldResults) {

            TaskResultMySqlModel newResult = new TaskResultMySqlModel();
            BeanUtils.copyProperties(oldResult, newResult);

            newResult.setId(new TaskResultMySqlModel().getId());
            newResult.setRole(newJob.getMyRole());
            newResult.setJobId(newJob.getJobId());
            newResult.setTaskId(node.createTaskId(newJob));

            taskResultRepository.save(newResult);
        }

        TableDataSetMysqlModel dataSetModel = tableDataSetService.query(oldJob.getJobId(), node.getComponentType());
        if (dataSetModel != null) {
            TableDataSetMysqlModel newDataSetModel = new TableDataSetMysqlModel();
            BeanUtils.copyProperties(dataSetModel, newDataSetModel);
            newDataSetModel.setId(new TableDataSetMysqlModel().getId());
            newDataSetModel.setDerivedFromJobId(newJob.getJobId());
            newDataSetModel.setDerivedFrom(node.getComponentType());
            tableDataSetService.save(newDataSetModel);
        }

        return newTask;
    }


    /**
     * save the list of members participating in this job
     */
    private List<JobMemberMySqlModel> listJobMembers(String projectId, String flowId, String jobId, JobArbiterInfo jobArbiterInfo, boolean isOotMode) throws StatusCodeWithException {

        List<ProjectFlowNodeMySqlModel> nodes = projectFlowNodeService.listAboutLoadDataSetNodes(flowId);

        List<JobMemberMySqlModel> jobMembers = new ArrayList<>();

        String promoterId = null;
        for (ProjectFlowNodeMySqlModel node : nodes) {
            List<? extends AbstractDataSetItem> dataSetItemList = null;

            AbstractDataIOParam params = (AbstractDataIOParam) Components
                    .get(node.getComponentType())
                    .deserializationParam(node.getParams());

            switch (node.getComponentType()) {
                case DataIO:
                case ImageDataIO:
                    dataSetItemList = params.getDataSetList();
                    break;
                case Oot:
                    OotComponent.Params ootParams = (OotComponent.Params) params;
                    dataSetItemList = StringUtil.isNotEmpty(ootParams.getJobId())
                            ? params.getDataSetList()
                            : dataSetItemList;
                    break;
                default:
                    StatusCode.UNEXPECTED_ENUM_CASE.throwException();
            }

            if (CollectionUtils.isEmpty(dataSetItemList)) {
                continue;
            }

            for (AbstractDataSetItem item : dataSetItemList) {
                boolean memberExisted = jobMembers.stream()
                        .anyMatch(x ->
                                x.getMemberId().equals(item.getMemberId())
                                        && x.getJobRole().equals(item.getMemberRole())
                        );

                if (memberExisted) {
                    continue;
                }

                JobMemberMySqlModel jobMember = new JobMemberMySqlModel();
                jobMember.setDataSetId(item.getDataSetId());
                jobMember.setJobRole(item.getMemberRole());
                jobMember.setMemberId(item.getMemberId());

                jobMembers.add(jobMember);

                if (jobMember.getJobRole() == JobMemberRole.promoter
                        && jobMember.getMemberId().equals(CacheObjects.getMemberId())) {
                    promoterId = jobMember.getMemberId();
                }
            }
        }

        if (jobArbiterInfo.isHasArbiter()) {
            JobMemberMySqlModel arbiter = new JobMemberMySqlModel();
            arbiter.setJobRole(JobMemberRole.arbiter);
            arbiter.setMemberId(StringUtils.isBlank(promoterId) ? jobArbiterInfo.getArbiterMemberId() : promoterId);
            jobMembers.add(arbiter);
        }

        jobMembers.forEach(x -> {
            x.setCreatedBy(CurrentAccount.id());
            x.setProjectId(projectId);
            x.setFlowId(flowId);
            x.setJobId(jobId);

            jobMemberRepo.save(x);
        });

        // Sort to ensure that the promoter is at the top
        jobMembers.sort(Comparator.comparingInt(o -> o.getJobRole().getIndex()));

        return jobMembers;
    }


    /**
     * Sort out all data sets used in the job
     */
    private List<JobDataSet> listJobDataSets(JobMySqlModel job, List<FlowGraphNode> nodes) throws StatusCodeWithException {

        List<JobDataSet> jobDataSetList = new ArrayList<>();

        List<ComponentType> dataSetComponentTypeList = Arrays.asList(
                ComponentType.DataIO,
                ComponentType.HorzXGBoostValidationDataSetLoader,
                ComponentType.VertXGBoostValidationDataSetLoader,
                ComponentType.HorzLRValidationDataSetLoader,
                ComponentType.VertLRValidationDataSetLoader,
                ComponentType.Oot
        );

        for (FlowGraphNode node : nodes) {
            if (!dataSetComponentTypeList.contains(node.getComponentType())) {
                continue;
            }

            List<DataIOComponent.DataSetItem> dataSetItemList;
            if (ComponentType.Oot.equals(node.getComponentType())) {
                OotComponent.Params params = (OotComponent.Params) node.getParamsModel();
                dataSetItemList = params.getDataSetList();
            } else {
                DataIOComponent.Params params = (DataIOComponent.Params) node.getParamsModel();
                dataSetItemList = params.getDataSetList();
            }

            if (CollectionUtils.isEmpty(dataSetItemList)) {
                continue;
            }

            JobDataSet dataSet = new JobDataSet();
            dataSet.componentType = node.getComponentType();
            dataSet.nodeId = node.getNodeId();
            dataSet.taskId = FlowGraphNode.createTaskId(job, node.getComponentType(), node.getNodeId());
            dataSet.members = new ArrayList<>();

            for (DataIOComponent.DataSetItem item : dataSetItemList) {
                JobDataSet.Member member = new JobDataSet.Member();
                member.memberId = item.getMemberId();
                member.memberRole = item.getMemberRole();
                member.dataSetId = item.getDataSetId();

                TableDataSetOutputModel dataSetInfo = tableDataSetService.findDataSetFromLocalOrUnion(member.memberId, member.dataSetId);
                if (dataSetInfo != null) {
                    member.dataSetRows = dataSetInfo.getTotalDataCount();
                    member.dataSetFeatures = dataSetInfo.getFeatureCount();
                }

                dataSet.members.add(member);

            }

            jobDataSetList.add(dataSet);
        }

        return jobDataSetList;
    }
}
