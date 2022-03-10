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

import com.welab.wefe.board.service.database.entity.job.ModelOotRecordMysqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowMySqlModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectFlowDetailOutputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.ModelOotRecordService;
import com.welab.wefe.board.service.service.ProjectFlowService;
import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "project/flow/detail", name = "get flow detail")
public class DetailFlowApi extends AbstractApi<DetailFlowApi.Input, ProjectFlowDetailOutputModel> {

    @Autowired
    private ProjectFlowService projectFlowService;
    @Autowired
    ProjectService projectService;
    @Autowired
    private ModelOotRecordService modelOotRecordService;

    @Override
    protected ApiResult<ProjectFlowDetailOutputModel> handle(Input input) throws StatusCodeWithException {
        ProjectFlowMySqlModel flow = projectFlowService.findOne(input.flowId);

        if (flow == null) {
            return success();
        }

        ProjectFlowDetailOutputModel output = ModelMapper.map(flow, ProjectFlowDetailOutputModel.class);
        output.setProject(projectService.detail(flow.getProjectId()));
        output.setParamsIsNullFlowNodes(projectFlowService.getParamsIsNullFlowNodes(input.flowId));
        output.setIsCreator(CacheObjects.isCurrentMemberAccount(flow.getCreatedBy()));

        // OOT model
        ModelOotRecordMysqlModel modelOotRecordMysqlModel = modelOotRecordService.findByFlowId(input.flowId);
        if (null != modelOotRecordMysqlModel) {
            output.setOotJobId(modelOotRecordMysqlModel.getOotJobId());
            output.setOotModelFlowNodeId(modelOotRecordMysqlModel.getOotModelFlowNodeId());
        }
        return success(output);
    }

    public static class Input extends AbstractApiInput {
        public Input() {
        }

        public Input(String flowId) {
            this.flowId = flowId;
        }

        @Check(name = "流程主键", require = true)
        private String flowId;

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

    }

}
