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

package com.welab.wefe.board.service.dto.entity;

import com.welab.wefe.board.service.dto.entity.project.data_set.ProjectDataSetOutputModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

import java.util.List;

/**
 * @author jacky.jiang
 */
public class BloomFilterDetailOutputModel extends AbstractOutputModel {

    @Check(name = "项目ID")
    private String projectId;

    @Check(name = "我方身份;枚举（promoter/provider）")
    private JobMemberRole myRole;

    @Check(name = "我方成员ID")
    private String memberId;

    private List<ProjectDataSetOutputModel> dataSetList;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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

    public List<ProjectDataSetOutputModel> getDataSetList() {
        return dataSetList;
    }

    public void setDataSetList(List<ProjectDataSetOutputModel> dataSetList) {
        this.dataSetList = dataSetList;
    }
}
