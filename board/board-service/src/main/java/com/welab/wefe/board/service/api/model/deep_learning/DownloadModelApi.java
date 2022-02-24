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
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.dto.globalconfig.DeepLearningConfigModel;
import com.welab.wefe.board.service.service.TaskService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.File;

/**
 * @author zane
 * @date 2022/2/14
 */
@Api(path = "model/deep_learning/download", name = "下载模型")
public class DownloadModelApi extends AbstractApi<DownloadModelApi.Input, ResponseEntity<?>> {

    @Autowired
    private TaskService taskService;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws Exception {
        TaskMySqlModel task = taskService.findOne(input.taskId);
        DeepLearningConfigModel deepLearningConfig = globalConfigService.getDeepLearningConfig();
        String url = deepLearningConfig.paddleVisualDlBaseUrl + "/serving_model/download?task_id=" + task.getTaskId() + "&job_id=" + task.getJobId();
        long start = System.currentTimeMillis();
        HttpResponse response = HttpRequest
                .create(url)
                // 超时时间：一小时
                .setTimeout(1000 * 60 * 60)
                .get();
        LOG.info("从飞桨下载模型耗时：" + (System.currentTimeMillis() - start) + "ms taskId:" + input.taskId);

        if (!response.success()) {
            StatusCode.RPC_ERROR.throwException("请求飞桨服务失败：" + response.getMessage());
        }
        String filePath = WeFeFileSystem.CallDeepLearningModel.getModelFile(input.taskId).getAbsolutePath();
        File file = response.getBodyAsFile(filePath);
        return file(file);
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String taskId;
    }
}
