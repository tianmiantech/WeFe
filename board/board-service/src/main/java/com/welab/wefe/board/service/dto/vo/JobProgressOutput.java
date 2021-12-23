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

package com.welab.wefe.board.service.dto.vo;

import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.dto.entity.job.JobMemberOutputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.JobStatus;
import com.welab.wefe.common.wefe.enums.TaskStatus;


/**
 * 任务执行进度
 *
 * @author zane.luo
 */
public class JobProgressOutput {
    private String memberId;
    private JobStatus jobStatus;
    private JobMemberRole jobRole;
    private int progress;
    private String nodeId;
    private String taskId;
    private TaskStatus taskStatus;
    private boolean getProgressSuccess;
    private String message;

    public static JobProgressOutput fail(JobMemberOutputModel member, Exception e) {
        JobProgressOutput output = new JobProgressOutput();
        output.memberId = member.getMemberId();
        output.jobRole = member.getJobRole();
        output.getProgressSuccess = false;
        output.message = e.getMessage();

        return output;
    }

    public static JobProgressOutput success(String memberId, JobMySqlModel job, TaskMySqlModel task) {
        JobProgressOutput output = new JobProgressOutput();
        output.memberId = memberId;
        output.progress = job.getProgress();
        output.jobStatus = job.getStatus();
        output.jobRole = job.getMyRole();
        output.getProgressSuccess = true;
        output.message = job.getMessage();

        if (task != null) {
            output.nodeId = task.getFlowNodeId();
            output.taskId = task.getTaskId();
            output.taskStatus = task.getStatus();
        }

        return output;
    }

    public String getMemberName() {
        return CacheObjects.getMemberName(memberId);
    }

    //region getter/setter

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public JobMemberRole getJobRole() {
        return jobRole;
    }

    public void setJobRole(JobMemberRole jobRole) {
        this.jobRole = jobRole;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public boolean isGetProgressSuccess() {
        return getProgressSuccess;
    }

    public void setGetProgressSuccess(boolean getProgressSuccess) {
        this.getProgressSuccess = getProgressSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    //endregion
}
