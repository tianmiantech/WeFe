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

package com.welab.wefe.board.service.api.project.member;

import com.welab.wefe.board.service.database.entity.job.ProjectMemberMySqlModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectMemberOutputModel;
import com.welab.wefe.board.service.service.ProjectMemberService;
import com.welab.wefe.board.service.util.ModelMapper;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Api(path = "project/member/list", name = "Get the list of members in the project")
public class ListApi extends AbstractApi<ListApi.Input, ListApi.Output> {

    @Autowired
    private ProjectMemberService projectMemberService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        List<ProjectMemberMySqlModel> list = projectMemberService.findList(input);

        List<ProjectMemberOutputModel> output = list
                .parallelStream()
                .map(x -> ModelMapper.map(x, ProjectMemberOutputModel.class))
                .collect(Collectors.toList());

        return success(new Output(output));
    }

    public static class Input extends AbstractApiInput {

        public Input() {

        }

        public Input(String projectId) {
            this.projectId = projectId;
        }

        @Check(name = "项目Id", require = true)
        private String projectId;

        private String ootJobId;

        //region getter/setter

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getOotJobId() {
            return ootJobId;
        }

        public void setOotJobId(String ootJobId) {
            this.ootJobId = ootJobId;
        }
        //endregion
    }

    public static class Output {
        private List<ProjectMemberOutputModel> list;

        public Output(List<ProjectMemberOutputModel> list) {
            this.list = list;
        }

        public List<ProjectMemberOutputModel> getList() {
            return list;
        }

        public void setList(List<ProjectMemberOutputModel> list) {
            this.list = list;
        }
    }
}
