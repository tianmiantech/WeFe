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

package com.welab.wefe.board.service.dto.entity.project;

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.common.enums.ProjectFlowStatus;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import java.util.Date;

/**
 * @author zane.luo
 */
public class ProjectFlowProgressOutputModel extends AbstractOutputModel {
    @Check(name = "项目ID")
    private String projectId;
    @Check(name = "流程ID")
    private String flowId;
    @Check(name = "流程的状态")
    private ProjectFlowStatus flowStatus;
    private Date statusUpdatedTime;
    private String message;
    @Check(name = "任务进度")
    private Integer jobProgress;


    //region getter/setter

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

    public ProjectFlowStatus getFlowStatus() {
        return flowStatus;
    }

    public void setFlowStatus(ProjectFlowStatus flowStatus) {
        this.flowStatus = flowStatus;
    }

    public Date getStatusUpdatedTime() {
        return statusUpdatedTime;
    }

    public void setStatusUpdatedTime(Date statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getJobProgress() {
        return jobProgress;
    }

    public void setJobProgress(Integer jobProgress) {
        this.jobProgress = jobProgress;
    }


    //endregion

}
