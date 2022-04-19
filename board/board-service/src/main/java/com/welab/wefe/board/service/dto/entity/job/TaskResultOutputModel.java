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

package com.welab.wefe.board.service.dto.entity.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.TaskStatus;

import java.util.Date;
import java.util.List;

/**
 * @author zane.luo
 */
public class TaskResultOutputModel extends AbstractOutputModel {
    @Check(name = "任务Id")
    private String jobId;
    @Check(name = "流程Id")
    private String flowId;
    @Check(name = "流程节点Id")
    private String flowNodeId;
    @Check(name = "子任务Id")
    private String taskId;
    @Check(name = "task参数")
    private JSONObject taskConfig;
    @Check(name = "任务名称，例如：vert_lr_0")
    private String name;
    @Check(name = "组件id")
    private ComponentType componentType;
    @Check(name = "成员角色")
    private JobMemberRole role;
    @Check(name = "类型，一个 task 会有多行不同类型的 result")
    private String type;
    @Check(name = "执行结果")
    private JSONObject result;
    @Check(name = "是否是可以导出到 serving 的模型")
    private boolean servingModel;

    @Check(name = "task的状态")
    private TaskStatus status;
    @Check(name = "开始时间")
    private Date startTime;
    @Check(name = "结束时间")
    private Date finishTime;
    @Check(name = "消息备注;失败原因/备注")
    private String message;
    @Check(name = "发生错误的详细原因，通常是堆栈信息。")
    private String errorCause;
    @Check(name = "task执行顺序")
    private Integer position;
    private Integer spend;
    /**
     * 参与方
     */
    private List<JObject> members;

    public JSONObject getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = JSON.parseObject(result);
    }

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

    public boolean isServingModel() {
        return servingModel;
    }

    public void setServingModel(boolean servingModel) {
        this.servingModel = servingModel;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCause() {
        return errorCause;
    }

    public void setErrorCause(String errorCause) {
        this.errorCause = errorCause;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getSpend() {
        return spend;
    }

    public void setSpend(Integer spend) {
        this.spend = spend;
    }

    public List<JObject> getMembers() {
        return members;
    }

    public void setMembers(List<JObject> members) {
        this.members = members;
    }

    public JSONObject getTaskConfig() {
        return taskConfig;
    }

    public void setTaskConfig(JSONObject taskConfig) {
        this.taskConfig = taskConfig;
    }

    //endregion
}
