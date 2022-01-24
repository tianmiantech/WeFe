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

package com.welab.wefe.board.service.api.project.fusion.member;


import com.welab.wefe.board.service.database.entity.job.ProjectMemberMySqlModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectMemberOutputModel;
import com.welab.wefe.board.service.service.ProjectMemberService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/query/providers",
        name = "query provider list",
        desc = "query provider list"
)
public class QueryProvidersApi extends AbstractApi<QueryProvidersApi.Input, List<ProjectMemberOutputModel>> {
    @Autowired
    ProjectMemberService projectMemberService;

    @Override
    protected ApiResult<List<ProjectMemberOutputModel>> handle(Input input) throws StatusCodeWithException, IOException {
        List<ProjectMemberMySqlModel> memberMySqlModelList =
                projectMemberService.listFormalProjectProviders(input.getProjectId());
        return success(ModelMapper.maps(memberMySqlModelList, ProjectMemberOutputModel.class));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "project_id", require = true)
        String projectId;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }
    }
}
