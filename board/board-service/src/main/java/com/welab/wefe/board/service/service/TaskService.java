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

import com.welab.wefe.board.service.api.project.flow.QueryDataIoTaskConfigApi;
import com.welab.wefe.board.service.api.project.flow.QueryDataIoTaskFeaturesApi;
import com.welab.wefe.board.service.api.project.job.task.DetailApi;
import com.welab.wefe.board.service.component.OotComponent;
import com.welab.wefe.board.service.database.entity.job.*;
import com.welab.wefe.board.service.database.repository.JobRepository;
import com.welab.wefe.board.service.database.repository.TaskRepository;
import com.welab.wefe.board.service.dto.entity.DataIoTaskFeatureInfoOutputModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.TaskStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author seven.zeng
 */
@Service
public class TaskService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    TaskRepository taskRepo;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobMemberService jobMemberService;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private ProjectFlowService projectFlowService;

    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;
    @Autowired
    private JobRepository jobRepository;

    /**
     * 找到所有包含网格搜索的模型结果
     */
    public List<TaskMySqlModel> listTaskWithGridSearch(String jobId){
        return taskRepo.findTaskWithGridSearch(jobId);
    }

    /**
     * Query all execution records of a node
     */
    public List<TaskMySqlModel> findTaskHistory(String flowId, String flowNodeId, JobMemberRole role) {
        return taskRepo
                .findAll(
                        Where
                                .create()
                                .equal("flowId", flowId)
                                .equal("flowNodeId", flowNodeId)
                                .equal("role", role)
                                .notEqual("status", TaskStatus.wait_run)
                                .orderBy("createdTime", OrderBy.desc)
                                .build(TaskMySqlModel.class)
                );
    }


    public TaskMySqlModel findOne(DetailApi.Input input) throws StatusCodeWithException {
        if (StringUtil.isNotEmpty(input.getTaskId())) {
            return findOne(input.getTaskId());
        } else {

            String jobId = input.getJobId();
            ProjectMySqlModel project;
            if (StringUtil.isEmpty(jobId)) {
                if (StringUtil.isEmpty(input.getFlowNodeId())) {
                    StatusCode
                            .PARAMETER_VALUE_INVALID
                            .throwException("job_id 不传的时候 flow_id 必须要指定");
                }

                // 通过 flow_id 获取最后一个 job
                ProjectFlowMySqlModel flow = projectFlowService.findOne(input.getFlowNodeId());
                project = projectService.findByProjectId(flow.getProjectId());
                JobMySqlModel job = jobRepository.findLastByFlowId(input.getFlowId(), project.getMyRole().name());
                if (job == null) {
                    return null;
                }
                jobId = job.getJobId();
            } else {
                project = projectService.findProjectByJobId(input.getJobId());
            }

            if (project == null) {
                return null;
            }

            return taskRepo.findOne(jobId, input.getFlowNodeId(), project.getMyRole().name());
        }
    }
    
    public List<TaskMySqlModel> findAll(DetailApi.Input input) {
        ProjectMySqlModel project = projectService.findProjectByJobId(input.getJobId());
        if (project == null) {
            return null;
        }
        return findAll(input.getJobId(), input.getFlowNodeId(), project.getMyRole());
    }

    public TaskMySqlModel findOne(String taskId) {
        return taskRepo
                .findOne(
                        Where
                                .create()
                                .equal("taskId", taskId)
                                .notEqual("role", JobMemberRole.arbiter)
                                .build(TaskMySqlModel.class)
                )
                .orElse(null);
    }

    public TaskMySqlModel findOne(String jobId, String flowNodeId, JobMemberRole role) {
        return taskRepo
                .findOne(
                        Where
                                .create()
                                .equal("jobId", jobId)
                                .equal("flowNodeId", flowNodeId)
                                .equal("role", role)
                                .build(TaskMySqlModel.class)
                )
                .orElse(null);
    }

    public List<TaskMySqlModel> findAll(String jobId, String flowNodeId, JobMemberRole role) {
        return taskRepo
                .findAll(
                        Where
                                .create()
                                .equal("jobId", jobId)
                                .equal("flowNodeId", flowNodeId)
                                .equal("role", role)
                                .build(TaskMySqlModel.class)
                );
    }

    /**
     * Due to the existence of arbit, there may be two pieces of data here.
     */
    public List<TaskMySqlModel> findAll(String jobId, String flowNodeId) {
        return taskRepo
                .findAll(
                        Where
                                .create()
                                .equal("jobId", jobId)
                                .equal("flowNodeId", flowNodeId)
                                .build(TaskMySqlModel.class)
                );
    }

    /**
     * Get all tasks under job
     */
    public List<TaskMySqlModel> listByJobId(String jobId, JobMemberRole role) {

        Specification<TaskMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("role", role)
                .orderBy("position", OrderBy.asc)
                .build(TaskMySqlModel.class);

        return taskRepo.findAll(where);
    }

    public TaskMySqlModel updateTask(TaskMySqlModel model, Function<TaskMySqlModel, TaskMySqlModel> func) {

        if (model == null) {
            return null;
        }

        model = func.apply(model);
        model.setUpdatedBy(CurrentAccountUtil.get().getId());

        return taskRepo.save(model);
    }

    public TaskMySqlModel findByParentTaskId(String parentTaskId) {

        return taskRepo.findOne("parentTaskId", parentTaskId, TaskMySqlModel.class);
    }

    /**
     * Find out all tasks that meet the conditions
     */
    public List<TaskMySqlModel> findAll(String jobId, String flowId, ComponentType componentType) {
        Specification<TaskMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("flowId", flowId)
                .equal("taskType", componentType)
                .orderBy("createdTime", OrderBy.desc)
                .build(TaskMySqlModel.class);

        return taskRepo.findAll(where);
    }

    /**
     * get config of DataIO task
     */
    public JObject findDataIoTaskConfig(QueryDataIoTaskConfigApi.Input input) {
        TaskMySqlModel taskMySqlModel = findDataIoTask(input.getJobId(), input.getRole());
        if (null == taskMySqlModel) {
            return JObject.create();
        }

        return JObject.create(taskMySqlModel.getTaskConf());
    }

    public TaskMySqlModel findDataIoTask(String jobId, JobMemberRole role) {
        List<TaskMySqlModel> taskMySqlModelList = listByJobId(jobId, role);
        if (CollectionUtils.isEmpty(taskMySqlModelList)) {
            return null;
        }

        return taskMySqlModelList.stream().filter(x -> OotComponent.DATA_IO_COMPONENT_TYPE_LIST.contains(x.getTaskType())).findFirst().orElse(null);
    }

    /**
     * According to JobId, role and task ID,
     * find out the list of all nodes in the same branch of task id.
     */
    public List<TaskMySqlModel> findHomologousBranchByJobId(String jobId, JobMemberRole role, String taskId) throws StatusCodeWithException {
        JobMySqlModel jobMySqlModel = jobService.findByJobId(jobId, role);
        if (null == jobMySqlModel) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到任务信息。");
        }

        List<TaskMySqlModel> totalTaskList = listByJobId(jobId, role);

        return baseFindHomologousBranch(totalTaskList, taskId);
    }

    /**
     * Find out the branch node of the specified task id from the list of all tasks
     */
    public List<TaskMySqlModel> baseFindHomologousBranch(List<TaskMySqlModel> totalTaskList, String taskId) {
        // List of all nodes in the same branch
        List<TaskMySqlModel> resultTaskList = new ArrayList<>();
        if (CollectionUtils.isEmpty(totalTaskList)) {
            return resultTaskList;
        }

        // First find out all the child nodes under the taskId
        List<TaskMySqlModel> totalSubTaskMysqlModelList = new ArrayList<>();
        findTotalSubTaskMysqlModelList(totalSubTaskMysqlModelList, totalTaskList, taskId);
        // The child node is not empty to prove that there are child nodes under the node,
        // then take the last child node as the end node and then go forward to get all the parent nodes of its branch.
        if (CollectionUtils.isNotEmpty(totalSubTaskMysqlModelList)) {
            taskId = totalSubTaskMysqlModelList.get(totalSubTaskMysqlModelList.size() - 1).getTaskId();
        }

        findTotalParentTaskMysqlModelList(resultTaskList, totalTaskList, taskId);
        // Since the returned parent node result is from bottom to top,
        // it needs to be reversed
        // (normally it should be sorted in ascending order according to the deep field,
        // but since the incoming original list is already sorted according to the deep field,
        // it can be reversed directly)
        Collections.reverse(resultTaskList);
        return resultTaskList;
    }


    /**
     * Query the entry feature information of DataIo
     */
    public List<DataIoTaskFeatureInfoOutputModel> findDataIoTaskFeatures(QueryDataIoTaskFeaturesApi.Input input) throws StatusCodeWithException {

        boolean isOotMode = StringUtil.isNotEmpty(input.getJobId());
        return isOotMode ? findDataIoTaskFeaturesWithOot(input) : findDataIoTaskFeaturesWithNonOot(input);
    }


    /**
     * Query DataIo's entry feature information (oot mode)
     * <p>
     * In oot mode, the entry feature list of other members is stored in its own database,
     * so it is necessary to send a request to get the entry feature list to the other party's system.
     */
    private List<DataIoTaskFeatureInfoOutputModel> findDataIoTaskFeaturesWithOot(QueryDataIoTaskFeaturesApi.Input input) throws StatusCodeWithException {
        List<DataIoTaskFeatureInfoOutputModel> resultList = new ArrayList<>();

        JobMySqlModel jobMySqlModel = jobService.findByJobId(input.getJobId(), JobMemberRole.promoter);
        if (null == jobMySqlModel) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到相关promoter相应任务信息，请确认该请求是由promoter发起。");
        }

        List<JobMemberMySqlModel> jobMemberMySqlModelList = jobMemberService.findListByJobId(input.getJobId());
        if (CollectionUtils.isEmpty(jobMemberMySqlModelList)) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "不存在任何成员信息。");
        }
        // Filter arbiter role
        jobMemberMySqlModelList = jobMemberMySqlModelList.stream().filter(x -> !JobMemberRole.arbiter.equals(x.getJobRole())).collect(Collectors.toList());
        // Query designated members
        if (StringUtil.isNotEmpty(input.getMemberId())) {
            jobMemberMySqlModelList = jobMemberMySqlModelList.stream().filter(x -> x.getMemberId().equals(input.getMemberId())).collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(jobMemberMySqlModelList)) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到该成员的任务信息。");
        }

        DataIoTaskFeatureInfoOutputModel dataIoTaskFeatureInfoOutputModel = null;
        // Obtain the job configuration of the provider
        for (JobMemberMySqlModel jobMemberMySqlModel : jobMemberMySqlModelList) {
            dataIoTaskFeatureInfoOutputModel = new DataIoTaskFeatureInfoOutputModel();
            dataIoTaskFeatureInfoOutputModel.setMemberId(jobMemberMySqlModel.getMemberId());
            dataIoTaskFeatureInfoOutputModel.setRole(jobMemberMySqlModel.getJobRole());
            dataIoTaskFeatureInfoOutputModel.setDataSetId(jobMemberMySqlModel.getDataSetId());
            String memberName = CacheObjects.getMemberName(jobMemberMySqlModel.getMemberId());
            dataIoTaskFeatureInfoOutputModel.setMemberName(memberName);
            QueryDataIoTaskConfigApi.Input queryTaskConfigInput = new QueryDataIoTaskConfigApi.Input();
            queryTaskConfigInput.setJobId(input.getJobId());
            queryTaskConfigInput.setRole(jobMemberMySqlModel.getJobRole());
            try {
                // The promoter can directly query the local
                if (JobMemberRole.promoter.equals(jobMemberMySqlModel.getJobRole())) {
                    TaskMySqlModel taskMySqlModel = findDataIoTask(input.getJobId(), jobMemberMySqlModel.getJobRole());
                    if (null != taskMySqlModel) {
                        List<String> dataIoFeatures = JObject.parseArray(JObject.create(taskMySqlModel.getTaskConf()).getStringByPath("params.need_features"))
                                .toJavaList(String.class);
                        dataIoTaskFeatureInfoOutputModel.setFeatures(dataIoFeatures);
                    }
                } else if (JobMemberRole.provider.equals(jobMemberMySqlModel.getJobRole())) {
                    // The provider needs to send a request to the other party to obtain
                    Object result = gatewayService.callOtherMemberBoard(
                            jobMemberMySqlModel.getMemberId(),
                            JobMemberRole.promoter,
                            QueryDataIoTaskConfigApi.class,
                            queryTaskConfigInput,
                            Object.class
                    );

                    JObject data = JObject.create(result);
                    if (null == data || data.isEmpty()) {
                        throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "获取成员[" + memberName + "]的入模特征为空。");
                    }

                    // The feature column selected during the original dataIO modeling
                    List<String> dataIoFeatures = JObject.parseArray(data.getStringByPath("params.need_features")).toJavaList(String.class);
                    dataIoTaskFeatureInfoOutputModel.setFeatures(dataIoFeatures);
                }
            } catch (StatusCodeWithException e) {
                LOG.error("获取成员[" + memberName + "]的特征异常：", e);
                throw e;
            } catch (Exception e) {
                LOG.error("获取成员[" + memberName + "]的特征异常：", e);
                throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "获取成员[" + memberName + "]的入模特征异常: " + e.getMessage());
            }

            resultList.add(dataIoTaskFeatureInfoOutputModel);
        }
        return resultList;
    }


    /**
     * Query DataIo's entry feature information (non-oot mode)
     */
    private List<DataIoTaskFeatureInfoOutputModel> findDataIoTaskFeaturesWithNonOot(QueryDataIoTaskFeaturesApi.Input input) throws StatusCodeWithException {
        List<DataIoTaskFeatureInfoOutputModel> resultList = new ArrayList<>();

        ProjectFlowMySqlModel projectFlowMySqlModel = projectFlowService.findOne(input.getFlowId());
        if (null == projectFlowMySqlModel) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "流程信息不存在。");
        }
        List<ProjectFlowNodeMySqlModel> projectFlowNodeMySqlModelList = projectFlowNodeService.findNodesByFlowId(input.getFlowId());
        if (CollectionUtils.isEmpty(projectFlowNodeMySqlModelList)) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "流程节点信息为空。");
        }
        // Find the dataIo node of the process
        ProjectFlowNodeMySqlModel dataIoFlowNode = projectFlowNodeMySqlModelList.stream()
                .filter(x -> OotComponent.DATA_IO_COMPONENT_TYPE_LIST.contains(x.getComponentType())).findFirst().orElse(null);
        if (null == dataIoFlowNode || StringUtil.isEmpty(dataIoFlowNode.getParams())) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "请保存[选择数据集]节点信息。");
        }

        List<JObject> dataSetList = JObject.create(dataIoFlowNode.getParams()).getJSONList("dataset_list");
        DataIoTaskFeatureInfoOutputModel dataIoTaskFeatureInfoOutputModel = null;
        // If memberId is empty, it means to query all
        boolean queryTotal = StringUtil.isEmpty(input.getMemberId());
        for (JObject dataSet : dataSetList) {
            String memberId = dataSet.getString("member_id");
            String memberRole = dataSet.getString("member_role");
            String dataSetId = dataSet.getString("data_set_id");
            // Ignore arbiter
            if (JobMemberRole.arbiter.name().equals(memberRole)) {
                continue;
            }
            dataIoTaskFeatureInfoOutputModel = new DataIoTaskFeatureInfoOutputModel();
            dataIoTaskFeatureInfoOutputModel.setMemberId(memberId);
            dataIoTaskFeatureInfoOutputModel.setMemberName(CacheObjects.getMemberName(memberId));
            dataIoTaskFeatureInfoOutputModel.setDataSetId(dataSetId);
            dataIoTaskFeatureInfoOutputModel.setRole(JobMemberRole.valueOf(memberRole));
            List<String> features = JObject.parseArray(dataSet.getString("features")).toJavaList(String.class);
            dataIoTaskFeatureInfoOutputModel.setFeatures(features);
            if (queryTotal) {
                resultList.add(dataIoTaskFeatureInfoOutputModel);
            } else if (input.getMemberId().equals(memberId)) {
                resultList.add(dataIoTaskFeatureInfoOutputModel);
                break;
            }
        }

        return resultList;
    }

    /**
     * Query the list of all child nodes up to the end node (excluding the child nodes specified by startTaskId)
     * <p>
     * Currently only supports single-branch node query, the sub-nodes of multiple branches are too complicated, and currently only the child nodes of one branch are supported from the modeling node)
     *
     * @param resultList    result list
     * @param totalTaskList List of all task results under a jobId
     * @param startTaskId   List of node IDs to start querying
     */
    private void findTotalSubTaskMysqlModelList(List<TaskMySqlModel> resultList, List<TaskMySqlModel> totalTaskList, final String startTaskId) {
        TaskMySqlModel subTaskMySqlModel = null;
        String newStartTaskId = null;
        for (TaskMySqlModel taskMySqlModel : totalTaskList) {
            String parentTaskIdList = taskMySqlModel.getParentTaskIdList();
            if (StringUtil.isNotEmpty(parentTaskIdList) && Arrays.asList(parentTaskIdList.split(",")).contains(startTaskId)) {
                resultList.add(taskMySqlModel);
                subTaskMySqlModel = taskMySqlModel;
                newStartTaskId = taskMySqlModel.getTaskId();
                break;
            }
        }
        // Prove that the child node was found
        if (null != subTaskMySqlModel) {
            findTotalSubTaskMysqlModelList(resultList, totalTaskList, newStartTaskId);
        }
    }

    /**
     * Query the list of all parent nodes of the specified node
     * (the returned result list contains the node itself specified by startTaskId)
     *
     * @param resultList    Result list
     * @param totalTaskList List of all task results under a jobId
     * @param startTaskIds  List of node IDs to start the query (multiple separated by commas)
     */
    private void findTotalParentTaskMysqlModelList(List<TaskMySqlModel> resultList, List<TaskMySqlModel> totalTaskList, final String startTaskIds) {
        List<TaskMySqlModel> taskMysqlModelList = totalTaskList.stream().filter(x -> Arrays.asList(startTaskIds.split(",")).contains(x.getTaskId())
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(taskMysqlModelList)) {
            resultList.addAll(taskMysqlModelList);
            List<String> parentTaskIdList = new ArrayList<>();
            for (TaskMySqlModel taskMySqlModel : taskMysqlModelList) {
                if (StringUtil.isNotEmpty(taskMySqlModel.getParentTaskIdList())) {
                    parentTaskIdList.addAll(Stream.of(taskMySqlModel.getParentTaskIdList().split(",")).collect(Collectors.toList()));
                }
            }
            // No parent node ID proof has reached the top
            if (CollectionUtils.isEmpty(parentTaskIdList)) {
                return;
            }
            String parentTaskIds = parentTaskIdList.stream().collect(Collectors.joining(","));
            findTotalParentTaskMysqlModelList(resultList, totalTaskList, parentTaskIds);
        }
    }


}
