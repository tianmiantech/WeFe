/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.api.project.project;

import com.welab.wefe.board.service.database.entity.job.*;
import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author lonnie
 */
@Api(
        path = "project/data/info",
        name = "Get the project and the information in the project",
        desc = "Get the project and the data set, project members, project process, project process node information in the project."
)
public class DataInfoApi extends AbstractApi<DataInfoApi.Input, DataInfoApi.Output> {

    @Autowired
    private ProjectService projectService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        return success(projectService.getDataInfo(input));
    }

    public static class Input extends AbstractApiInput {

        public Input() {

        }

        public Input(String project) {
            this.projectId = project;
        }

        @Check(name = "项目id", require = true)
        private String projectId;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }
    }

    public static class Output {

        private ProjectMySqlModel project;

        private List<ProjectMemberMySqlModel> projectMembers;

        private List<ProjectDataSetMySqlModel> projectDataSets;

        private List<ProjectFlowMySqlModel> projectFlows;

        private List<ProjectFlowNodeMySqlModel> projectFlowNodes;

        public ProjectMySqlModel getProject() {
            return project;
        }

        public void setProject(ProjectMySqlModel project) {
            this.project = project;
        }

        public List<ProjectMemberMySqlModel> getProjectMembers() {
            return projectMembers;
        }

        public void setProjectMembers(List<ProjectMemberMySqlModel> projectMembers) {
            this.projectMembers = projectMembers;
        }

        public List<ProjectDataSetMySqlModel> getProjectDataSets() {
            return projectDataSets;
        }

        public void setProjectDataSets(List<ProjectDataSetMySqlModel> projectDataSets) {
            this.projectDataSets = projectDataSets;
        }

        public List<ProjectFlowMySqlModel> getProjectFlows() {
            return projectFlows;
        }

        public void setProjectFlows(List<ProjectFlowMySqlModel> projectFlows) {
            this.projectFlows = projectFlows;
        }

        public List<ProjectFlowNodeMySqlModel> getProjectFlowNodes() {
            return projectFlowNodes;
        }

        public void setProjectFlowNodes(List<ProjectFlowNodeMySqlModel> projectFlowNodes) {
            this.projectFlowNodes = projectFlowNodes;
        }
    }
}
