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

package com.welab.wefe.board.service.api.project.dataset;

import com.welab.wefe.board.service.dto.entity.ProjectDataSetInput;
import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zan.luo
 */
@Api(path = "project/data_set/add", name = "add data set to project")
public class AddDataSetApi extends AbstractNoneOutputApi<AddDataSetApi.Input> {

    @Autowired
    ProjectService service;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        service.addProjectDataSet(input);
        return success();
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "项目ID", require = true)
        private String projectId;

        @Check(name = "数据集列表", require = true)
        private List<ProjectDataSetInput> dataSetList;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public List<ProjectDataSetInput> getDataSetList() {
            return dataSetList;
        }

        public void setDataSetList(List<ProjectDataSetInput> dataSetList) {
            this.dataSetList = dataSetList;
        }

    }
}
