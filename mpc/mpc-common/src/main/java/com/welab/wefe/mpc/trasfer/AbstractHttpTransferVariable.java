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

package com.welab.wefe.mpc.trasfer;

import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author: eval
 * @Date: 2021-12-30
 **/
public abstract class AbstractHttpTransferVariable {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpTransferVariable.class);

    public <T> T query(Object request, String apiName, CommunicationConfig config, Class<T> clz) {
        String url = config.getServerUrl() + apiName;
        JSONObject data = (JSONObject) JSONObject.toJSON(request);
        return JSON.parseObject(query(url, data, config), clz);
    }

    public static String query(String url, JSONObject params, CommunicationConfig mConfig) {
        params = new JSONObject(new TreeMap(params));
        String data = params.toJSONString();
        if (mConfig.isNeedSign()) {
            String sign = SignUtil.sign(data, mConfig.getSignPrivateKey());
            JSONObject body = new JSONObject();
            body.put("customerId", mConfig.getCommercialId());
            body.put("sign", sign);
            body.put("data", params);
            data = body.toJSONString();
        }
        else {
        	JSONObject body = new JSONObject();
            body.put("customerId", mConfig.getCommercialId());
            body.put("sign", "");
            body.put("data", params);
            data = body.toJSONString();
        }
        logger.debug("request:" + data);
        HttpResponse response = HttpRequest.post(url).timeout(HttpGlobalConfig.getTimeout()).body(data).execute();
        logger.debug("response:" + response);
        while (response == null || response.getStatus() != HttpStatus.HTTP_OK) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response = HttpRequest.post(url).timeout(HttpGlobalConfig.getTimeout()).body(data).execute();
            logger.debug("response:" + response);
        }

        String responseString = response.body();
        JSONObject res = JSONObject.parseObject(responseString);
        String result = res.getString("data");
        logger.debug(url);
        logger.debug(JSONObject.toJSONString(res, true));
        return result;
    }
}
