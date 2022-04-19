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
package com.welab.wefe.board.service.api.model.deep_learning;

import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane
 * @date 2022/4/14
 */
@Api(path = "model/deep_learning/infer/stop", name = "中止推理")
public class StopInferApi extends AbstractNoneOutputApi<StopInferApi.Input> {

    @Autowired
    private TaskResultService taskResultService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        taskResultService.stopDeepLearningInfer(input.taskId);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String taskId;
    }
}
