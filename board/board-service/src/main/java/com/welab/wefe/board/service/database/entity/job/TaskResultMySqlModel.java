/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.database.entity.job;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.JobMemberRole;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane.luo
 */
@Entity(name = "task_result")
public class TaskResultMySqlModel extends AbstractBaseMySqlModel {
    /**
     * 任务Id
     */
    private String jobId;
    /**
     * 流程Id
     */
    private String flowId;
    /**
     * 流程节点Id
     */
    private String flowNodeId;
    /**
     * 子任务Id
     */
    private String taskId;
    /**
     * 任务名称，例如：vert_lr_0
     */
    private String name;
    /**
     * 组件id
     */
    @Enumerated(EnumType.STRING)
    private ComponentType componentType;
    /**
     * 成员角色
     */
    @Enumerated(EnumType.STRING)
    private JobMemberRole role;
    /**
     * 类型，一个 task 会有多行不同类型的 result
     */
    private String type;
    /**
     * 执行结果
     */
    private String result;

    /**
     * 是否是可以导出到 serving 的模型
     */
    private boolean servingModel;

    /**
     * 项目id
     */
    private String projectId;

    //region getter/setter

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowNodeId() {
        return flowNodeId;
    }

    public void setFlowNodeId(String flowNodeId) {
        this.flowNodeId = flowNodeId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public JobMemberRole getRole() {
        return role;
    }

    public void setRole(JobMemberRole role) {
        this.role = role;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isServingModel() {
        return servingModel;
    }

    public void setServingModel(boolean servingModel) {
        this.servingModel = servingModel;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    //endregion
}
