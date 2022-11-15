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
package com.welab.wefe.board.service.component;

import cn.hutool.core.collection.CollectionUtil;
import com.welab.wefe.board.service.dto.vo.FlowDataSetOutputModel;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.exception.StatusCodeWithException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 此类用于跟踪各组件的特征列表变化情况，并对检查所有节点输入的特征列表是否符合要求。
 * <p>
 * https://www.tapd.cn/53885119/prong/stories/view/1153885119001089334
 *
 * @author zane.luo
 * @date 2022/11/11
 */
public class TableDataSetFeatureTracer {

    private FlowGraph graph;
    private String endNodeId;
    /**
     * 跟踪结果
     */
    private List<FlowDataSetOutputModel> result = new ArrayList<>();
    // graph中的节点路径列表，每当遇到分叉，产生一个新的路径。
    private List<List<String>> pathList = new ArrayList<>();

    /**
     * @param endNodeId 指定的任务结束节点id，可能为空，为空时任务会执行完整流程。
     */
    public TableDataSetFeatureTracer(FlowGraph graph, String endNodeId) throws StatusCodeWithException {
        this.graph = graph;
        this.endNodeId = endNodeId;

        List<FlowGraphNode> steps = graph.getJobSteps(endNodeId);

        // 从叶子节点向上搜索，遍历出所有执行路径。
        List<FlowGraphNode> leafNodes = steps.stream()
                .filter(FlowGraphNode::isLeafNode)
                .collect(Collectors.toList());

        for (FlowGraphNode leafNode : leafNodes) {
            ArrayList<String> path = new ArrayList<>();
            pathList.add(path);

            rollUp(path, leafNode);
        }
    }

    private void rollUp(List<String> path, FlowGraphNode node) {
        path.add(node.getNodeId());

        List<FlowGraphNode> parents = node.getParents();
        if (CollectionUtil.isEmpty(parents)) {
            return;
        }

        // 如果只有一个父节点，说明没有分叉，不用新开路径。
        rollUp(path, parents.get(0));

        // 对于分叉，新开 path。
        for (int i = 1; i < parents.size(); i++) {
            ArrayList<String> newPath = new ArrayList<>(path);
            pathList.add(newPath);
            rollUp(newPath, parents.get(i));
        }
    }

    public void check() throws StatusCodeWithException {
        for (List<String> path : pathList) {

        }
        for (FlowGraphNode step : steps) {
            step.check
        }
    }

    public void putTableDataSet(DataIOComponent.DataSetItem dataSetItem) {

    }

    private FlowDataSetOutputModel getDateSet()
}
