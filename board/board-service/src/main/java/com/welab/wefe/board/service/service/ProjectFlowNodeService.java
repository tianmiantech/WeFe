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
import com.welab.wefe.board.service.api.project.node.CheckExistEvaluationComponentApi;
import com.welab.wefe.board.service.api.project.node.UpdateApi;
import com.welab.wefe.board.service.component.Components;
import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowNodeMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.repository.ProjectFlowNodeRepository;
import com.welab.wefe.board.service.database.repository.ProjectFlowRepository;
import com.welab.wefe.board.service.dto.entity.data_resource.output.TableDataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.job.ProjectFlowNodeOutputModel;
import com.welab.wefe.board.service.dto.kernel.machine_learning.JobDataSet;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.ProjectFlowStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Service
public class ProjectFlowNodeService {

    @Autowired
    private ProjectFlowNodeRepository projectFlowNodeRepository;
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private ProjectFlowService projectFlowService;
    @Autowired
    private TableDataSetService tableDataSetService;

    @Autowired
    private ProjectFlowRepository projectFlowRepo;

    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;

    @Autowired
    private JobService jobService;

    @Autowired
    private TaskService taskService;

    /**
     * Get all nodes in the flow
     */
    public List<ProjectFlowNodeMySqlModel> findNodesByFlowId(String flowId) {
        Specification<ProjectFlowNodeMySqlModel> where = Where
                .create()
                .equal("flowId", flowId)
                .build(ProjectFlowNodeMySqlModel.class);

        return projectFlowNodeRepository.findAll(where);
    }

    public List<JobDataSet> findFlowDataSetInfo(String flowId) throws StatusCodeWithException {
        List<ProjectFlowNodeMySqlModel> mysqlNodes = findNodesByFlowId(flowId);
        List<FlowGraphNode> allNodes = mysqlNodes.stream().map(x -> ModelMapper.map(x, FlowGraphNode.class))
                .collect(Collectors.toList());

        List<ComponentType> dataSetComponentTypeList = Arrays.asList(ComponentType.DataIO,
                ComponentType.HorzXGBoostValidationDataSetLoader, ComponentType.VertXGBoostValidationDataSetLoader,
                ComponentType.HorzLRValidationDataSetLoader, ComponentType.VertLRValidationDataSetLoader);
        List<JobDataSet> dataSetItemList = new ArrayList<>();
        for (FlowGraphNode node : allNodes) {
            if (!dataSetComponentTypeList.contains(node.getComponentType())) {
                continue;
            }
            // Take out the input parameters of DataIO
            DataIOComponent.Params params = (DataIOComponent.Params) node.getParamsModel();
            if (CollectionUtils.isEmpty(params.getDataSetList())) {
                continue;
            }
            JobDataSet dataSet = new JobDataSet();
            dataSet.members = new ArrayList<>();
            for (DataIOComponent.DataSetItem item : params.getDataSetList()) {
                JobDataSet.Member member = new JobDataSet.Member();
                member.memberRole = item.getMemberRole();
                TableDataSetOutputModel dataSetInfo = tableDataSetService.findDataSetFromLocalOrUnion(item.getMemberId(),
                        item.getDataSetId());
                if (dataSetInfo != null) {
                    member.dataSetRows = dataSetInfo.getTotalDataCount();
                    member.dataSetFeatures = dataSetInfo.getFeatureCount();
                }
                dataSet.members.add(member);
            }
            dataSetItemList.add(dataSet);
        }
        return dataSetItemList;
    }

    /**
     * Find all nodes of the selected data set in the process
     */
    public List<ProjectFlowNodeMySqlModel> listAboutLoadDataSetNodes(String flowId) {

        return projectFlowNodeRepository.findAll(
                Where
                        .create()
                        .equal("flowId", flowId)
                        .in("componentType",
                                Arrays.asList(
                                        ComponentType.DataIO,
                                        ComponentType.ImageDataIO,
                                        ComponentType.HorzXGBoostValidationDataSetLoader,
                                        ComponentType.VertXGBoostValidationDataSetLoader,
                                        ComponentType.HorzLRValidationDataSetLoader,
                                        ComponentType.VertLRValidationDataSetLoader,
                                        ComponentType.Oot
                                )
                        )
                        .build(ProjectFlowNodeMySqlModel.class)
        );
    }

    /**
     * Nodes in the update flow
     */
    @Transactional(rollbackFor = Exception.class)
    public List<ProjectFlowNodeOutputModel> updateFlowNode(UpdateApi.Input input) throws StatusCodeWithException {

        // 对于网格搜索任务参数自动回写等程序自动修改参数的场景，不需要将状态修改为 editing
        if (CurrentAccountUtil.get() != null || input.fromGateway()) {
            // Update flow status
            projectFlowService.updateFlowStatus(input.getFlowId(), ProjectFlowStatus.editing);
        }

        ProjectFlowNodeMySqlModel node = findOne(input.getFlowId(), input.getNodeId());

        List<ProjectFlowNodeOutputModel> list = new ArrayList<>();

        // If the node does not exist, it will be created automatically.
        if (node == null) {

            node = new ProjectFlowNodeMySqlModel();

            node.setFlowId(input.getFlowId());
            node.setNodeId(input.getNodeId());
            node.setParams(input.getParams());
            node.setCreatedBy(input);
            node.setComponentType(input.getComponentType());

            ProjectFlowMySqlModel flow = projectFlowService.findOne(input.getFlowId());
            node.setProjectId(flow.getProjectId());
        }
        // If the node already exists, update it.
        else {
            // 如果参数没有变化，不执行更新，避免刷新 params_version 字段导致缓存不可用。
            // 这里为了增强鲁棒性，使用相同的序列化方式消除因字段顺序差异等原因导致的字符串不一致。
            String oldParams = node.getParams() == null ? "" : JSON.parseObject(node.getParams()).toString();
            String newParams = JSON.parseObject(input.getParams()).toString();
            if (newParams.equals(oldParams)) {
                return list;
            }

            node.setParams(input.getParams());
            node.setParamsVersion(System.currentTimeMillis());
            node.setUpdatedBy(input);
        }

        // If the parameters of the DataIO node have changed,
        // the node parameters with selective features in the child nodes need to be blank.
        if (node.getComponentType() == ComponentType.DataIO) {
            List<ProjectFlowNodeMySqlModel> nodes = findNodesByFlowId(node.getFlowId());
            for (ProjectFlowNodeMySqlModel flowNode : nodes) {
                if (Components.get(flowNode.getComponentType()).canSelectFeatures()) {
                    flowNode.setParams(null);
                    flowNode.setParamsVersion(System.currentTimeMillis());
                    projectFlowNodeRepository.save(flowNode);
                    list.add(ModelMapper.map(flowNode, ProjectFlowNodeOutputModel.class));
                }
            }
        }

        // 特征筛选组件重新选择特征后，后续节点已选择的特征需要重新选择。
        if (node.getComponentType() == ComponentType.FeatureSelection) {
            FlowGraph graph = jobService.createFlowGraph(input.getFlowId());
            for (FlowGraphNode step : graph.getAllJobSteps()) {
                // 有特征列表选项，且是当前编辑节点的子节点
                if (Components.get(step.getComponentType()).canSelectFeatures()
                        && graph.isChild(step.getNodeId(), input.getNodeId())) {

                    ProjectFlowNodeMySqlModel stepNode = findOne(input.getFlowId(), step.getNodeId());
                    stepNode.setParams(null);
                    stepNode.setParamsVersion(System.currentTimeMillis());
                    projectFlowNodeRepository.save(stepNode);
                    list.add(ModelMapper.map(stepNode, ProjectFlowNodeOutputModel.class));
                }
            }
        }

        projectFlowNodeRepository.save(node);

        input.stopwatch.tapAndPrint("start syncToOtherFormalProjectMembers");
        gatewayService.syncToOtherFormalProjectMembers(node.getProjectId(), input, UpdateApi.class);
        input.stopwatch.tapAndPrint("end syncToOtherFormalProjectMembers");

        return list;
    }

    public ProjectFlowNodeMySqlModel findOne(String flowId, String nodeId) {
        Specification<ProjectFlowNodeMySqlModel> where = Where
                .create()
                .equal("flowId", flowId)
                .equal("nodeId", nodeId)
                .build(ProjectFlowNodeMySqlModel.class);

        return projectFlowNodeRepository.findOne(where).orElse(null);
    }

    /**
     * Check whether the current node has specific node type
     */
    public boolean checkExistSpecificComponent(CheckExistEvaluationComponentApi.Input input, List<ComponentType> targetComponentList) throws StatusCodeWithException {
        // Whether it is oot mode (click into the canvas in the model list, that is,
        // oot mode, in oot mode, only check whether there is an evaluation node)
        boolean isOotMode = StringUtil.isNotEmpty(input.getJobId());
        if (isOotMode) {
            JobMySqlModel jobMySqlModel = jobService.findByJobId(input.getJobId(), JobMemberRole.promoter);
            if (null == jobMySqlModel) {
                throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "作业信息不存在。");
            }
            // Find out all the task information of the promoter under the job
            List<TaskMySqlModel> totalTaskMySqlModelList = taskService.listByJobId(jobMySqlModel.getJobId(), jobMySqlModel.getMyRole());
            if (CollectionUtils.isEmpty(totalTaskMySqlModelList)) {
                throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "任务信息不存在。");
            }

            // Find out the task ID of the current model
            List<TaskMySqlModel> myRoleTaskMySqlModelList = taskService.findAll(jobMySqlModel.getJobId(), input.getModelNodeId(), jobMySqlModel.getMyRole());
            if (CollectionUtils.isEmpty(myRoleTaskMySqlModelList)) {
                throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "模型任务信息不存在。");
            }

            // Find out all the branch nodes of the model with the same origin and judge whether there is an evaluation node in all nodes
            String modelTaskId = myRoleTaskMySqlModelList.get(0).getTaskId();
            List<TaskMySqlModel> resultList = taskService.baseFindHomologousBranch(totalTaskMySqlModelList, modelTaskId);
            for (TaskMySqlModel taskMySqlModel : resultList) {
                if (targetComponentList.contains(taskMySqlModel.getTaskType())) {
                    return true;
                }
            }
            return false;

        }
        ProjectFlowMySqlModel projectFlowMySqlModel = projectFlowService.findOne(input.getFlowId());
        if (null == projectFlowMySqlModel) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "流程信息不存在。");
        }
        List<ProjectFlowNodeMySqlModel> flowNodeMySqlModelList = projectFlowNodeService.findNodesByFlowId(input.getFlowId());
        if (CollectionUtils.isEmpty(flowNodeMySqlModelList)) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "流程节点信息不存在。");
        }

        FlowGraph flowGraph = new FlowGraph(projectFlowMySqlModel.getFederatedLearningType(), flowNodeMySqlModelList);
        FlowGraphNode node = flowGraph.getNode(input.getNodeId());
        if (null == node) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "节点信息不存在。");
        }

        // Find related parent node types
        for (ComponentType componentType : targetComponentList) {
            if (null != flowGraph.findOneNodeFromParent(node, componentType)) {
                return true;
            }
        }
        return false;
    }
}
