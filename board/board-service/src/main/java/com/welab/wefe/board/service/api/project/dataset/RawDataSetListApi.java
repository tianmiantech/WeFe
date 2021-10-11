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

package com.welab.wefe.board.service.api.project.dataset;

import com.welab.wefe.board.service.dto.entity.project.ProjectDataSetOutputModel;
import com.welab.wefe.board.service.service.ProjectDataSetService;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zane.luo
 */
@Api(path = "project/raw_data_set/list", name = "Get the list of raw data sets in the project", login = false)
public class RawDataSetListApi extends AbstractApi<RawDataSetListApi.Input, RawDataSetListApi.Output> {

    @Autowired
    private ProjectDataSetService projectDataSetService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        List<ProjectDataSetOutputModel> list = projectDataSetService.listRawDataSet(input.projectId, input.memberId, input.memberRole, input.containsY);
        return success(new Output(list));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "项目Id", require = true)
        private String projectId;

        @Check(name = "成员Id", require = true, desc = "当此参数为空时，返回项目中所有数据集")
        private String memberId;

        @Check(name = "成员角色", require = true)
        private JobMemberRole memberRole;

        @Check(name = "是否包含Y")
        private Boolean containsY;

        //region getter/setter

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public JobMemberRole getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(JobMemberRole memberRole) {
            this.memberRole = memberRole;
        }

        public Boolean getContainsY() {
            return containsY;
        }

        public void setContainsY(Boolean containsY) {
            this.containsY = containsY;
        }

        //endregion
    }

    public static class Output {
        private List<ProjectDataSetOutputModel> list;

        public Output(List<ProjectDataSetOutputModel> list) {
            this.list = list;
        }

        public List<ProjectDataSetOutputModel> getList() {
            return list;
        }

        public void setList(List<ProjectDataSetOutputModel> list) {
            this.list = list;
        }
    }
}
