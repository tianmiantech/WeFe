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

package com.welab.wefe.board.service.model;

import com.welab.wefe.board.service.component.base.filter.OutputItemFilterFunction;
import com.welab.wefe.board.service.component.base.io.NodeOutputItem;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowNodeMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author zane.luo
 */
public class FlowGraph extends BaseFlowGraph {


    public FlowGraph(JobMySqlModel job, JobMySqlModel lastJob, List<JobMemberMySqlModel> members, List<ProjectFlowNodeMySqlModel> mysqlNodes) throws StatusCodeWithException {
        super(job, lastJob, members, mysqlNodes);
    }

    public FlowGraph(FederatedLearningType federatedLearningType, List<ProjectFlowNodeMySqlModel> mysqlNodes) throws StatusCodeWithException {
        super(federatedLearningType, null, mysqlNodes);
    }

    public FlowGraph(FederatedLearningType federatedLearningType, JobMySqlModel lastJob, List<ProjectFlowNodeMySqlModel> mysqlNodes) throws StatusCodeWithException {
        super(federatedLearningType, lastJob, mysqlNodes);
    }

    /**
     * Find a certain type of node from the parent node of the specified node
     */
    public FlowGraphNode findOneNodeFromParent(FlowGraphNode node, ComponentType componentType) {
        return findOneNodeFromParent(node, x -> x.getComponentType() == componentType);
    }

    /**
     * Find the modeling node from the parent node of the specified node
     */
    public FlowGraphNode findModelingNodeFromParent(FlowGraphNode node) {

        return findModelingNodeFromParent(node, super.federatedLearningType);
    }

    /**
     * Manually pass in the federatedLearningType
     * (for some FlowGraph created only with node lists, there is no job information)
     */
    public FlowGraphNode findModelingNodeFromParent(FlowGraphNode node, FederatedLearningType federatedLearningType) {
        FlowGraphNode result;

        switch (federatedLearningType) {
            case horizontal:
                result = findOneNodeFromParent(node, ComponentType.HorzLR);
                if (result == null) {
                    result = findOneNodeFromParent(node, ComponentType.HorzSecureBoost);
                    if (result == null) {
                        result = findOneNodeFromParent(node, ComponentType.HorzNN);
                    }
                }
                return result;

            case vertical:
                result = findOneNodeFromParent(node, ComponentType.VertLR);
                if (result == null) {
                    result = findOneNodeFromParent(node, ComponentType.VertSecureBoost);
                    if (result == null) {
                        result = findOneNodeFromParent(node, ComponentType.VertNN);
                    }
                }
                return result;
            case mix:
                result = findOneNodeFromParent(node, ComponentType.MixLR);
                if (result == null) {
                    result = findOneNodeFromParent(node, ComponentType.MixSecureBoost);
                }
                return result;
            default:
                return null;
        }
    }

    /**
     * Find the verification data set node from the parent node of the specified node
     *
     * @param modelingType Modeling type
     */
    public FlowGraphNode findValidationDataSetFromParent(FlowGraphNode node, ComponentType modelingType) {

        switch (modelingType) {
            case HorzLR:
                return findOneNodeFromParent(node, ComponentType.HorzLRValidationDataSetLoader);
            case VertLR:
                return findOneNodeFromParent(node, ComponentType.VertLRValidationDataSetLoader);
            case HorzSecureBoost:
                return findOneNodeFromParent(node, ComponentType.HorzXGBoostValidationDataSetLoader);
            case VertSecureBoost:
                return findOneNodeFromParent(node, ComponentType.VertXGBoostValidationDataSetLoader);
            default:
                return null;
        }

    }

    /**
     * Find a NodeOutputItem that meets the requirements from the parent node of the specified node
     */
    public NodeOutputItem findNodeOutputFromParent(FlowGraphNode node, OutputItemFilterFunction filter) throws FlowNodeException {

        List<FlowGraphNode> parents = node.getParents();
        if (CollectionUtils.isEmpty(parents)) {
            return null;
        }

        for (FlowGraphNode parent : parents) {

            // 获取此节点的输出项列表
            // Get the list of output items of this node
            List<OutputItem> items = parent
                    .getComponent()
                    .outputs(this, parent);

            // Iterate through the output items and try to find a suitable output item.
            if (items != null) {
                for (OutputItem item : items) {
                    if (filter.apply(parent, item)) {
                        return new NodeOutputItem(parent, item);
                    }
                }
            }

        }

        for (FlowGraphNode parent : parents) {
            NodeOutputItem result = findNodeOutputFromParent(parent, filter);
            if (result != null) {
                return result;
            }
        }

        return null;

    }

    /**
     * Find the nearest node that meets the requirements from the parent node of the specified node
     */
    public FlowGraphNode findOneNodeFromParent(FlowGraphNode node, Function<FlowGraphNode, Boolean> where) {
        List<FlowGraphNode> parents = node.getParents();
        if (CollectionUtils.isEmpty(parents)) {
            return null;
        }

        for (FlowGraphNode parent : parents) {
            if (where.apply(parent)) {
                return parent;
            }
        }

        for (FlowGraphNode parent : parents) {
            FlowGraphNode result = findOneNodeFromParent(parent, where);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * 从指定节点的父节点中查找所有满足要求的节点
     * <p>
     * Find all nodes that meet the requirements from the parent node of the specified node
     */
    public List<FlowGraphNode> findAllNodesFromParent(FlowGraphNode node, Function<FlowGraphNode, Boolean> where) {
        List<FlowGraphNode> result = new ArrayList<>();

        findAllNodesFromParent(result, node, where);

        return result;
    }

    /**
     * Find all nodes that meet the requirements from the parent node of the specified node
     */
    private void findAllNodesFromParent(List<FlowGraphNode> result, FlowGraphNode node, Function<FlowGraphNode, Boolean> where) {
        List<FlowGraphNode> parents = node.getParents();
        if (CollectionUtils.isEmpty(parents)) {
            return;
        }

        for (FlowGraphNode parent : parents) {
            if (where.apply(parent)) {
                result.add(parent);
            }
        }

        for (FlowGraphNode parent : parents) {
            findAllNodesFromParent(result, parent, where);
        }
    }

    /**
     * 从任务成员列表中取出自己
     * <p>
     * Remove yourself from the task member list
     */
    public JobMemberMySqlModel getJobMemberIsMe() {
        return members
                .stream()
                .filter(x -> CacheObjects.getMemberId().equals(x.getMemberId()) && x.getJobRole() == job.getMyRole())
                .findFirst()
                .orElse(null);
    }

}
