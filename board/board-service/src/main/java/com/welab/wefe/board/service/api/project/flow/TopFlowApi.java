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
package com.welab.wefe.board.service.api.project.flow;

import com.welab.wefe.board.service.service.ProjectFlowService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane
 * @date 2022/3/22
 */
@Api(path = "project/flow/top", name = "设置项目流程置顶状态")
public class TopFlowApi extends AbstractNoneOutputApi<TopFlowApi.Input> {

    @Autowired
    private ProjectFlowService projectFlowService;

    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        projectFlowService.top(input.flowId, input.top);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "训练Id", require = true)
        public String flowId;
        @Check(name = "是否置顶", require = true)
        public boolean top;
    }
}
