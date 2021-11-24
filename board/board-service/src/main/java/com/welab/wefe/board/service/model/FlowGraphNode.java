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

package com.welab.wefe.board.service.model;

import com.welab.wefe.board.service.component.Components;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowNodeMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
public class FlowGraphNode<T extends AbstractCheckModel> extends ProjectFlowNodeMySqlModel {

    /**
     * Node form parameters
     */
    private T paramsModel;
    /**
     * Depth
     * <p>
     * When it is empty,
     * it means that it is not in the main process, and such a node will not create a task.
     */
    private Integer deep;
    /**
     * The sequence number in the task list, if null, means that the node will not be executed.
     */
    private Integer position;
    /**
     * Whether there are historical cached results available
     */
    private Boolean hasCacheResult;

    private String taskName;
    /**
     * parent nodes
     */
    private List<FlowGraphNode> parents = new ArrayList<>();
    /**
     * children nodes
     */
    private List<FlowGraphNode> children = new ArrayList<>();


    public String getTaskName() {
        if (StringUtils.isBlank(this.taskName)) {
            this.taskName = createTaskName(super.getComponentType(), super.getNodeId());
        }
        return this.taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Generate task name (unique in job scope)
     */
    public static String createTaskName(ComponentType componentType, String nodeId) {
        return componentType.name() + "_" + nodeId;
    }


    public String createTaskId(JobMySqlModel job) {
        return createTaskId(job, super.getComponentType(), super.getNodeId());
    }

    public String createTaskId(JobMySqlModel job, int index) {
        return createTaskId(job, super.getComponentType(), super.getNodeId(), index);
    }

    /**
     * create task id
     */
    public static String createTaskId(JobMySqlModel job, ComponentType componentType, String nodeId) {
        return job.getJobId() + "_" + job.getMyRole().name() + "_" + createTaskName(componentType, nodeId);
    }

    /**
     * create task id
     */
    public static String createTaskId(JobMySqlModel job, ComponentType componentType, String nodeId, int index) {
        return job.getJobId() + "_" + job.getMyRole().name() + "_" + createTaskName(componentType, nodeId) + "_"
                + index;
    }

    /**
     * Join the ParentTaskIds field of the task
     */
    public String createParentTaskIds(JobMySqlModel job) {
        // 只有一个横向建模的时候，arbiter任务没有父节点。特殊情况：串行两个横向建模，目前先不处理
        // When there is only one horizontal modeling, the arbiter task has no parent node.
        // Special case: two horizontal modeling in series, do not deal with it now.
        if (job.getMyRole() == JobMemberRole.arbiter) {
            return "";
        }

        return getParents()
                .stream()
                .map(x -> x.createTaskId(job))
                .collect(Collectors.joining(","));
    }

    /**
     * Join the ParentTaskIds field of the task
     */
    public String createParentTaskIds(JobMySqlModel job, int index) {
        // 只有一个横向建模的时候，arbiter任务没有父节点。特殊情况：串行两个横向建模，目前先不处理
        if (job.getMyRole() == JobMemberRole.arbiter) {
            return "";
        }

        return getParents().stream().map(x -> x.createTaskId(job, index)).collect(Collectors.joining(","));
    }

    /**
     * Get the component object corresponding to the node
     */
    public AbstractComponent<?> getComponent() {
        return Components.get(super.getComponentType());
    }

    /**
     * Get form parameter object
     */
    public T getParamsModel() {
        if (paramsModel != null) {
            return paramsModel;
        }

        try {
            paramsModel = (T) Components
                    .get(super.getComponentType())
                    .deserializationParam(this, super.getParams());
        } catch (FlowNodeException e) {
            return null;
        }
        return paramsModel;
    }

    //region getter/setter


    public Integer getDeep() {
        return deep;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getHasCacheResult() {
        return hasCacheResult;
    }

    public void setHasCacheResult(Boolean hasCacheResult) {
        this.hasCacheResult = hasCacheResult;
    }

    public List<FlowGraphNode> getParents() {
        if (deep != null && parents != null) {
            parents.forEach(x -> x.setDeep(deep - 1));
        }

        if (parents == null) {
            parents = new ArrayList<>();
        }

        return parents;
    }

    public void setParents(List<FlowGraphNode> parents) {
        this.parents = parents;
    }

    public List<FlowGraphNode> getChildren() {
        if (deep != null && children != null) {
            children.forEach(x -> x.setDeep(deep + 1));
        }

        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public void setChildren(List<FlowGraphNode> children) {
        this.children = children;
    }


    //endregion
}
