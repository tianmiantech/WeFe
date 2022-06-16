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

import com.welab.wefe.board.service.base.file_system.WeFeFileSystem;
import com.welab.wefe.board.service.component.Components;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.dto.entity.job.TaskResultOutputModel;
import com.welab.wefe.board.service.service.TaskService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.UUID;

/**
 * @author zane
 * @date 2022/2/14
 */
@Api(path = "model/deep_learning/infer/result/download", name = "下载模型推理结果")
public class DownloadInferResultApi extends AbstractApi<DownloadInferResultApi.Input, ResponseEntity<?>> {

    @Autowired
    private TaskService taskService;

    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws Exception {
        TaskMySqlModel task = taskService.findOne(input.taskId);
        if (task == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("task 不存在：" + input.taskId);
        }

        TaskResultOutputModel result = Components.get(task.getTaskType()).getTaskResult(task.getTaskId(), "infer");

        File file = WeFeFileSystem
                .getBaseDir(WeFeFileSystem.UseType.Temp)
                .resolve(UUID.randomUUID() + ".json")
                .toFile();

        FileUtil.writeTextToFile(result.getResult().toJSONString(), file.toPath(), false);

        return file(file);
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String taskId;
    }
}
