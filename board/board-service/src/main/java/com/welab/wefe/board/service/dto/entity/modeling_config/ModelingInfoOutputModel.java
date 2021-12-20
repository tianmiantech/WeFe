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

package com.welab.wefe.board.service.dto.entity.modeling_config;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

/**
 * @author lonnie
 */
public class ModelingInfoOutputModel extends AbstractOutputModel {

    @Check(name = "任务Id")
    private String jobId;
    @Check(name = "流程Id")
    private String flowId;
    @Check(name = "流程节点Id")
    private String flowNodeId;
    @Check(name = "子任务Id")
    private String taskId;
    @Check(name = "流程名称")
    private String flowName;
    @Check(name = "任务名称，例如：vert_lr_0")
    private String name;
    @Check(name = "组件类型")
    private ComponentType componentType;
    @Check(name = "组件类型中文名")
    private String componentName;
    @Check(name = "成员角色")
    private JobMemberRole role;
    @Check(name = "类型，一个 task 会有多行不同类型的 result")
    private String type;
    @Check(name = "执行结果")
    private JSONObject result;
    @Check(name = "是否是可以导出到 serving 的模型")
    private boolean servingModel;

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

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
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

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
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

    public JSONObject getResult() {
        return result;
    }

    public void setResult(JSONObject result) {
        this.result = result;
    }

    public boolean isServingModel() {
        return servingModel;
    }

    public void setServingModel(boolean servingModel) {
        this.servingModel = servingModel;
    }
}
