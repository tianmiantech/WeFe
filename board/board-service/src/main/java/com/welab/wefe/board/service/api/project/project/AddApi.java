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

package com.welab.wefe.board.service.api.project.project;

import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.ProjectRepository;
import com.welab.wefe.board.service.dto.entity.ProjectDataSetInput;
import com.welab.wefe.board.service.dto.entity.ProjectMemberInput;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zane.luo
 */
@Api(path = "project/add", name = "add new project")
public class AddApi extends AbstractApi<AddApi.Input, AddApi.Output> {
    @Autowired
    ProjectService service;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        String projectId = service.addProject(input);
        return success(new Output(projectId));
    }

    public static class Output {
        private String projectId;

        public Output(String projectId) {
            this.projectId = projectId;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "业务层面的项目ID", hiddenForFrontEnd = true)
        private String projectId;
        @Check(name = "所有成员列表", hiddenForFrontEnd = true)
        private List<ProjectMemberInput> members;

        @Check(name = "项目名称", require = true)
        private String name;

        @Check(name = "项目描述", require = true)
        private String desc;

        @Check(name = "promoter 的数据集列表")
        private List<ProjectDataSetInput> promoterDataSetList;

        @Check(name = "合作方列表", require = true)
        private List<ProjectMemberInput> providerList;

        @Check(name = "协作方列表")
        private List<ProjectMemberInput> promoterList;

        @Check(name = "角色")
        private JobMemberRole role;


        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            // Project name cannot be repeated
            if (!super.fromGateway()) {
                List<ProjectMySqlModel> allByName = Launcher.CONTEXT.getBean(ProjectRepository.class).findAllByName(name);
                if (!allByName.isEmpty()) {
                    StatusCode.PARAMETER_VALUE_INVALID.throwException(
                            "这个项目名称已经被用过了哟~ 再想一个吧~"
                    );
                }
            }

            if (CollectionUtils.isEmpty(providerList)) {
                throw new StatusCodeWithException("请选择合作方", StatusCode.PARAMETER_VALUE_INVALID);
            }
            Set<String> promoterIds = new HashSet<>();
            // When only one promoter member is allowed, the member is duplicated,
            // so it is returned directly, and there is no need to verify later
            if (promoterList == null || promoterList.isEmpty() || fromGateway()) {
                return;
            }
            promoterList.forEach(p -> promoterIds.add(p.getMemberId()));
            if (promoterList.size() != promoterIds.size()) {
                throw new StatusCodeWithException("发起方成员不能重复", StatusCode.PARAMETER_VALUE_INVALID);
            }
            if (!fromGateway() && !promoterIds.add(CacheObjects.getMemberId())) {
                throw new StatusCodeWithException("发起方成员不能重复", StatusCode.PARAMETER_VALUE_INVALID);
            }
            boolean mixFlag = promoterIds.size() >= 2;
            for (ProjectMemberInput m : providerList) {
                if (promoterIds.contains(m.getMemberId()) && mixFlag) {
                    throw new StatusCodeWithException("成员【" + CacheObjects.getMemberName(m.getMemberId()) + "】不能重复存在 ",
                            StatusCode.PARAMETER_VALUE_INVALID);
                }
            }
        }


        //region getter/setter

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public List<ProjectMemberInput> getMembers() {
            return members;
        }

        public void setMembers(List<ProjectMemberInput> members) {
            this.members = members;
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

        public List<ProjectDataSetInput> getPromoterDataSetList() {
            return promoterDataSetList;
        }

        public void setPromoterDataSetList(List<ProjectDataSetInput> promoterDataSetList) {
            this.promoterDataSetList = promoterDataSetList;
        }

        public List<ProjectMemberInput> getProviderList() {
            return providerList;
        }

        public void setProviderList(List<ProjectMemberInput> providerList) {
            this.providerList = providerList;
        }

        public List<ProjectMemberInput> getPromoterList() {
            return promoterList;
        }

        public void setPromoterList(List<ProjectMemberInput> promoterList) {
            this.promoterList = promoterList;
        }

        public JobMemberRole getRole() {
            return role;
        }

        public void setRole(JobMemberRole role) {
            this.role = role;
        }

        //endregion

    }


}
