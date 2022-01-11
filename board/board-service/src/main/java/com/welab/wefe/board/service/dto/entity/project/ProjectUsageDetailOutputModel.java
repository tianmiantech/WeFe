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

package com.welab.wefe.board.service.dto.entity.project;


import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.ProjectType;

import java.util.Date;


/**
 * @author Jacky.jiang
 */
public class ProjectUsageDetailOutputModel extends AbstractApiOutput {

    @Check(name = "项目ID")
    private String projectId;
    @Check(name = "名称")
    private String name;
    @Check(name = "项目描述")
    private String projectDesc;
    @Check(name = "我方角色")
    private JobMemberRole myRole;
    @Check(name = "该项目的创建者ID")
    private String memberId;
    @Check(name = "开始时间")
    private Date startTime;
    @Check(name = "结束时间")
    private Date finishTime;
    @Check(name = "项目类型")
    private ProjectType projectType;

    // region getter/setter

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
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

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }


    // endregion
}
