/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.api.project.flow;

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.project.ProjectFlowListOutputModel;
import com.welab.wefe.board.service.service.ProjectFlowService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.ProjectFlowStatisticsStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author winter.zou
 */
@Api(path = "project/flow/query", name = "query flow list")
public class FlowQueryApi extends AbstractApi<FlowQueryApi.Input, PagingOutput<ProjectFlowListOutputModel>> {

    @Autowired
    ProjectFlowService flowService;

    @Override
    protected ApiResult<PagingOutput<ProjectFlowListOutputModel>> handle(Input input) throws StatusCodeWithException {
        return success(flowService.query(input));
    }

    public static class Input extends PagingInput {

        @Check(name = "是否已被删除")
        public boolean deleted = false;
        @Check(name = "项目ID 主键")
        public String projectId;
        @Check(name = "flow id 列表")
        public List<String> flowIdList;

        @Check(name = "联邦任务类型（横向/纵向）")
        public FederatedLearningType federatedLearningType;
        @Check(name = "状态")
        public ProjectFlowStatisticsStatus status;
        @Check(name = "上传者")
        public String creator;

    }
}
