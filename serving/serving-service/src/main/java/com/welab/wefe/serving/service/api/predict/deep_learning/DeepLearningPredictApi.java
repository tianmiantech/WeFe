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
package com.welab.wefe.serving.service.api.predict.deep_learning;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.file.decompression.SuperDecompressor;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.utils.DeepLearningUtil;
import com.welab.wefe.serving.service.utils.ServingFileUtil;

import java.io.File;
import java.nio.file.Path;

/**
 * @author hunter.zhao
 * @date 2022/3/24
 */
@Api(
        path = "predict/deep_learning",
        name = "深度学习预测",
        login = false
)
public class DeepLearningPredictApi extends AbstractApi<DeepLearningPredictApi.Input, String> {

    @Override
    protected ApiResult<String> handle(Input input) throws Exception {
        // zip 文件解压到以 taskId 命名的文件夹中
        String distDir = getWordDir(input.getModelId());

        //指定结果输出路径
        Path output = ServingFileUtil.DeepLearningModelFile.getPredictOutputPath(input.getModelId());

        //调用paddle_serving服务
        String resultStr = DeepLearningUtil.callPaddleServing(input.imagePath, "label_list.txt", output.toString(), distDir);

        //转化结果
        return success(resultStr);
    }

    private String getWordDir(String modelId) throws Exception {
        Path distDirPath = ServingFileUtil.DeepLearningModelFile
                .getZipFileUnzipDir(modelId)
                .toAbsolutePath();

        unZipModelFile(modelId, distDirPath);

        return distDirPath.toString();
    }

    private void unZipModelFile(String modelId, Path distDirPath) throws Exception {
        if (!distDirPath.toFile().exists()) {
            File zipFile = ServingFileUtil.DeepLearningModelFile.getZipFile(modelId);
            SuperDecompressor.decompression(zipFile, distDirPath.toString(), false);
        }
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "模型唯一标识")
        private String modelId;

        @Check(name = "检测图片路径")
        private String imagePath;


        //region getter/setter

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        //endregion
    }


    public static class Output {
        public String images;
        public JObject resultJson;

        public Output(String images, JObject resultJson) {
            this.images = images;
            this.resultJson = resultJson;
        }
    }

}
