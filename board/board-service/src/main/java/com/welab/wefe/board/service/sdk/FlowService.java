/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.welab.wefe.board.service.dto.globalconfig.FlowConfigModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.checkpoint.dto.ServiceAvailableCheckOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane.luo
 */
@Service
public class FlowService extends AbstractService {
    @Autowired
    private GlobalConfigService globalConfigService;

    public ServiceAvailableCheckOutput getAvailable() throws StatusCodeWithException {
        return request("/service/available", null)
                .toJavaObject(ServiceAvailableCheckOutput.class);
    }

    public JObject alive() throws StatusCodeWithException {
        return request("/service/alive", null);
    }

    private JObject request(String api, JSONObject params) throws StatusCodeWithException {
        FlowConfigModel flowConfig = globalConfigService.getModel(FlowConfigModel.class);
        if (flowConfig == null || StringUtil.isEmpty(flowConfig.intranetBaseUri)) {
            StatusCode.RPC_ERROR.throwException("尚未设置 flow 内网地址，请在[全局设置][系统设置]中设置 flow 服务的内网地址。");
        }

        if (params == null) {
            params = new JSONObject();
        }
        String data = params.toJSONString();

        if (!api.startsWith("/")) {
            api = "/" + api;
        }

        HttpResponse response = HttpRequest
                .create(globalConfigService.getModel(FlowConfigModel.class).intranetBaseUri + api)
                .setBody(data)
                .postJson();

        if (!response.success()) {
            StatusCode.RPC_ERROR.throwException(response.getMessage());
        }

        JObject json;
        try {
            json = new JObject(response.getBodyAsJson());
        } catch (JSONException e) {
            throw new StatusCodeWithException("flow 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
        }

        Integer code = json.getInteger("code");
        if (code == null || !code.equals(0)) {
            throw new StatusCodeWithException("flow 响应失败(" + code + ")：" + json.getString("message"), StatusCode.RPC_ERROR);
        }
        return json.getJObject("data");
    }
}
