/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.api.project.dataset;

import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "project/data_resource/remove", name = "Delete the data set in the project")
public class RemoveDataSetApi extends AbstractNoneOutputApi<RemoveDataSetApi.Input> {

    @Autowired
    ProjectService service;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        service.removeDataSet(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "项目ID", require = true)
        private String projectId;

        @Check(name = "数据集ID", require = true)
        private String dataSetId;

        @Check(name = "成员角色", desc = "当删除原始数据集时，需要指定角色，删除衍生数据集时，不需要。")
        private JobMemberRole memberRole;


        //region getter/setter

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getDataSetId() {
            return dataSetId;
        }

        public void setDataSetId(String dataSetId) {
            this.dataSetId = dataSetId;
        }

        public JobMemberRole getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(JobMemberRole memberRole) {
            this.memberRole = memberRole;
        }

        //endregion
    }

}
