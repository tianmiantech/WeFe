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
package com.welab.wefe.board.service.sdk;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.dto.globalconfig.DeepLearningConfigModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane
 * @date 2022/2/19
 */
@Service
public class PaddleVisualService extends AbstractService {
    @Autowired
    private GlobalConfigService globalConfigService;

    public JObject infer(JSONObject params) throws StatusCodeWithException {
        return request("/infer", params);
    }

    private JObject request(String api, JSONObject params) throws StatusCodeWithException {
        DeepLearningConfigModel deepLearningConfig = globalConfigService.getDeepLearningConfig();

        if (deepLearningConfig == null || StringUtil.isEmpty(deepLearningConfig.paddleVisualDlBaseUrl)) {
            StatusCode.RPC_ERROR.throwException("尚未设置VisualFL服务地址，请在[全局设置][计算引擎设置]中设置VisualFL服务地址。");
        }

        if (params == null) {
            params = new JSONObject();
        }
        String data = params.toJSONString();

        if (!api.startsWith("/")) {
            api = "/" + api;
        }

        HttpResponse response = HttpRequest
                .create(deepLearningConfig.paddleVisualDlBaseUrl + api)
                .setBody(data)
                .postJson();

        if (!response.success()) {
            StatusCode.RPC_ERROR.throwException(response.getMessage());
        }

        JObject json;
        try {
            json = new JObject(response.getBodyAsJson());
        } catch (JSONException e) {
            throw new StatusCodeWithException("paddle 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
        }

        Integer code = json.getInteger("code");
        if (code == null || !code.equals(200)) {
            throw new StatusCodeWithException("paddle 响应失败(" + code + ")：" + json.getString("message"), StatusCode.RPC_ERROR);
        }
        return json;
    }
}
