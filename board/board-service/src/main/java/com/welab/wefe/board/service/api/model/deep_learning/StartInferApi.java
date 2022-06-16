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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.base.file_system.WeFeFileSystem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.sdk.PaddleVisualService;
import com.welab.wefe.board.service.service.TaskService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.UUID;

/**
 * @author zane
 * @date 2022/2/14
 */
@Api(path = "model/deep_learning/infer/start", name = "调用深度学习模型")
public class StartInferApi extends AbstractApi<StartInferApi.Input, StartInferApi.Output> {

    @Autowired
    private TaskService taskService;
    @Autowired
    private PaddleVisualService paddleVisualService;

    @Override
    protected ApiResult<Output> handle(StartInferApi.Input input) throws Exception {
        File rawFile = WeFeFileSystem.CallDeepLearningModel.getRawFile(input.filename);

        TaskMySqlModel task = taskService.findOne(input.taskId);
        if (task == null) {
            rawFile.delete();
            StatusCode.PARAMETER_VALUE_INVALID.throwException("此task不存在:" + input.taskId);
        }

        // 考虑到文件名冲突、并发问题，这里使用UUID作为本次预测的标识号。
        String inferSessionId = UUID.randomUUID().toString().replace("-", "");

        // 如果是单张图片
        if (FileUtil.isImage(rawFile)) {
            WeFeFileSystem.CallDeepLearningModel.moveSingleImageToSessionDir(rawFile, input.taskId, inferSessionId);
        } else {
            WeFeFileSystem.CallDeepLearningModel.moveZipFileToSessionDir(rawFile, input.taskId, inferSessionId);
        }

        File zipFile = WeFeFileSystem.CallDeepLearningModel.zipImageSimpleDir(input.taskId, inferSessionId);

        // 调用VisualFL开始推理
        JObject dataSetInfo = JObject.create();
        dataSetInfo.put("download_url", buildZipDownloadUrl(input.taskId, inferSessionId));
        dataSetInfo.put("name", zipFile.getName());
        dataSetInfo.put("infer_session_id", inferSessionId);

        JSONObject json = JSON.parseObject(task.getTaskConf());
        json.put("data_set", dataSetInfo);

        JObject response = paddleVisualService.infer(json);

        return success(new Output(inferSessionId, response));
    }

    private String buildZipDownloadUrl(String taskId, String inferSessionId) {
        Api annotation = DownloadDataSetZipApi.class.getAnnotation(Api.class);

        return Launcher.getBean(GlobalConfigService.class)
                .getBoardConfig()
                .intranetBaseUri
                + "/"
                + annotation.path()
                + "?taskId=" + taskId
                + "&inferSessionId=" + inferSessionId;
    }

    public static class Output {
        public String inferSessionId;
        public JObject response;

        public Output(String inferSessionId, JObject response) {
            this.inferSessionId = inferSessionId;
            this.response = response;
        }
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String taskId;

        @Check(require = true, messageOnEmpty = "请指定数据集文件")
        public String filename;
    }
}
