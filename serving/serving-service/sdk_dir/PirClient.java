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
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.sdk.PrivateInformationRetrievalQuery;
import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;

import java.util.List;

// 匿踪查询 pir
public class PirClient {
    // 私钥
    private static final String 测试客户1_privateKey="***";
    // 公钥
    private static final String 测试客户1_publicKey="***";
    // 客户code
    private static final String 测试客户1_code = "TEST***25";

	public static final String NAORPINKAS_OT = "naorpinkas_ot";
	public static final String HUACK_OT = "huack_ot";

	public static void main(String[] args) {
		CommunicationConfig communicationConfig = new CommunicationConfig();
		// 服务地址
		communicationConfig.setServerUrl("https://****/serving-service-01/");
		communicationConfig.setApiName("api/*****");
		communicationConfig.setNeedSign(true);  // 是否需要签名
		communicationConfig.setCommercialId(测试客户1_code); // 客户code
		communicationConfig.setSignPrivateKey(测试客户1_privateKey); // 客户私钥
		// params
		String idsStr = "[\n" +
				"  {\n" +
				"    \"member_id\": \"d0f47307804844898ecfc65b875abe87\",\n" +
				"    \"model_id\": \"cee66626a97e42198bccb226dcd9743a_VertSecureBoost_16294251366419513\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"member_id\": \"1\",\n" +
				"    \"model_id\": \"2\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"member_id\": \"1\",\n" +
				"    \"model_id\": \"2\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"member_id\": \"1\",\n" +
				"    \"model_id\": \"2\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"member_id\": \"1\",\n" +
				"    \"model_id\": \"2\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"member_id\": \"1\",\n" +
				"    \"model_id\": \"2\"\n" +
				"  }\n" +
				"]";
		int targetIndex = 0; // 目标
		List<JSONObject> ids = JSONObject.parseArray(idsStr, JSONObject.class);
		PrivateInformationRetrievalConfig config = new PrivateInformationRetrievalConfig((List) ids, 0, 10, null);
		PrivateInformationRetrievalQuery privateInformationRetrievalQuery = new PrivateInformationRetrievalQuery();
		try {
			config.setTargetIndex(targetIndex);
			String result = privateInformationRetrievalQuery.query(config, communicationConfig);
			System.out.println("result = " + result); // get result
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}