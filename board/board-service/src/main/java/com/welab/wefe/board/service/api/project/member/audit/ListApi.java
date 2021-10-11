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

package com.welab.wefe.board.service.api.project.member.audit;

import com.welab.wefe.board.service.database.entity.job.ProjectMemberAuditMySqlModel;
import com.welab.wefe.board.service.dto.entity.ProjectMemberAuditOutput;
import com.welab.wefe.board.service.service.ProjectMemberAuditService;
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
@Api(path = "project/member/add/audit/list", name = "Get the review status of new members in the project")
public class ListApi extends AbstractApi<ListApi.Input, ListApi.Output> {

    @Autowired
    private ProjectMemberAuditService projectMemberAuditService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        List<ProjectMemberAuditMySqlModel> list = projectMemberAuditService.listAll(input.projectId, input.memberId);

        List<ProjectMemberAuditOutput> output = list
                .parallelStream()
                .map(x -> ModelMapper.map(x, ProjectMemberAuditOutput.class))
                .collect(Collectors.toList());

        return success(new Output(output));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "项目Id", require = true)
        private String projectId;

        @Check(name = "成员Id", desc = "当成员 Id 为空时查所有成员")
        private String memberId;


        //region getter/setter

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }


        //endregion
    }

    public static class Output {
        private List<ProjectMemberAuditOutput> list;

        public Output(List<ProjectMemberAuditOutput> list) {
            this.list = list;
        }

        public List<ProjectMemberAuditOutput> getList() {
            return list;
        }

        public void setList(List<ProjectMemberAuditOutput> list) {
            this.list = list;
        }
    }
}
