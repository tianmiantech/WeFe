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

package com.welab.wefe.board.service.api.project.flow;

import com.welab.wefe.board.service.dto.entity.project.ProjectFlowProgressOutputModel;
import com.welab.wefe.board.service.service.ProjectFlowService;
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
@Api(path = "project/flow/get_progress", name = "Get the latest progress of the specified flow")
public class GetProgressApi extends AbstractApi<GetProgressApi.Input, GetProgressApi.Output> {

    @Autowired
    ProjectFlowService flowService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        List<ProjectFlowProgressOutputModel> list = flowService.getProgress(input.flowIdList);
        return success(new Output(list));
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "流程 id 列表", require = true)
        private List<String> flowIdList;

        public List<String> getFlowIdList() {
            return flowIdList;
        }

        public void setFlowIdList(List<String> flowIdList) {
            this.flowIdList = flowIdList;
        }
    }

    public static class Output {
        public List<ProjectFlowProgressOutputModel> list;

        public Output(List<ProjectFlowProgressOutputModel> list) {
            this.list = list;
        }
    }
}
