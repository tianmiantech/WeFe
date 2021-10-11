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

package com.welab.wefe.board.service.api.gateway;

import com.welab.wefe.board.service.dto.entity.project.DerivedProjectDataSetOutputModel;
import com.welab.wefe.board.service.service.ProjectDataSetService;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "gateway/derived_data_set/detail", name = "get a list of derived data sets in the project", rsaVerify = true, login = false)
public class GetDerivedDataSetDetailApi extends AbstractApi<GetDerivedDataSetDetailApi.Input, DerivedProjectDataSetOutputModel> {

    @Autowired
    private ProjectDataSetService projectDataSetService;

    @Override
    protected ApiResult<DerivedProjectDataSetOutputModel> handle(Input input) throws StatusCodeWithException {
        DerivedProjectDataSetOutputModel output = projectDataSetService.getDerivedDataSetDetail(input);
        return success(output);
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "项目Id", require = true)
        private String projectId;
        @Check(name = "数据集Id", require = true)
        private String dataSetId;
        @Check(name = "成员角色Id", require = true)
        private JobMemberRole memberRole;

        public Input() {
        }

        public Input(String projectId, String dataSetId, JobMemberRole memberRole) {
            this.projectId = projectId;
            this.dataSetId = dataSetId;
            this.memberRole = memberRole;
        }

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
