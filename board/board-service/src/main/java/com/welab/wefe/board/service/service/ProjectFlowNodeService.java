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

        // Update flow status
        projectFlowService.updateFlowStatus(input.getFlowId(), ProjectFlowStatus.editing);

        ProjectFlowNodeMySqlModel node = findOne(input.getFlowId(), input.getNodeId());

        // 对表单有效性进行检查
        Components
                .get(node.getComponentType())
                .deserializationParam(input.getParams());

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
                    projectFlowNodeRepository.save(flowNode);
                    list.add(ModelMapper.map(flowNode, ProjectFlowNodeOutputModel.class));
                }
            }
        }

        projectFlowNodeRepository.save(node);

        gatewayService.syncToOtherFormalProjectMembers(node.getProjectId(), input, UpdateApi.class);

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
     * Check whether the current node has an evaluation node type
     */
    public boolean checkExistEvaluationComponent(CheckExistEvaluationComponentApi.Input input) throws StatusCodeWithException {
        // Whether it is oot mode (click into the canvas in the model list, that is,
        // oot mode, in oot mode, only check whether there is an evaluation node)
        boolean isOotMode = StringUtil.isNotEmpty(input.getJobId());
        if (isOotMode) {
            JobMySqlModel jobMySqlModel = jobService.findByJobId(input.getJobId(), JobMemberRole.promoter);
            if (null == jobMySqlModel) {
                throw new StatusCodeWithException("作业信息不存在。", StatusCode.DATA_NOT_FOUND);
            }
            // Find out all the task information of the promoter under the job
            List<TaskMySqlModel> totalTaskMySqlModelList = taskService.listByJobId(jobMySqlModel.getJobId(), jobMySqlModel.getMyRole());
            if (CollectionUtils.isEmpty(totalTaskMySqlModelList)) {
                throw new StatusCodeWithException("任务信息不存在。", StatusCode.DATA_NOT_FOUND);
            }

            // Find out the task ID of the current model
            List<TaskMySqlModel> myRoleTaskMySqlModelList = taskService.findAll(jobMySqlModel.getJobId(), input.getModelNodeId(), jobMySqlModel.getMyRole());
            if (CollectionUtils.isEmpty(myRoleTaskMySqlModelList)) {
                throw new StatusCodeWithException("模型任务信息不存在。", StatusCode.DATA_NOT_FOUND);
            }

            // Find out all the branch nodes of the model with the same origin and judge whether there is an evaluation node in all nodes
            String modelTaskId = myRoleTaskMySqlModelList.get(0).getTaskId();
            List<TaskMySqlModel> resultList = taskService.baseFindHomologousBranch(totalTaskMySqlModelList, modelTaskId);
            for (TaskMySqlModel taskMySqlModel : resultList) {
                if (ComponentType.Evaluation == taskMySqlModel.getTaskType()) {
                    return true;
                }
            }
            return false;

        }
        ProjectFlowMySqlModel projectFlowMySqlModel = projectFlowService.findOne(input.getFlowId());
        if (null == projectFlowMySqlModel) {
            throw new StatusCodeWithException("流程信息不存在。", StatusCode.DATA_NOT_FOUND);
        }
        List<ProjectFlowNodeMySqlModel> flowNodeMySqlModelList = projectFlowNodeService.findNodesByFlowId(input.getFlowId());
        if (CollectionUtils.isEmpty(flowNodeMySqlModelList)) {
            throw new StatusCodeWithException("流程节点信息不存在。", StatusCode.DATA_NOT_FOUND);
        }

        FlowGraph flowGraph = new FlowGraph(projectFlowMySqlModel.getFederatedLearningType(), flowNodeMySqlModelList);
        FlowGraphNode node = flowGraph.getNode(input.getNodeId());
        if (null == node) {
            throw new StatusCodeWithException("节点信息不存在。", StatusCode.DATA_NOT_FOUND);
        }

        // Find related parent node types
        FlowGraphNode preModelNode = flowGraph.findOneNodeFromParent(node, ComponentType.Evaluation);
        return null != preModelNode;
    }
}
