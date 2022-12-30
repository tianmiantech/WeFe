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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.util.RSAUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.TreeMap;

// 多方匿踪查询 用来生成http请求参数，然后自己通过http请求
public class MultiPir {
    // 私钥
    private static final String customer_privateKey = "***"; // TODO
    // 公钥
    private static final String customer_publicKey = "***"; // TODO
    // 客户code
    private static final String customer_code = "***"; // TODO
    // Serving服务地址
    private static final String serverUrl = "https://***/***/"; // TODO
    // Service Api name
    private static final String apiName = "api/*****"; // TODO

    public static void main(String[] args) throws Exception {
        String dataStr = "{\n" +
                "  \"ids\": [\n" +
                "    {\n" +
                "      \"member_id\": \"*****\",\n" +
                "      \"model_id\": \"****\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"member_id\": \"dsghsdfg\",\n" +
                "      \"model_id\": \"qwer\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"member_id\": \"zsdfas\",\n" +
                "      \"model_id\": \"zxgasdf\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"member_id\": \"zdfasf\",\n" +
                "      \"model_id\": \"asdfaw\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"member_id\": \"zxcv\",\n" +
                "      \"model_id\": \"qwer\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"member_id\": \"zxdvfasd\",\n" +
                "      \"model_id\": \"asdf\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"index\":0\n" +
                "}";
        String params = request(dataStr);
        System.out.println("多方匿踪查询参数 naorpinkas_ot方式:\t" + params);
        // 服务地址
        System.out.println("多方匿踪查询参数 url:" + serverUrl + apiName);
        System.out.println("响应结果：" + sendPost(serverUrl + apiName, params));
    }

    protected static String request(String dataStr) throws Exception {
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("data", JSONObject.parseObject(dataStr));
        String data = params.get("data").toString();
        String sign = "";
        try {
            sign = RSAUtil.sign(data, customer_privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject body = new JSONObject();
        body.put("customer_id", customer_code);
        body.put("sign", sign);
        body.put("data", JSONObject.parseObject(data));
        body.put("requestId", "xxx");
        boolean verified = RSAUtil.verify(params.get("data").toString().getBytes(),
                RSAUtil.getPublicKey(customer_publicKey), sign);
        if (verified) {
            return body.toJSONString();
        } else {
            return "";
        }
    }
}
