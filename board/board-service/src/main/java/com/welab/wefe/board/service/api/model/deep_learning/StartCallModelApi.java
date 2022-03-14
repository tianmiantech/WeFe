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
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.file.decompression.SuperDecompressor;
import com.welab.wefe.common.file.decompression.dto.DecompressionResult;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * @author zane
 * @date 2022/2/14
 */
@Api(path = "model/deep_learning/call/start", name = "调用深度学习模型")
public class StartCallModelApi extends AbstractApi<StartCallModelApi.Input, StartCallModelApi.Output> {

    @Autowired
    private TaskService taskService;
    @Autowired
    private PaddleVisualService paddleVisualService;

    @Override
    protected ApiResult<Output> handle(StartCallModelApi.Input input) throws Exception {
        // zip 文件解压到以 taskId 命名的文件夹中
        String distDir = WeFeFileSystem.CallDeepLearningModel
                .getZipFileUnzipDir(input.taskId)
                .toAbsolutePath()
                .toString();
        File zipFile = WeFeFileSystem.CallDeepLearningModel.getZipFile(input.taskId);
        DecompressionResult result = SuperDecompressor.decompression(zipFile, distDir, false);

        // 安全起见，把非图片文件删除掉。
        int imageCount = 0;
        for (File file : result.files) {
            if (FileUtil.isImage(file)) {
                imageCount++;
                // 将文件移动到解压目录的根目录，避免zip包内有子文件导致路径不好管理。
                FileUtil.moveFile(file, distDir);
            } else {
                file.delete();
            }
        }

        // 调用飞桨开始推理
        TaskMySqlModel task = taskService.findOne(input.taskId);
        if (task == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("此task不存在:" + input.taskId);
        }

        JObject dataSetInfo = JObject.create();
        dataSetInfo.put("download_url", buildZipDownloadUrl(input.taskId));
        dataSetInfo.put("name", input.filename);

        JSONObject json = JSON.parseObject(task.getTaskConf());
        json.put("data_set", dataSetInfo);

        JObject response = paddleVisualService.infer(json);

        return success(new Output(imageCount, response));
    }

    private String buildZipDownloadUrl(String taskId) {
        Api annotation = DownloadDataSetZipApi.class.getAnnotation(Api.class);

        return Launcher.getBean(GlobalConfigService.class)
                .getBoardConfig()
                .intranetBaseUri
                + "/"
                + annotation.path()
                + "?taskId=" + taskId;
    }

    public static class Output {
        public int fileCount;
        public JObject response;

        public Output(int fileCount, JObject response) {
            this.fileCount = fileCount;
            this.response = response;
        }
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String taskId;

        @Check(require = true, messageOnEmpty = "请指定数据集文件")
        public String filename;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            // 如果是单张图片，要打包为 zip。
            if (FileUtil.isImage(filename)) {
                File zipFile = WeFeFileSystem.CallDeepLearningModel.singleImageToZip(filename, taskId);
                filename = zipFile.getName();
            }

            WeFeFileSystem.CallDeepLearningModel.renameZipFile(filename, taskId);

        }
    }
}
