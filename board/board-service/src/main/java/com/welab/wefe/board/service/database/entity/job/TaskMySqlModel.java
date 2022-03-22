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

package com.welab.wefe.board.service.database.entity.job;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.TaskStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * @author Zane
 */
@Entity(name = "task")
public class TaskMySqlModel extends AbstractBaseMySqlModel {
    /**
     * 名称，在 job 中唯一。
     */
    private String name;
    /**
     * 深度，同一深度的 task 允许并行执行。
     */
    private Integer deep;
    /**
     * 任务Id
     */
    private String jobId;
    /**
     * 角色
     */
    @Enumerated(EnumType.STRING)
    private JobMemberRole role;
    /**
     * 业务ID，多方唯一
     */
    private String taskId;
    /**
     * 流程号
     */
    private String flowId;
    /**
     * 任务在流程中的节点Id
     */
    private String flowNodeId;
    /**
     * 子任务的父节点
     */
    private String parentTaskIdList;
    /**
     * 子任务依赖
     */
    private String dependenceList;
    /**
     * 子任务类型;枚举（DataIO/Intersection/HeteroLR...）
     */
    @Enumerated(EnumType.STRING)
    private ComponentType taskType;
    /**
     * 任务conf_json
     */
    private String taskConf;
    /**
     * 状态;枚举（running/canceled/success/error）
     */
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date finishTime;
    /**
     * 消息备注;失败原因/备注
     */
    private String message;
    /**
     * 发生错误的详细原因，通常是堆栈信息。
     */
    private String errorCause;
    /**
     * task执行顺序
     */
    private Integer position;
    private Integer spend;

    /**
     * 项目id
     */
    private String projectId;

    //region getter/setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDeep() {
        return deep;
    }

    public void setDeep(Integer deep) {
        this.deep = deep;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public Integer getSpend() {
        return spend;
    }

    public void setSpend(Integer spend) {
        this.spend = spend;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobMemberRole getRole() {
        return role;
    }

    public void setRole(JobMemberRole role) {
        this.role = role;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getFlowNodeId() {
        return flowNodeId;
    }

    public void setFlowNodeId(String flowNodeId) {
        this.flowNodeId = flowNodeId;
    }

    public String getParentTaskIdList() {
        return parentTaskIdList;
    }

    public void setParentTaskIdList(String parentTaskId) {
        this.parentTaskIdList = parentTaskId;
    }

    public String getDependenceList() {
        return dependenceList;
    }

    public void setDependenceList(String dependenceList) {
        this.dependenceList = dependenceList;
    }

    public ComponentType getTaskType() {
        return taskType;
    }

    public void setTaskType(ComponentType taskType) {
        this.taskType = taskType;
    }

    public String getTaskConf() {
        return taskConf;
    }

    public void setTaskConf(String taskConf) {
        this.taskConf = taskConf;
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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    //endregion

}
