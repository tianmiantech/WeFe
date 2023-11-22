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

package com.wolaidai.wefe.sdk;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.util.RSAUtil;

import java.util.TreeMap;

// 多方匿踪查询 用来生成http请求参数
public class MultiPir {
	// 私钥
	private static final String 测试客户1_privateKey="***";
	// 公钥
	private static final String 测试客户1_publicKey="***";
	// 客户code
	private static final String 测试客户1_code = "TEST***25";

	public static void main(String[] args) throws Exception {
		String dataStr = "{\n" +
				"  \"ids\": [\n" +
				"    {\n" +
				"      \"member_id\": \"d0f47307804844898ecfc65b875abe87\",\n" +
				"      \"model_id\": \"cee66626a97e42198bccb226dcd9743a_VertSecureBoost_16294251366419513\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"member_id\": \"1\",\n" +
				"      \"model_id\": \"2\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"member_id\": \"1\",\n" +
				"      \"model_id\": \"2\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"member_id\": \"1\",\n" +
				"      \"model_id\": \"2\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"member_id\": \"1\",\n" +
				"      \"model_id\": \"2\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"member_id\": \"1\",\n" +
				"      \"model_id\": \"2\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"index\":0,\n" +
				"  \"otMethod\":\"huack_ot\"\n" +
				"}";
		System.out.println("多方匿踪查询参数 huack_ot 方式:\t" + request(dataStr));

		dataStr = "{\n" +
				"  \"ids\": [\n" +
				"    {\n" +
				"      \"member_id\": \"d0f47307804844898ecfc65b875abe87\",\n" +
				"      \"model_id\": \"cee66626a97e42198bccb226dcd9743a_VertSecureBoost_16294251366419513\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"member_id\": \"1\",\n" +
				"      \"model_id\": \"2\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"member_id\": \"1\",\n" +
				"      \"model_id\": \"2\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"member_id\": \"1\",\n" +
				"      \"model_id\": \"2\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"member_id\": \"1\",\n" +
				"      \"model_id\": \"2\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"member_id\": \"1\",\n" +
				"      \"model_id\": \"2\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"index\":0,\n" +
				"  \"otMethod\":\"naorpinkas_ot\"\n" +
				"}";
		System.out.println("多方匿踪查询参数 naorpinkas_ot方式:\t" + request(dataStr));
        // 服务地址
		System.out.println("多方匿踪查询参数 url:https://xxxx/serving-service-01/api/*****");
	}

	protected static String request(String dataStr) throws Exception {
		TreeMap<String, Object> params = new TreeMap<>();
		params.put("data", JSONObject.parseObject(dataStr));
		String data = params.get("data").toString();
		String sign = "";
		try {
			sign = RSAUtil.sign(data, 测试客户1_privateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject body = new JSONObject();
		body.put("customer_id", 测试客户1_code);
		body.put("sign", sign);
		body.put("data", JSONObject.parseObject(data));
		boolean verified = RSAUtil.verify(params.get("data").toString().getBytes(),
				RSAUtil.getPublicKey(测试客户1_publicKey), sign);
		if(verified) {
			return body.toJSONString();
		}
		else{
			return "";
		}
	}
}
