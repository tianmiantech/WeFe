
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

package com.welab.wefe.mpc.sa.sdk.transfer.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyRequest;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyResponse;
import com.welab.wefe.mpc.sa.request.QuerySAResultRequest;
import com.welab.wefe.mpc.sa.request.QuerySAResultResponse;
import com.welab.wefe.mpc.sa.sdk.transfer.SecureAggregationTransferVariable;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * @author eval
 */
public class HttpTransferVariable implements SecureAggregationTransferVariable {
    @Override
    public QueryDiffieHellmanKeyResponse queryDiffieHellmanKey(String url, QueryDiffieHellmanKeyRequest request) {
        return JSON.parseObject(query(url, (JSONObject) JSONObject.toJSON(request)), QueryDiffieHellmanKeyResponse.class);
    }

    @Override
    public QuerySAResultResponse queryResult(String url, QuerySAResultRequest request) {
        return JSON.parseObject(query(url, (JSONObject) JSONObject.toJSON(request)), QuerySAResultResponse.class);
    }

    private String query(String url, JSONObject params) {
        params = new JSONObject(new TreeMap(params));
        String data = params.toJSONString();
        // TOdo
//        if (mConfig.isNeedSign()) {
//            String sign = SignUtil.sign(data, mConfig.getSignPrivateKey());
//            JSONObject body = new JSONObject();
//            body.put("member_id", mConfig.getCommercialId());
//            body.put("sign", sign);
//            body.put("data", data);
//            data = body.toJSONString();
//        }

        String response = HttpUtil.post(url, data);
        while (StrUtil.isEmpty(response)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response = HttpUtil.post(url, data);
        }

        return response;
    }

}
