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

package com.welab.wefe.board.service.api.project.node;

import com.welab.wefe.board.service.component.Components;
import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.component.EvaluationComponent;
import com.welab.wefe.board.service.component.enums.EvaluationType;
import com.welab.wefe.board.service.dto.entity.job.ProjectFlowNodeOutputModel;
import com.welab.wefe.board.service.dto.vo.data_set.table_data_set.LabelDistribution;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.JobService;
import com.welab.wefe.board.service.service.ProjectFlowNodeService;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.ComponentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Api(path = "project/flow/node/update", name = "update node info")
public class UpdateApi extends AbstractApi<UpdateApi.Input, UpdateApi.Output> {

    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;
    @Autowired
    private JobService jobService;
    @Autowired
    private TableDataSetService tableDataSetService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        Output output = new Output(projectFlowNodeService.updateFlowNode(input));

        check(input);

        return success(output);
    }

    /**
     * 编辑流程时，检查分类数与评估组件选择的模式是否匹配，不匹配时提醒。
     *
     * 检查事件触发时机：
     * - 选择数据集组件保存后
     * - 模型评估组件保存后
     *
     * 检查逻辑：
     * 1. 构建 graph，遍历所有评估组件，找到评估组件对应的数据集。
     * 2. 当评估组件的模式为二分类，且数据集的y值分类数大于2时，予以提醒。
     * 3. 当评估组件的模式为非二分类，且数据集的y值分类数小于等于2时，予以提醒。
     *
     * 消息展示节点的选择：
     * - 如果是在模型评估组件点保存，消息展示在选择数据集节点。
     * - 如果是在选择数据集组件点保存，消息展示在模型评估节点。
     *
     * <a href="https://www.tapd.cn/53885119/prong/stories/view/1153885119001086386">查看原始需求</a>
     */
    private void check(Input input) throws StatusCodeWithException {
        // 编辑时，只在发起方做检查（启动时两边都做）。
        if (input.fromGateway()) {
            return;
        }

        List<ComponentType> list = Arrays.asList(ComponentType.DataIO, ComponentType.Evaluation);
        if (!list.contains(input.componentType)) {
            return;
        }

        FlowGraph graph = jobService.createFlowGraph(input.flowId);
        List<FlowGraphNode> evaluationNodes = graph.getAllJobSteps().stream()
                .filter(x -> x.getComponentType() == ComponentType.Evaluation)
                .collect(Collectors.toList());

        for (FlowGraphNode evaluationNode : evaluationNodes) {
            checkByEvaluationNode(graph, evaluationNode);
        }
    }

    public void checkByEvaluationNode(FlowGraph graph, FlowGraphNode evaluationNode) throws FlowNodeException {
        // 获取 DataIO 所选数据集的分类数
        FlowGraphNode dataIoNode = graph.findOneNodeFromParent(evaluationNode, ComponentType.DataIO);
        DataIOComponent.Params dataIoParams = dataIoNode.getParamsModel();
        if (dataIoParams == null) {
            return;
        }
        int labelSpeciesCount = tableDataSetService
                .findOneById(dataIoParams.getMyDataSetConfig().dataSetId)
                .getLabelDistribution()
                .toJavaObject(LabelDistribution.class)
                .labelSpeciesCount;

        // 获取评估组件的评估模式
        EvaluationComponent.Params evaluationParams = evaluationNode.getParamsModel();
        if (evaluationParams == null) {
            return;
        }
        EvaluationType evalType = evaluationParams.getEvalType();

        // 判断，并输出提示。
        if (evalType == EvaluationType.binary) {
            if (labelSpeciesCount > 2) {
                throw new FlowNodeException(evaluationNode, "【评估类别与数据集不匹配】当前评估模式为二分类，但选择的数据集 label 分类数为：" + labelSpeciesCount);
            }
        } else {
            if (labelSpeciesCount <= 2) {
                throw new FlowNodeException(evaluationNode, "【评估类别与数据集不匹配】当前评估模式为 " + evalType.name() + "，但选择的数据集 label 分类数为：" + labelSpeciesCount);
            }
        }
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "流程id", require = true)
        private String flowId;

        @Check(name = "节点ID", require = true)
        private String nodeId;

        @Check(name = "组件类型", require = true)
        private ComponentType componentType;

        @Check(name = "组件参数", require = true, blockXss = false)
        private String params;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            // 对表单有效性进行检查
            Components
                    .get(componentType)
                    .deserializationParam(params);
        }

        //region getter/setter


        public ComponentType getComponentType() {
            return componentType;
        }

        public void setComponentType(ComponentType componentType) {
            this.componentType = componentType;
        }

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getParams() {
            return params;
        }

        public void setParams(String params) {
            this.params = params;
        }


        //endregion
    }

    public static class Output {

        public Output(List<ProjectFlowNodeOutputModel> paramsIsNullFlowNodes) {
            this.paramsIsNullFlowNodes = paramsIsNullFlowNodes;
        }

        private List<ProjectFlowNodeOutputModel> paramsIsNullFlowNodes;

        public List<ProjectFlowNodeOutputModel> getParamsIsNullFlowNodes() {
            return paramsIsNullFlowNodes;
        }

        public void setParamsIsNullFlowNodes(List<ProjectFlowNodeOutputModel> paramsIsNullFlowNodes) {
            this.paramsIsNullFlowNodes = paramsIsNullFlowNodes;
        }
    }


}
