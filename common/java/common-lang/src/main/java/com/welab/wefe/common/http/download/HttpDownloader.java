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
package com.welab.wefe.common.http.download;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author zane
 * @date 2022/3/8
 */
public class HttpDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(HttpDownloader.class);

    private DownloadResult result;

    private HttpDownloader(String url, File file) {
        result = new DownloadResult();
        result.url = url;
        result.file = file;

        if (file.exists()) {
            throw new RuntimeException("文件已经存在，HttpDownloader 无法下载文件：" + file.getAbsolutePath());
        }
    }

    public static HttpDownloader create(String url, File file) {
        return new HttpDownloader(url, file);
    }

    public DownloadResult getResult() {
        refreshResult();
        return result;
    }

    public void refreshResult() {

        // 下载完毕
        if (result.status == DownloadStatus.downloading && result.file.exists()) {
            completed();
        }
        // 下载中
        if (getDownloadingFile().exists()) {
            downloading();
        }
    }

    /**
     * 开始下载
     */
    public HttpDownloader start() throws IOException {
        refreshResult();
        if (result.status != DownloadStatus.prepare) {
            return this;
        }

        doDownload();
        return this;
    }

    /**
     * 执行下载
     */
    private void doDownload() {
        // result.status = DownloadStatus.downloading
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
        HttpGet httpGet = new HttpGet(result.url);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            InputStream is = response.getEntity().getContent();
            File downloadingFile = getDownloadingFile();
            if (downloadingFile.exists()) {
                downloadingFile.delete();
            }
            downloadingFile.getParentFile().mkdirs();
            FileOutputStream fileout = new FileOutputStream(downloadingFile);
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
            // throw e;
        } finally {
            httpGet.releaseConnection();
        }
    }


    private void downloading() {
        File downloadingFile = getDownloadingFile();
        if (!downloadingFile.exists()) {
            throw new RuntimeException("文件不存在：" + downloadingFile.getAbsolutePath());
        }
        result.status = DownloadStatus.downloading;
        if (result.currentLength <= 0) {
            result.currentLength = downloadingFile.length();
        }
    }

    private void completed() {
        if (!result.file.exists()) {
            throw new RuntimeException("文件不存在：" + result.file.getAbsolutePath());
        }
        result.status = DownloadStatus.completed;
        if (result.currentLength <= 0) {
            result.currentLength = result.file.length();
        }
    }

    private File getDownloadingFile() {
        return new File(result.file.getAbsolutePath() + ".downloading");
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
}
