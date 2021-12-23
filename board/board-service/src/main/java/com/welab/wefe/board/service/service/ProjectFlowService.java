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

package com.welab.wefe.board.service.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.project.flow.*;
import com.welab.wefe.board.service.api.project.job.OnJobFinishedApi;
import com.welab.wefe.board.service.api.project.modeling.DetailApi;
import com.welab.wefe.board.service.api.project.modeling.QueryApi;
import com.welab.wefe.board.service.component.Components;
import com.welab.wefe.board.service.database.entity.flow.FlowTemplateMySqlModel;
import com.welab.wefe.board.service.database.entity.job.*;
import com.welab.wefe.board.service.database.repository.JobRepository;
import com.welab.wefe.board.service.database.repository.ProjectFlowNodeRepository;
import com.welab.wefe.board.service.database.repository.ProjectFlowRepository;
import com.welab.wefe.board.service.database.repository.TaskResultRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.job.ProjectFlowNodeOutputModel;
import com.welab.wefe.board.service.dto.entity.job.TaskResultOutputModel;
import com.welab.wefe.board.service.dto.entity.modeling_config.ModelingInfoOutputModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectFlowListOutputModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectFlowProgressOutputModel;
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Service
public class ProjectFlowService extends AbstractService {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FlowTemplateService flowTemplateService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;
    @Autowired
    private JobService jobService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private ProjectFlowRepository projectFlowRepo;
    @Autowired
    private ProjectFlowNodeRepository projectFlowNodeRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private TaskResultRepository taskResultRepository;

    @Autowired
    private ModelOotRecordService modelOotRecordService;

    /**
     * delete flow
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void delete(DeleteApi.Input input) throws StatusCodeWithException {
        ProjectFlowMySqlModel flow = findOne(input.getFlowId());
        if (flow == null) {
            return;
        }

        OnlineDemoBranchStrategy.hackOnDelete(input, flow, "只能删除自己创建的流程。");

        ProjectMySqlModel project = projectService.findByProjectId(flow.getProjectId());

        if (!input.fromGateway() && !flow.getCreatedBy().equals(CurrentAccount.id())) {
            throw new StatusCodeWithException("非法操作", StatusCode.PARAMETER_VALUE_INVALID);
        }
        flow.setDeleted(true);
        flow.setUpdatedBy(input);
        projectFlowRepo.save(flow);

        projectService.updateFlowStatusStatistics(flow.getProjectId());

        gatewayService.syncToNotExistedMembers(project.getProjectId(), input, DeleteApi.class);
    }

    /**
     * create flow
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized String addFlow(AddFlowApi.Input input) throws StatusCodeWithException {

        ProjectMySqlModel project = projectService.findByProjectId(input.getProjectId());

        if (!input.fromGateway() && project.getMyRole() != JobMemberRole.promoter) {
            throw new StatusCodeWithException("只有 promoter 才能创建流程", StatusCode.ILLEGAL_REQUEST);
        }

        ProjectMemberMySqlModel member = projectMemberService.findOneByMemberId(input.getProjectId(),
                CacheObjects.getMemberId(), JobMemberRole.promoter);
        if (!input.fromGateway()) {
            if (member == null || member.isExited()) {
                throw new StatusCodeWithException("非项目成员不能新建流程", StatusCode.ILLEGAL_REQUEST);
            }
        }

        if (!input.fromGateway() && member.getAuditStatus() != AuditStatus.agree) {
            throw new StatusCodeWithException("非正式成员不能新建流程", StatusCode.ILLEGAL_REQUEST);
        }

        if (!input.fromGateway()) {
            input.setFlowId(UUID.randomUUID().toString().replaceAll("-", ""));
        }

        ProjectFlowMySqlModel flow = new ProjectFlowMySqlModel();
        flow.setFederatedLearningType(input.getFederatedLearningType());
        flow.setCreatedBy(input);
        flow.setProjectId(input.getProjectId());
        flow.setFlowId(input.getFlowId());
        flow.setFlowName(input.getName());
        flow.setFlowDesc(input.getDesc());
        flow.setFlowStatus(ProjectFlowStatus.editing);
        flow.setMyRole(input.fromGateway() ? project.getMyRole() : JobMemberRole.promoter);
        flow.setCreatorMemberId(input.fromGateway() ? input.callerMemberInfo.getMemberId() : CacheObjects.getMemberId());

        if (StringUtils.isNotBlank(input.getTemplateId())) {
            FlowTemplateMySqlModel template = flowTemplateService.findById(input.getTemplateId());
            if (template == null) {
                throw new StatusCodeWithException("未找到相应的流程模板！", StatusCode.ILLEGAL_REQUEST);
            } else {
                flow.setGraph(template.getGraph());
                flow.setFederatedLearningType(input.isOotMode() ? input.getFederatedLearningType() : template.getFederatedLearningType());

            }
        }
        projectFlowRepo.save(flow);

        projectService.updateFlowStatusStatistics(input.getProjectId());

        gatewayService.syncToOtherFormalProjectMembers(flow.getProjectId(), input, AddFlowApi.class);

        if (!input.fromGateway() && StringUtils.isNotBlank(input.getTemplateId())) {
            UpdateFlowGraphApi.Input updateFlowGraphInput = new UpdateFlowGraphApi.Input();
            updateFlowGraphInput.setFlowId(flow.getFlowId());
            updateFlowGraphInput.setGraph(flow.getGraph());
            updateFlowGraph(updateFlowGraphInput);
        }

        return flow.getFlowId();
    }

    /**
     * update flow - basic info
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void updateFlowBaseInfo(UpdateFlowBaseInfoApi.Input input) throws StatusCodeWithException {
        ProjectFlowMySqlModel flow = projectFlowRepo.findOne("flowId", input.getFlowId(), ProjectFlowMySqlModel.class);
        if (flow == null) {
            throw new StatusCodeWithException("未找到该流程", StatusCode.ILLEGAL_REQUEST);
        }
        if (input.getFederatedLearningType() != null
                && flow.getFederatedLearningType() != input.getFederatedLearningType()) {
            throw new StatusCodeWithException("训练类型不允许更改", StatusCode.ILLEGAL_REQUEST);
        }
//        List<ProjectFlowNodeMySqlModel> nodes = projectFlowNodeService.findNodesByFlowId(flow.getFlowId());
//        if (nodes != null && !nodes.isEmpty()) {
//            for (ProjectFlowNodeMySqlModel node : nodes) {
//                if (node.getComponentType().getFederatedLearningTypes() != null && !node.getComponentType()
//                        .getFederatedLearningTypes().contains(input.getFederatedLearningType())) {
//                    throw new StatusCodeWithException("训练类型选择错误，请先移除组件 【" + node.getComponentType().getLabel() + "】",
//                            StatusCode.ILLEGAL_REQUEST);
//                }
//            }
//        }
//        flow.setFederatedLearningType(input.getFederatedLearningType());
        flow.setFlowName(input.getName());
        flow.setFlowDesc(input.getDesc());
        flow.setUpdatedBy(input);

        projectFlowRepo.save(flow);

        gatewayService.syncToOtherFormalProjectMembers(flow.getProjectId(), input, UpdateFlowBaseInfoApi.class);
    }

    /**
     * update flow graph
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void updateFlowGraph(UpdateFlowGraphApi.Input input) throws StatusCodeWithException {

        ProjectFlowMySqlModel flow = findOne(input.getFlowId());
        if (flow == null) {
            throw new StatusCodeWithException("未找到相应的流程！", StatusCode.ILLEGAL_REQUEST);
        }

        flow.setGraph(input.getGraph());
        flow.setUpdatedBy(input);
        projectFlowRepo.save(flow);

        updateFlowStatus(input.getFlowId(), ProjectFlowStatus.editing);

        // Create a component corresponding to the flow
        JSONObject graph = JSON.parseObject(input.getGraph());
        JSONArray nodes = graph.getJSONArray("nodes");
        JSONArray edges = graph.getJSONArray("edges");

        // List of nodes to save to the database
        List<ProjectFlowNodeMySqlModel> newNodes = new ArrayList<>();

        // create ProjectFlowNodeMySqlModel
        for (int i = 0; i < nodes.size(); i++) {

            ProjectFlowNodeMySqlModel node = graphNodeToFlowNode(
                    flow.getProjectId(),
                    flow.getFlowId(),
                    nodes.getJSONObject(i)
            );

            if (node == null) {
                continue;
            }

            newNodes.add(node);
        }

        Map<String, TreeSet<String>> parentNodeIdSetMap = new HashMap<>();

        // update parent node id
        for (int i = 0; i < edges.size(); i++) {
            JSONObject edge = edges.getJSONObject(i);

            String source = edge.getString("source");
            String target = edge.getString("target");

            ProjectFlowNodeMySqlModel node = newNodes
                    .stream()
                    .filter(x -> target.equals(x.getNodeId()))
                    .findFirst()
                    .orElse(null);

            if (node != null) {
                node.setStartNode(false);

                if ("start".equals(source)) {
                    node.setStartNode(true);
                } else {
                    if (!parentNodeIdSetMap.containsKey(node.getNodeId())) {
                        parentNodeIdSetMap.put(node.getNodeId(), new TreeSet<>());
                    }
                    TreeSet<String> parentIds = parentNodeIdSetMap.get(node.getNodeId());
                    parentIds.add(source);
                }

            }
        }

        // Save all nodes
        newNodes.forEach(x -> {
            String beforeParentIds = x.getParentNodeIdList();
            String nowParentIds = parentNodeIdSetMap.containsKey(x.getNodeId())
                    ? StringUtils.join(parentNodeIdSetMap.get(x.getNodeId()), ",")
                    : null;

            // If the parent node changes, reset its parameter version number.
            if (!Objects.equals(beforeParentIds, nowParentIds)) {
                x.setParamsVersion(System.currentTimeMillis());
            }

            x.setParentNodeIdList(nowParentIds);
            projectFlowNodeRepository.save(x);
        });


        // Remove nodes that are not in the graph
        List<String> nodeIds = newNodes.stream().map(x -> x.getNodeId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(nodeIds)) {
            projectFlowNodeRepository.deleteNotInNodeIds(flow.getFlowId(), nodeIds);
        }

        gatewayService.syncToOtherFormalProjectMembers(flow.getProjectId(), input, UpdateFlowGraphApi.class);

    }


    /**
     * Create a ProjectFlowNodeMySqlModel object according to the node information transmitted from the front
     */
    private ProjectFlowNodeMySqlModel graphNodeToFlowNode(String projectId, String flowId, JSONObject input) {
        String nodeId = input.getString("id");
        JSONObject data = input.getJSONObject("data");

        if ("system".equals(data.getString("nodeType"))) {
            return null;
        }

        ProjectFlowNodeMySqlModel node = projectFlowNodeRepository.findByNodeId(flowId, nodeId);

        // is new
        if (node == null) {
            node = new ProjectFlowNodeMySqlModel();
            node.setCreatedBy(CurrentAccount.id());
        } else {
            node.setUpdatedBy(CurrentAccount.id());
        }

        node.setComponentType(ComponentType.valueOf(data.getString("componentType")));
        node.setFlowId(flowId);
        node.setNodeId(nodeId);
        node.setProjectId(projectId);


        return node;

    }


    public ProjectFlowMySqlModel findOne(String flowId) {
        return projectFlowRepo.findOne("flowId", flowId, ProjectFlowMySqlModel.class);
    }

    public PagingOutput<ProjectFlowListOutputModel> query(FlowQueryApi.Input input) {

        Specification<ProjectFlowMySqlModel> where = Where
                .create()
                .equal("projectId", input.getProjectId())
                .equal("deleted", input.isDeleted())
                .in("flowId", input.getFlowIdList())
                .build(ProjectFlowMySqlModel.class);

        PagingOutput<ProjectFlowListOutputModel> page = projectFlowRepo.paging(where, input, ProjectFlowListOutputModel.class);
        page
                .getList()
                .forEach(x -> {

                    JobMySqlModel lastJob = jobRepository.findLastByFlowId(x.getFlowId(), x.getMyRole().name());
                    if (lastJob != null) {
                        x.setJobProgress(lastJob.getProgress());
                    }
                    x.setIsCreator(CacheObjects.isCurrentMemberAccount(x.getCreatedBy()));
                });
        return page;
    }


    public List<ProjectFlowMySqlModel> findFlowsByProjectId(String projectId) {
        Specification<ProjectFlowMySqlModel> where = Where
                .create()
                .equal("projectId", projectId)
                .build(ProjectFlowMySqlModel.class);

        return projectFlowRepo.findAll(where);
    }

    /**
     * copy flow
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void copy(CopyFlowApi.Input input) throws StatusCodeWithException {

        List<ProjectMemberMySqlModel> projectMembers = projectMemberService.listFormalProjectMembers(input.getTargetProjectId());
        ProjectMemberMySqlModel targetPromoterProjectMember = projectMembers
                .stream()
                .filter(x -> x.getMemberRole() == JobMemberRole.promoter && StringUtils.isEmpty(x.getInviterId()))
                .findFirst()
                .orElse(null);

        if (targetPromoterProjectMember == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到promoter成员信息");
        }

        ProjectMySqlModel targetProjectMySqlModel = projectService.findByProjectId(input.getTargetProjectId());
        if (null == targetProjectMySqlModel) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到目标项目信息");
        }

        ProjectFlowMySqlModel sourceProjectFlow = findOne(input.getSourceFlowId());
        if (sourceProjectFlow == null) {
            // If the source replication flow cannot be found locally, obtain the source flow from the initiator

            sourceProjectFlow = gatewayService.callOtherMemberBoard(
                    targetPromoterProjectMember.getMemberId(),
                    JobMemberRole.provider,
                    DetailFlowApi.class,
                    new DetailFlowApi.Input(input.getSourceFlowId()),
                    ProjectFlowMySqlModel.class
            );
        }
        if (sourceProjectFlow == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到原流程信息：" + input.getSourceFlowId());
        }

        // Get the node information of the original process
        QueryFlowNodeListApi.Output output = gatewayService.callOtherMemberBoard(
                targetPromoterProjectMember.getMemberId(),
                JobMemberRole.provider,
                QueryFlowNodeListApi.class,
                new QueryFlowNodeListApi.Input(input.getSourceFlowId()),
                QueryFlowNodeListApi.Output.class
        );
        List<ProjectFlowNodeOutputModel> sourceProjectFlowNodeList = output.getList();
        if (CollectionUtils.isEmpty(sourceProjectFlowNodeList)) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到原流程节点信息：" + input.getSourceFlowId());
        }

        // Create target flow
        ProjectFlowMySqlModel targetProjectFlow = new ProjectFlowMySqlModel();
        BeanUtils.copyProperties(sourceProjectFlow, targetProjectFlow);
        targetProjectFlow.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        targetProjectFlow.setProjectId(input.getTargetProjectId());
        targetProjectFlow.setFlowName(StringUtils.isEmpty(input.getFlowRename()) ? ("Copy of " + targetProjectFlow.getFlowName()) : input.getFlowRename());
        targetProjectFlow.setFlowId(input.fromGateway() ? input.getNewFlowId() : UUID.randomUUID().toString().replaceAll("-", ""));
        targetProjectFlow.setMyRole(targetProjectMySqlModel.getMyRole());
        targetProjectFlow.setFlowStatus(ProjectFlowStatus.editing);
        targetProjectFlow.setCreatedTime(new Date());
        targetProjectFlow.setCreatedBy(input);
        targetProjectFlow.setCreatorMemberId(sourceProjectFlow.getCreatorMemberId());
        projectFlowRepo.save(targetProjectFlow);

        for (ProjectFlowNodeOutputModel sourceProjectFlowNode : sourceProjectFlowNodeList) {
            ProjectFlowNodeMySqlModel targetProjectFlowNode = new ProjectFlowNodeMySqlModel();
            BeanUtils.copyProperties(sourceProjectFlowNode, targetProjectFlowNode);
            targetProjectFlowNode.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            targetProjectFlowNode.setProjectId(input.getTargetProjectId());
            targetProjectFlowNode.setFlowId(targetProjectFlow.getFlowId());
            targetProjectFlowNode.setParams(JObject.create(sourceProjectFlowNode.getParams()).toJSONString());
            // Copy in different projects, clear node parameters
            if (!input.getTargetProjectId().equals(sourceProjectFlow.getProjectId())) {
                targetProjectFlowNode.setParams(null);
            }
            targetProjectFlowNode.setCreatedBy(input);
            targetProjectFlowNode.setCreatedTime(new Date());
            projectFlowNodeRepository.save(targetProjectFlowNode);
        }

        projectService.updateFlowStatusStatistics(input.getTargetProjectId());

        //Notify other members of the project to synchronize the new flow
        input.setNewFlowId(targetProjectFlow.getFlowId());
        gatewayService.syncToOtherFormalProjectMembers(input.getTargetProjectId(), input, CopyFlowApi.class);
    }


    public void flowFinished(OnJobFinishedApi.Input input) throws StatusCodeWithException {
        List<JobMySqlModel> jobs = jobService.listByJobId(input.getJobId());
        JobMySqlModel job = null;

        if (CollectionUtils.isNotEmpty(jobs)) {
            job = jobs.get(0);
        }
        if (job != null) {
            projectService.updateFlowStatusStatistics(job.getProjectId());
        }
    }

    public void updateFlowStatus(String flowId, ProjectFlowStatus projectFlowStatus) throws StatusCodeWithException {

        ProjectFlowMySqlModel flow = findOne(flowId);
        if (flow == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到需要更新的流程！");
        }

        flow.setFlowStatus(projectFlowStatus);
        flow.setStatusUpdatedTime(new Date());
        flow.setUpdatedBy(CurrentAccount.id());
        projectFlowRepo.save(flow);

        projectService.updateFlowStatusStatistics(flow.getProjectId());
    }

    /**
     * Query the successfully modeled flow information in the project by page
     */
    public PagingOutput<ModelingInfoOutputModel> queryModelingInfo(QueryApi.Input input) {

        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("projectId", input.getProjectId())
                .equal("servingModel", true)
                .equal("jobId", input.getJobId())
                .equal("flowId", input.getFlowId())
                .equal("componentType", input.getComponentType())
                .orderBy("createdTime", OrderBy.desc)
                .build(TaskResultMySqlModel.class);

        PagingOutput<ModelingInfoOutputModel> pagingOutput = taskResultRepository.paging(where, input, ModelingInfoOutputModel.class);

        pagingOutput.getList().forEach(x -> {
            ProjectFlowMySqlModel flow = findOne(x.getFlowId());
            if (flow != null) {
                x.setFlowName(flow.getFlowName());
                x.setComponentName(x.getComponentType().getLabel());
            }
        });

        return pagingOutput;
    }

    /**
     * Query model details: including model evaluation results.
     */
    public TaskResultOutputModel findModelingResult(DetailApi.Input input) throws StatusCodeWithException {

        TaskResultOutputModel result = null;

        TaskMySqlModel modelTask = taskService.findOne(input);

        if (modelTask == null) {
            return null;
        }

        List<TaskMySqlModel> evaluationTasks = taskService.findAll(input.getJobId(), modelTask.getFlowId(), ComponentType.Evaluation);

        evaluationTasks.sort(Comparator.comparingInt(x -> x.getRole().getIndex()));

        if (CollectionUtils.isNotEmpty(evaluationTasks)) {
            TaskMySqlModel evaluationTask = evaluationTasks.get(0);
            if (evaluationTask != null) {
                result = Components
                        .get(ComponentType.Evaluation)
                        .getTaskResult(evaluationTask.getTaskId(), input.getType());
            }
        } else {
            result = Components
                    .get(modelTask.getTaskType())
                    .getTaskResult(modelTask.getTaskId(), input.getType());
        }

        return result;
    }

    /**
     * if DataIO node params is not null,Returns the node with empty params in the flow
     */
    public List<ProjectFlowNodeOutputModel> getParamsIsNullFlowNodes(String flowId) {
        List<ProjectFlowNodeMySqlModel> flowNodes = projectFlowNodeService.findNodesByFlowId(flowId);

        ProjectFlowNodeMySqlModel dataIONode = flowNodes.stream().filter(x -> x.getComponentType() == ComponentType.DataIO).findFirst().orElse(null);

        if (dataIONode != null && StringUtils.isNotEmpty(dataIONode.getParams())) {

            return flowNodes
                    .stream()
                    .filter(x -> x.getParams() == null && Objects.requireNonNull(Components.get(x.getComponentType())).canSelectFeatures())
                    .map(x -> ModelMapper.map(x, ProjectFlowNodeOutputModel.class))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    public List<ProjectFlowProgressOutputModel> getProgress(List<String> flowIdList) {
        Specification<ProjectFlowMySqlModel> where = Where
                .create()
                .in("flowId", flowIdList.stream().map(x -> (Object) x).collect(Collectors.toList()))
                .build(ProjectFlowMySqlModel.class);

        return projectFlowRepo
                .findAll(where)
                .stream()
                .map(x -> {
                    ProjectFlowProgressOutputModel output = ModelMapper.map(x, ProjectFlowProgressOutputModel.class);
                    JobMySqlModel lastJob = jobRepository.findLastByFlowId(x.getFlowId(), x.getMyRole().name());
                    if (lastJob != null) {
                        output.setJobProgress(lastJob.getProgress());
                    }
                    return output;
                })
                .collect(Collectors.toList());

    }

    /**
     * add oot flow
     */
    public AddOotFlowApi.Output addOotFlow(AddOotFlowApi.Input input) throws StatusCodeWithException {
        List<FlowTemplateMySqlModel> models = flowTemplateService.query();
        List<JobMySqlModel> jobMySqlModelList = jobService.listByJobId(input.getOotJobId());
        if (CollectionUtils.isEmpty(jobMySqlModelList)) {
            throw new StatusCodeWithException("找不到原作业信息", StatusCode.DATA_NOT_FOUND);
        }
        JobMySqlModel jobMySqlModel = jobMySqlModelList.stream().filter(x -> JobMemberRole.promoter.equals(x.getMyRole())).findFirst().orElse(null);
        if (null == jobMySqlModel) {
            throw new StatusCodeWithException("只有 promoter 才能创建流程", StatusCode.ILLEGAL_REQUEST);
        }

        if (FederatedLearningType.mix.equals(jobMySqlModel.getFederatedLearningType())) {
            throw new StatusCodeWithException("暂时不支持混合联邦类型", StatusCode.UNSUPPORTED_HANDLE);
        }

        FlowTemplateMySqlModel ootFlowTemplateMySqlModel = models.stream().filter(x -> "oot".equals(x.getEnname())).findFirst().orElse(null);
        if (null == ootFlowTemplateMySqlModel) {
            throw new StatusCodeWithException("找不到打分验证组件模板", StatusCode.DATA_NOT_FOUND);
        }

        AddOotFlowApi.Output output = new AddOotFlowApi.Output();
        // is oot?
        ModelOotRecordMysqlModel modelOotRecordMysqlModel = null;
        modelOotRecordMysqlModel = modelOotRecordService.findByJobIdAndModelFlowNodeId(input.getOotJobId(),
                input.getOotModelFlowNodeId());
        if (null != modelOotRecordMysqlModel) {
            ProjectFlowMySqlModel projectFlowMySqlModel = findOne(modelOotRecordMysqlModel.getFlowId());
            if (null != projectFlowMySqlModel && !projectFlowMySqlModel.getDeleted()) {
                output.setFlowId(modelOotRecordMysqlModel.getFlowId());
                return output;
            }
        }

        AddFlowApi.Input addFlowInput = new AddFlowApi.Input();
        addFlowInput.setProjectId(jobMySqlModel.getProjectId());
        addFlowInput.setTemplateId(ootFlowTemplateMySqlModel.getId());
        addFlowInput.setFederatedLearningType(jobMySqlModel.getFederatedLearningType());
        addFlowInput.setName(jobMySqlModel.getName() + "- [打分验证-" + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()).substring(11, 19) + "]");
        addFlowInput.setOotMode(true);
        output.setFlowId(addFlow(addFlowInput));

        modelOotRecordMysqlModel = (null != modelOotRecordMysqlModel ? modelOotRecordMysqlModel : new ModelOotRecordMysqlModel());
        modelOotRecordMysqlModel.setFlowId(output.getFlowId());
        modelOotRecordMysqlModel.setOotJobId(input.getOotJobId());
        modelOotRecordMysqlModel.setOotModelFlowNodeId(input.getOotModelFlowNodeId());
        modelOotRecordService.save(modelOotRecordMysqlModel);

        return output;
    }


    public List<ProjectFlowNodeOutputModel> getFlowNodes(String flowId) {
        List<ProjectFlowNodeMySqlModel> flowNodes = projectFlowNodeService.findNodesByFlowId(flowId);
        if (CollectionUtils.isEmpty(flowNodes)) {
            return new ArrayList<>();
        }
        return flowNodes
                .stream()
                .map(x -> ModelMapper.map(x, ProjectFlowNodeOutputModel.class))
                .collect(Collectors.toList());
    }
}
