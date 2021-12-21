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

package com.welab.wefe.board.service.api.project.dataset;

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.project.data_set.DerivedProjectDataSetOutputModel;
import com.welab.wefe.board.service.service.ProjectDataSetService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "project/derived_data_set/query", name = "get a list of derived data sets in the project")
public class QueryDerivedDataSetApi extends AbstractApi<QueryDerivedDataSetApi.Input, PagingOutput<DerivedProjectDataSetOutputModel>> {

    @Autowired
    private ProjectDataSetService projectDataSetService;

    @Override
    protected ApiResult<PagingOutput<DerivedProjectDataSetOutputModel>> handle(Input input) throws StatusCodeWithException {
        return success(projectDataSetService.queryDerivedDataSet(input));
    }

    public static class Input extends PagingInput {
        @Check(name = "项目Id", require = true)
        private String projectId;

        @Check(name = "数据集类型", require = true)
        private DataResourceType dataResourceType;

        @Check(name = "来源")
        private ComponentType sourceType;

        @Check(name = "来源流程id")
        private String sourceFlowId;

        @Check(name = "来源任务id")
        private String sourceJobId;

        @Check(name = "数据集id")
        private String dataSetId;


        //region getter/setter

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public DataResourceType getDataResourceType() {
            return dataResourceType;
        }

        public void setDataResourceType(DataResourceType dataResourceType) {
            this.dataResourceType = dataResourceType;
        }

        public ComponentType getSourceType() {
            return sourceType;
        }

        public void setSourceType(ComponentType sourceType) {
            this.sourceType = sourceType;
        }

        public String getSourceFlowId() {
            return sourceFlowId;
        }

        public void setSourceFlowId(String sourceFlowId) {
            this.sourceFlowId = sourceFlowId;
        }

        public String getSourceJobId() {
            return sourceJobId;
        }

        public void setSourceJobId(String sourceJobId) {
            this.sourceJobId = sourceJobId;
        }

        public String getDataSetId() {
            return dataSetId;
        }

        public void setDataSetId(String dataSetId) {
            this.dataSetId = dataSetId;
        }


        //endregion
    }

}
