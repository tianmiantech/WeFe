
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

package com.welab.wefe.mpc.pir.sdk.trasfer.impl;

import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.pir.PrivateInformationRetrievalApiName;
import com.welab.wefe.mpc.pir.request.*;
import com.welab.wefe.mpc.pir.sdk.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.sdk.trasfer.PrivateInformationRetrievalTransferVariable;
import com.welab.wefe.mpc.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * @author eval
 */
public class HttpTransferVariable implements PrivateInformationRetrievalTransferVariable {
    private static final Logger logger = LoggerFactory.getLogger(HttpTransferVariable.class);

    private CommunicationConfig mConfig;

    public HttpTransferVariable(CommunicationConfig config) {
        mConfig = config;
    }

    @Override
    public QueryRandomResponse queryRandom(QueryRandomRequest request) {
        return JSON.parseObject(query(mConfig.getServerUrl() + PrivateInformationRetrievalApiName.RANDOM, javaBeanToRequestJsonString(request, PrivateInformationRetrievalApiName.RANDOM)), QueryRandomResponse.class);
    }

    @Override
    public QueryRandomLegalResponse queryRandomLegal(QueryRandomLegalRequest request) {
        return JSON.parseObject(query(mConfig.getServerUrl() + PrivateInformationRetrievalApiName.RANDOM_LEGAL, javaBeanToRequestJsonString(request, PrivateInformationRetrievalApiName.RANDOM_LEGAL)), QueryRandomLegalResponse.class);
    }

    @Override
    public QueryKeysResponse queryKeys(QueryKeysRequest request) {
        return JSON.parseObject(query(mConfig.getServerUrl() + mConfig.getApiName(), javaBeanToRequestJsonString(request, PrivateInformationRetrievalApiName.KEYS)), QueryKeysResponse.class);
    }

    @Override
    public QueryPIRResultsResponse queryResults(QueryPIRResultsRequest request) {
        return JSON.parseObject(query(mConfig.getServerUrl() + PrivateInformationRetrievalApiName.RESULTS, javaBeanToRequestJsonString(request, PrivateInformationRetrievalApiName.RESULTS)), QueryPIRResultsResponse.class);
    }

    private JSONObject javaBeanToRequestJsonString(Object data, String apiName) {
        return (JSONObject) JSONObject.toJSON(data);
    }

    private String query(String url, JSONObject params) {
        params = new JSONObject(new TreeMap(params));
        String data = params.toJSONString();
        if (mConfig.isNeedSign()) {
            String sign = SignUtil.sign(data, mConfig.getSignPrivateKey());
            JSONObject body = new JSONObject();
            body.put("member_id", mConfig.getCommercialId());
            body.put("sign", sign);
            body.put("data", data);
            data = body.toJSONString();
        }

        HttpResponse response = HttpRequest.post(url).timeout(HttpGlobalConfig.getTimeout()).body(data).execute();

        while (response == null || response.getStatus() != HttpStatus.HTTP_OK) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response = HttpRequest.post(url).timeout(HttpGlobalConfig.getTimeout()).body(data).execute();
        }

        String responseString = response.body();
        JSONObject res = JSONObject.parseObject(responseString);
        String result = res.getJSONObject("data").getString("result");
        logger.debug(url);
        logger.debug(JSONObject.toJSONString(res, true));
        return result;
    }
}
