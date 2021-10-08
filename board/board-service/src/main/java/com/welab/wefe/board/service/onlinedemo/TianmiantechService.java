/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.onlinedemo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zane
 */
@Service
public class TianmiantechService extends AbstractService {
    @Value("${tianmiantech.api.base-url:}")
    private String TIANMIANTECH_BASE_URL;
    @Value("${tianmiantech.rsa.public:}")
    private String TIANMIANTECH_RSA_PUBLIC;
    @Value("${tianmiantech.rsa.private:}")
    private String TIANMIANTECH_RSA_PRIVATE;

    /**
     * call tianmiantech service
     */
    public JObject through(String api, Map<String, Object> params) throws StatusCodeWithException {
        return request(api, params);
    }

    /**
     * https://docs.tianmiantech.com/web/#/7/41
     */
    private JObject request(String api, Map<String, Object> params) throws StatusCodeWithException {
        if (params == null) {
            params = new JObject();
        }

        params.put("timestamp", System.currentTimeMillis());
        params.put("sign", sign(params));

        if (!api.startsWith("/")) {
            api = "/" + api;
        }

        HttpResponse response = HttpRequest
                .create(TIANMIANTECH_BASE_URL + api)
                .setBody(JSON.toJSONString(params))
                .postJson();

        if (!response.success()) {
            StatusCode.RPC_ERROR.throwException(response.getMessage());
        }

        JObject json;
        try {
            json = new JObject(response.getBodyAsJson());
        } catch (JSONException e) {
            throw new StatusCodeWithException("tianmiantech 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
        }

        Integer code = json.getInteger("status");
        if (code == null || !code.equals(200)) {
            throw new StatusCodeWithException("tianmiantech 响应失败(" + code + ")：" + json.getString("message"), StatusCode.RPC_ERROR);
        }
        return json.getJObject("data");
    }

    public String sign(Map<String, Object> params) throws StatusCodeWithException {
        List<String> dataList = params.values().stream().map(String::valueOf).collect(Collectors.toList());
        String[] sortDataArr = new String[dataList.size()];
        dataList.toArray(sortDataArr);
        Arrays.sort(sortDataArr);
        String dataStr = String.join(",", sortDataArr);

        try {
            return RSAUtil.sign(dataStr, TIANMIANTECH_RSA_PRIVATE, "utf-8");
        } catch (Exception e) {
            StatusCode.RSA_ERROR.throwException(e);
        }
        return null;
    }
}
