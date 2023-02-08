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
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.sdk.PrivateInformationRetrievalQuery;
import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;

import java.util.List;

/**
 * 两方匿踪查询客户端 <br>
 * 配合 mpc-pir-sdk-1.0.0.jar， bcprov-jdk15on-1.69.jar使用 <br>
 * 编译 `javac -cp mpc-pir-sdk-1.0.0.jar:. PirClient.java` <br>
 * 运行 `java -cp mpc-pir-sdk-1.0.0.jar:. PirClient`
 */
public class PirClient {
    // 私钥
    private static final String customer_privateKey = "***"; // TODO
    // 公钥
    private static final String customer_publicKey = "***"; // TODO
    // code
    private static final String customer_code = "***"; // TODO
    // Serving服务地址
    private static final String serverUrl = "http://xxxxx.com/xxxx/"; // TODO 参考readme.md 的serverUrl
    // Service Api name
    private static final String apiName = "api/*****"; // TODO 参考readme.md 的apiName

	public static void main(String[] args) {
		CommunicationConfig communicationConfig = new CommunicationConfig();
		// 服务地址
		communicationConfig.setServerUrl(serverUrl);
		communicationConfig.setApiName(apiName);
		communicationConfig.setCommercialId(customer_code); // 客户code
		communicationConfig.setSignPrivateKey(customer_privateKey); // 客户私钥
//		communicationConfig.setSecretKeyType("sm2");
		communicationConfig.setSecretKeyType("rsa");
		// params
		String idsStr = "[\n" +
				"  {\n" +
				"    \"member_id\": \"****\",\n" +
				"    \"model_id\": \"****\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"member_id\": \"asdf\",\n" +
				"    \"model_id\": \"zxcvzxv\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"member_id\": \"asdfasdf\",\n" +
				"    \"model_id\": \"dsfgsdfg\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"member_id\": \"asdfasq\",\n" +
				"    \"model_id\": \"xbsdfg\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"member_id\": \"qwerqwe\",\n" +
				"    \"model_id\": \"bdfgsd\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"member_id\": \"kjlkj\",\n" +
				"    \"model_id\": \"asdfasdf\"\n" +
				"  }\n" +
				"]";
		int targetIndex = 0; // 真实查询目标对应的数组下标
		List<JSONObject> ids = JSONObject.parseArray(idsStr, JSONObject.class);
		PrivateInformationRetrievalConfig config = new PrivateInformationRetrievalConfig((List) ids, 0, 10, null);
		PrivateInformationRetrievalQuery privateInformationRetrievalQuery = new PrivateInformationRetrievalQuery();
		try {
			config.setTargetIndex(targetIndex);
			String result = privateInformationRetrievalQuery.query(config, communicationConfig);
			System.out.println("result = " + result); // 获取结果 如果 result = {"rand":"thisisemptyresult"} 说明没结果
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}