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

package com.welab.wefe.board.service.dto.entity.job;

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * @author lonnie
 */
public class TaskProgressOuputModel extends AbstractOutputModel {

    @Check(name = "项目id")
    private String projectId;

    @Check(name = "流程号")
    private String flowId;

    @Check(name = "任务id")
    private String jobId;

    /**
     * 角色
     */
    @Enumerated(EnumType.STRING)
    private JobMemberRole role;

    @Check(name = "流程节点id")
    private String flowNodeId;

    @Check(name = "任务id")
    private String taskId;

    /**
     * 子任务类型;枚举（DataIO/Intersection/HeteroLR...）
     */
    @Enumerated(EnumType.STRING)
    private ComponentType taskType;

    @Check(name = "预计总工程量")
    private int expectWorkAmount;

    @Check(name = "实际总工程量")
    private int reallyWorkAmount;

    @Check(name = "进度")
    private int progress;

    @Check(name = "进度百分比")
    private double progressRate;

    @Check(name = "updated_time - created_time，毫秒。")
    private int spend;

    @Check(name = "预计结束时间")
    private Date expectEndTime;


    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
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

    public ComponentType getTaskType() {
        return taskType;
    }

    public void setTaskType(ComponentType taskType) {
        this.taskType = taskType;
    }

    public int getExpectWorkAmount() {
        return expectWorkAmount;
    }

    public void setExpectWorkAmount(int expectWorkAmount) {
        this.expectWorkAmount = expectWorkAmount;
    }

    public int getReallyWorkAmount() {
        return reallyWorkAmount;
    }

    public void setReallyWorkAmount(int reallyWorkAmount) {
        this.reallyWorkAmount = reallyWorkAmount;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public double getProgressRate() {
        return progressRate;
    }

    public void setProgressRate(double progressRate) {
        this.progressRate = progressRate;
    }

    public int getSpend() {
        return spend;
    }

    public void setSpend(int spend) {
        this.spend = spend;
    }

    public Date getExpectEndTime() {
        return expectEndTime;
    }

    public void setExpectEndTime(Date expectEndTime) {
        this.expectEndTime = expectEndTime;
    }
}
