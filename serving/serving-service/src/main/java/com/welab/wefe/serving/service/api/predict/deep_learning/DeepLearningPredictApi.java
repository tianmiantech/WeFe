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
import com.welab.wefe.common.file.decompression.dto.DecompressionResult;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.utils.DeepLearningUtil;
import com.welab.wefe.serving.service.utils.ServingFileUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

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
        String distDir = ServingFileUtil.DeepLearningModelFile
                .getZipFileUnzipDir(input.modelId)
                .toAbsolutePath()
                .toString();
        File zipFile = ServingFileUtil.DeepLearningModelFile.getZipFile(input.modelId);
        DecompressionResult result = SuperDecompressor.decompression(zipFile, distDir, false);

        //构造labelList
        Path labelList = ServingFileUtil
                .getBaseDir(ServingFileUtil.FileType.Temp)
                .resolve(input.modelId)
                .resolve("lable_list.txt");
        for (String label : input.labelList) {
            FileUtil.writeTextToFile(label + System.lineSeparator(), labelList, true);
        }

        //指定结果输出路径
        Path output = ServingFileUtil.DeepLearningModelFile.getPredictOutputPath(input.getModelId());

        //调用paddle_serving服务
        String resultStr = DeepLearningUtil.callPaddleServing(labelList.toString(), input.imagePath, output.toString());

        //转化结果
        return success(resultStr);
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "模型唯一标识")
        private String modelId;
        @Check(require = true, name = "标签list")
        private List<String> labelList;

        @Check(name = "检测图片路径")
        private String imagePath;


        //region getter/setter

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public List<String> getLabelList() {
            return labelList;
        }

        public void setLabelList(List<String> labelList) {
            this.labelList = labelList;
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
