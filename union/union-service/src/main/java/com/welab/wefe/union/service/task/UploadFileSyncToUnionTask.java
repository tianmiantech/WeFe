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

package com.welab.wefe.union.service.task;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.http.HttpContentType;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.SM2Util;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.union.service.cache.UnionNodeConfigCache;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public class UploadFileSyncToUnionTask extends Thread {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private String baseUrl;
    private String api;
    private JObject params;
    private MultiValueMap<String, MultipartFile> files;

    public UploadFileSyncToUnionTask(String baseUrl, String api, JObject params, MultiValueMap<String, MultipartFile> files) {
        this.baseUrl = baseUrl;
        this.params = params;
        this.api = api;
        this.files = files;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 3; i++) {
            long retryInterval = 300 * i;
            HttpResponse response;
            String url = baseUrl + "/" + api;
            String data = params.toJSONString();

            String sign;
            try {
                sign = SM2Util.sign(data, UnionNodeConfigCache.getSm2PrivateKey());
            } catch (Exception e) {
                LOG.error("sign error", e);
                continue;
            }

            JObject reqeustBody = JObject.create();
            reqeustBody.append("data", data);
            reqeustBody.append("sign", sign);
            reqeustBody.append("currentBlockchainNodeId", UnionNodeConfigCache.currentBlockchainNodeId);

            HttpRequest request = HttpRequest
                    .create(url)
                    .setContentType(HttpContentType.MULTIPART);

            request.appendParameters(reqeustBody);

            for (Map.Entry<String, MultipartFile> item : files.toSingleValueMap().entrySet()) {
                try {
                    MultipartFile file = item.getValue();
                    ContentType contentType = StringUtil.isEmpty(file.getContentType())
                            ? ContentType.DEFAULT_BINARY
                            : ContentType.create(file.getContentType());

                    InputStreamBody streamBody = new InputStreamBody(
                            file.getInputStream(),
                            contentType,
                            file.getOriginalFilename()
                    );


                    request.appendParameter(item.getKey(), streamBody);
                } catch (IOException e) {
                    LOG.error("File read / write failed", e);
                }
            }

            response = request.post();


            if (!response.success()) {
                LOG.error("UploadFileSyncToUnion error,union response fail," + response.getMessage());
                ThreadUtil.sleep(retryInterval);
                continue;
            }

            JSONObject json;
            try {
                json = response.getBodyAsJson();
            } catch (JSONException e) {
                LOG.error("UploadFileSyncToUnion error,union response fail,", e);
                ThreadUtil.sleep(retryInterval);
                continue;
            }


            Integer code = json.getInteger("code");
            if (code == null || !code.equals(0)) {
                LOG.error("UploadFileSyncToUnion error,union response fail:" + json.toJSONString());
                ThreadUtil.sleep(retryInterval);
                continue;
            }

            break;
        }
    }
}
