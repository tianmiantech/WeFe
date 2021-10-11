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

package com.welab.wefe.board.service.dto.entity.project;

import com.welab.wefe.board.service.dto.entity.job.ProjectFlowNodeOutputModel;

import java.util.List;

/**
 * @author zane
 */
public class ProjectFlowDetailOutputModel extends ProjectFlowOutputModel {
    private ProjectOutputModel project;

    private List<ProjectFlowNodeOutputModel> paramsIsNullFlowNodes;

    private boolean isCreator;

    /**
     * 被oot的任务ID
     */
    private String ootJobId;
    /**
     * 被oot的模型id
     */
    private String ootModelFlowNodeId;

    public ProjectOutputModel getProject() {
        return project;
    }

    public void setProject(ProjectOutputModel project) {
        this.project = project;
    }

    public List<ProjectFlowNodeOutputModel> getParamsIsNullFlowNodes() {
        return paramsIsNullFlowNodes;
    }

    public void setParamsIsNullFlowNodes(List<ProjectFlowNodeOutputModel> paramsIsNullFlowNodes) {
        this.paramsIsNullFlowNodes = paramsIsNullFlowNodes;
    }

    public boolean getIsCreator() {
        return isCreator;
    }

    public void setIsCreator(boolean isCreator) {
        this.isCreator = isCreator;
    }

    public String getOotJobId() {
        return ootJobId;
    }

    public void setOotJobId(String ootJobId) {
        this.ootJobId = ootJobId;
    }

    public String getOotModelFlowNodeId() {
        return ootModelFlowNodeId;
    }

    public void setOotModelFlowNodeId(String ootModelFlowNodeId) {
        this.ootModelFlowNodeId = ootModelFlowNodeId;
    }
}
