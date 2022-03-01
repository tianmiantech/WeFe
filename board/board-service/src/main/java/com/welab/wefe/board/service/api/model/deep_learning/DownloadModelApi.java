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
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

        File file = WeFeFileSystem.CallDeepLearningModel.getModelFile(input.taskId);
        try {
            long start = System.currentTimeMillis();
            download(url, file);

            LOG.info("从飞桨下载模型耗时：" + TimeSpan.fromMs(System.currentTimeMillis() - start) + " taskId:" + input.taskId);
        } catch (Exception e) {
            LOG.error("下载模型失败：" + e.getMessage(), e);
            StatusCode.RPC_ERROR.throwException("下载模型失败：" + e.getMessage());
        }

        return file(file);
    }

    private void download(String url, File file) throws IOException {
        // 创建Http请求配置参数
        RequestConfig requestConfig = RequestConfig.custom()
                // 获取连接超时时间
                .setConnectionRequestTimeout(10 * 1000)
                // 请求超时时间
                .setConnectTimeout(10 * 1000)
                // 响应超时时间
                .setSocketTimeout(1000 * 60 * 60)
                .build();
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            InputStream is = response.getEntity().getContent();
            if (file.exists()) {
                file.delete();
            }
            file.getParentFile().mkdirs();
            FileOutputStream fileout = new FileOutputStream(file);
            /**
             * 根据实际运行效果 设置缓冲区大小
             */
            byte[] buffer = new byte[10 * 1024];
            int ch = 0;
            long downloadSize = 0;
            while ((ch = is.read(buffer)) != -1) {
                fileout.write(buffer, 0, ch);
                downloadSize += ch;
                if (downloadSize % 1024 == 0) {
                    LOG.info("模型下载进度：" + getSizeString(downloadSize));
                }
            }
            is.close();
            fileout.flush();
            fileout.close();
            LOG.info("模型下载完毕：" + getSizeString(downloadSize));
        } catch (Exception e) {
            throw e;
        } finally {
            httpGet.releaseConnection();
        }
    }

    private String getSizeString(long byteSize) {
        if (byteSize < 1024) {
            return byteSize + "byte";
        }
        if (byteSize < 1024 * 1024) {
            return BigDecimal.valueOf(byteSize)
                    .divide(BigDecimal.valueOf(1024), 2, RoundingMode.FLOOR) + "KB";
        }
        return BigDecimal.valueOf(byteSize)
                .divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.FLOOR) + "MB";
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String taskId;
    }

    @Override
    protected int parallelism() {
        return 1;
    }
}
