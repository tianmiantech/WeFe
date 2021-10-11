/**
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

package com.welab.wefe.board.service.dto.entity.job.gateway;

import com.welab.wefe.common.enums.JobMemberRole;

import java.util.List;

/**
 * @author zane.luo
 */
public class ProjectForGatewayOutputModel {

    /**
     * 项目ID，非主键
     */
    private String projectId;
    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String projectDesc;

    /**
     * 合作方
     */
    private List<ProjectForGatewayMemberOutputModel> memberList;

    /**
     * 合作方数据集
     */
    private List<ProjectForGatewayDataSetOutputModel> dataSetList;

    /**
     * 角色
     */
    private JobMemberRole myRole;

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

    public List<ProjectForGatewayMemberOutputModel> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<ProjectForGatewayMemberOutputModel> memberList) {
        this.memberList = memberList;
    }

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
    }

    public List<ProjectForGatewayDataSetOutputModel> getDataSetList() {
        return dataSetList;
    }

    public void setDataSetList(List<ProjectForGatewayDataSetOutputModel> dataSetList) {
        this.dataSetList = dataSetList;
    }

}
