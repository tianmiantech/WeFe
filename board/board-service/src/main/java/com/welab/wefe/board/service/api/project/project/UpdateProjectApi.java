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

import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.ProjectRepository;
import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zane.luo
 */
@Api(path = "project/update", name = "update project info")
public class UpdateProjectApi extends AbstractNoneOutputApi<UpdateProjectApi.Input> {

    @Autowired
    ProjectService service;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        service.updateProject(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "项目id", require = true)
        private String projectId;

        @Check(name = "项目名称", require = true, messageOnEmpty = "请输入项目名称")
        private String name;

        @Check(name = "项目描述")
        private String desc;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            // Project name cannot be repeated
            if (!super.fromGateway()) {
                List<ProjectMySqlModel> allByName = Launcher.CONTEXT.getBean(ProjectRepository.class).findAllByName(name);
                if (!allByName.isEmpty()) {
                    if (allByName.size() > 1 || !allByName.get(0).getProjectId().equals(projectId)) {
                        StatusCode.PARAMETER_VALUE_INVALID.throwException(
                                "这个项目名称已经被用过了哟~ 再想一个吧~"
                        );
                    }
                }
            }
        }

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

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

    }

}
