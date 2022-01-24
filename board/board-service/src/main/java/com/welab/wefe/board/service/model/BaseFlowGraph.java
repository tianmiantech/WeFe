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

import com.welab.wefe.board.service.component.Components;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowNodeMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * To avoid overcrowding of the code in FlowGraph,
 * place non-public basic methods in this parent class.
 *
 * @author zane.luo
 */
public abstract class BaseFlowGraph {

    protected JobMySqlModel lastJob;
    /**
     * Job currently to be created
     */
    protected JobMySqlModel job;
    protected List<JobMemberMySqlModel> members;
    protected FederatedLearningType federatedLearningType;
    /**
     * All nodes in the graph
     */
    public List<FlowGraphNode> allNodes = new ArrayList<>();
    /**
     * Starting node in the graph
     */
    protected List<FlowGraphNode> startNodes;
    /**
     * Nodes that will be executed in the current graph
     */
    protected List<FlowGraphNode> jobSteps = new ArrayList<>();

    public BaseFlowGraph(JobMySqlModel job, JobMySqlModel lastJob, List<JobMemberMySqlModel> members, List<ProjectFlowNodeMySqlModel> mysqlNodes) throws StatusCodeWithException {
        this(job.getFederatedLearningType(), lastJob, mysqlNodes);

        this.job = job;
        this.members = members;

    }

    public BaseFlowGraph(FederatedLearningType federatedLearningType, JobMySqlModel lastJob, List<ProjectFlowNodeMySqlModel> mysqlNodes) throws StatusCodeWithException {
        this.federatedLearningType = federatedLearningType;
        this.lastJob = lastJob;

        if (CollectionUtils.isEmpty(mysqlNodes)) {
            return;
        }

        // Type conversion
        this.allNodes = mysqlNodes
                .stream()
                .map(x -> ModelMapper.map(x, FlowGraphNode.class))
                .collect(Collectors.toList());

        // Filter out the starting node
        this.startNodes = allNodes
                .stream()
                .filter(ProjectFlowNodeMySqlModel::isStartNode)
                .collect(Collectors.toList());

        // Initialize the deep of the starting node
        this.startNodes.forEach(x -> x.setDeep(1));

        /**
         * Sort out the parent node relationship of each node
         */
        this.allNodes.forEach(x -> x.setParents(findParents(x)));

        /**
         * Sort out the child node relationship of each node
         */
        this.allNodes.forEach(x -> x.setChildren(findChildren(x)));

        // Check whether it contains an infinite loop
        checkEndlessLoop();

        // Sort out the list of nodes that will be executed
        findJobSteps(jobSteps, startNodes);

        // Sort the task nodes that will be executed
        sortJobSteps();
    }

    /**
     * Sort the task nodes that will be executed
     */
    private void sortJobSteps() {
        // Sort by depth
        jobSteps.sort(Comparator.comparingInt(FlowGraphNode::getDeep));

        // Set the serial number
        for (int i = 0; i < jobSteps.size(); i++) {
            FlowGraphNode node = jobSteps.get(i);
            node.setPosition(i);
        }
    }

    /**
     * Recursively, traverse the child nodes to find the nodes that will be executed in the current graph.
     */
    private void findJobSteps(List<FlowGraphNode> preNodes, List<FlowGraphNode> nodes) throws StatusCodeWithException {

        if (nodes == null) {
            return;
        }

        int beforeTaskCount = preNodes.size();

        boolean stopCreateTask = false;
        for (FlowGraphNode node : nodes) {

            // Avoid going back when exploring upwards and skip the nodes that have already been traversed.
            if (preNodes.stream().anyMatch(x -> x.getNodeId().equals(node.getNodeId()))) {
                continue;
            }

            try {
                AbstractComponent<?> component = Components.get(node.getComponentType());

                stopCreateTask = component.stopCreateTask(preNodes, node);
                if (stopCreateTask) {
                    break;
                }

                jobSteps.add(node);

            } catch (FlowNodeException e) {
                throw e;
            } catch (Exception e) {
                throw new FlowNodeException(node, e.getMessage());
            }

        }

        if (stopCreateTask) {
            return;
        }

        // 只有在当前层级产生了新 task 时才进行探索动作，避免走回头路时死循环。
        // Only when a new task is generated at the current level
        // will the exploration action be carried out to avoid an endless loop when going back.
        if (preNodes.size() > beforeTaskCount) {
            // 向上探索
            // Explore upward
            for (FlowGraphNode node : nodes) {
                findJobSteps(preNodes, node.getParents());
            }

            // 向下探索
            // Explore down
            for (FlowGraphNode node : nodes) {
                findJobSteps(preNodes, node.getChildren());
            }
        }


    }

    /**
     * Get node object according to nodeId
     */
    public FlowGraphNode getNode(String nodeId) {
        return allNodes
                .stream()
                .filter(x -> x.getNodeId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    public List<FlowGraphNode> getAllJobSteps() throws StatusCodeWithException {
        return getJobSteps(null);
    }

    /**
     * 获取任务执行路径节点
     * Get task execution path node
     * <p>
     * 注意：这里没有考虑使用缓存的情况
     * 如果需要查看使用了节点缓存时的执行路径，请先使用 JobService.setGraphHasCacheResult() 设置各节点的缓存情况后再调用此方法。
     * <p>
     * Note: The use of cache is not considered here
     * If you need to view the execution path when the node cache is used,
     * please use JobService.setGraphHasCacheResult() to set the cache condition of each node before calling this method.
     *
     * @param endNodeId Specify the end node, if it is null, it will execute to the last node.
     */
    public List<FlowGraphNode> getJobSteps(String endNodeId) throws StatusCodeWithException {

        if (endNodeId != null) {
            FlowGraphNode endNode = getNode(endNodeId);

            if (endNode == null) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("错误的 end node id：" + endNodeId);
            }

            if (endNode.getPosition() == null) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("无法执行到此处");
            }

            return jobSteps
                    .stream()
                    .filter(x -> x.getPosition() <= endNode.getPosition())
                    .collect(Collectors.toList());

        } else {

            return jobSteps;
        }

    }

    //region private method


    /**
     * 检查是否包含死循环
     * Check whether it contains an infinite loop
     */
    private void checkEndlessLoop() throws StatusCodeWithException {

        for (FlowGraphNode node : startNodes) {

            // 如果起始节点有父节点，肯定死循环。
            // If the starting node has a parent node, there must be an endless loop.
            if (StringUtil.isNotEmpty(node.getParentNodeIdList())) {
                throw new FlowNodeException(node, "起始节点附近包含死循环，请修正。");
            }

            boolean loop = recursiveDescent(node);
            if (loop) {
                throw new FlowNodeException(node, "图中包含死循环，请修正。");
            }
        }

    }

    /**
     * 使用递归下降的方式获取子节点
     * Use recursive descent to get child nodes
     *
     * @return Whether an infinite loop is detected
     */
    private boolean recursiveDescent(FlowGraphNode parentNode) {

        List<FlowGraphNode> children = allNodes
                .stream()
                .filter(x -> {
                    if (StringUtil.isEmpty(x.getParentNodeIdList())) {
                        return false;
                    }

                    return x.getParentNodeIdList().contains(parentNode.getNodeId());
                })
                .collect(Collectors.toList());

        // 如果某个子节点的 deep 小于等于父节点，说明发生了死循环。
        // If the deep of a child node is less than or equal to the parent node, an infinite loop has occurred.
        if (children.stream().anyMatch(x -> x.getDeep() != null && x.getDeep() <= parentNode.getDeep())) {
            return true;
        }

        children.forEach(x -> x.setDeep(parentNode.getDeep() + 1));

        // There are no more child nodes, and the traversal ends.
        if (CollectionUtils.isEmpty(children)) {
            return false;
        }


        // 对子节点进行递归
        // Recurse on child nodes
        for (FlowGraphNode node : children) {
            boolean loop = recursiveDescent(node);
            if (loop) {
                return true;
            }
        }

        return false;

    }

    /**
     * 根据 nodeId 获取节点
     * Get node according to nodeId
     */
    private FlowGraphNode findOneById(String nodeId) {
        return allNodes
                .stream()
                .filter(x -> x.getNodeId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * find parents node
     */
    private List<FlowGraphNode> findParents(FlowGraphNode node) {
        if (StringUtil.isEmpty(node.getParentNodeIdList())) {
            return null;
        }

        List<FlowGraphNode> result = new ArrayList<>();

        for (String id : StringUtil.splitWithoutEmptyItem(node.getParentNodeIdList(), ",")) {
            result.add(findOneById(id));
        }
        return result;
    }

    /**
     * find children node
     */
    private List<FlowGraphNode> findChildren(FlowGraphNode node) {

        return allNodes
                .stream()
                .filter(x -> {
                    if (StringUtil.isEmpty(x.getParentNodeIdList())) {
                        return false;
                    }

                    return x.getParentNodeIdList().contains(node.getNodeId());
                })
                .collect(Collectors.toList());
    }

    //endregion


    //region getter/setter


    public JobMySqlModel getLastJob() {
        return lastJob;
    }

    public List<FlowGraphNode> getStartNodes() {
        return startNodes;
    }

    public JobMySqlModel getJob() {
        return job;
    }

    public List<JobMemberMySqlModel> getMembers() {
        return members;
    }

    public FederatedLearningType getFederatedLearningType() {
        return federatedLearningType;
    }

    //endregion
}
