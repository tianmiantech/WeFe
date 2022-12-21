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

package com.welab.wefe.mpc.pir.sdk;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;

/**
 * 两方匿踪查询客户端 <br>
 * 配合 mpc-pir-sdk-1.0.0.jar使用 <br>
 * 编译 `javac -cp mpc-pir-sdk-1.0.0.jar:. PirClient.java` <br>
 * 运行 `java -cp mpc-pir-sdk-1.0.0.jar:. PirClient`
 */
public class PirClient {
    // 私钥
    private static final String customer_privateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCW4gsKl4TWdJtq3+TDpOOUrB+w/GEtU8kQ5zjT9MpPC3FQ9PGtzl3PANp2SQFNIhwxRs0UMn2NC5yrBlb/wNhTqYVx6wr74I3O9cyaJQZy4tgipgfiHsi3zEyfQOHbmLwMay+1g16JfuNXCP20mIpXoLFps1fxDm8vDUNJ0m+/Q8WunjZjVQzGFFn+YpeRRvjR+KjO434j4nAtOK7N3OgJeG2XFrO8dYQNbfM1gPW31ZVq361lOJEGZGEVOGtjvwASt0EoHpTE7ZdiHO7uvcYMdnnmITEoEoigBu425ORbVc7gKh/i2XJG0ibIaRVLcRLmq+DjO4JZ0zwYC8HYf+aHAgMBAAECggEAMZmNV6YHnRgoHzIK2eXSioUqxduxdl0cs9nO7J6EAwgX8C0VZbV5URtOtaO6MckMy4Yi1IjkjQFMN0yWPO0AMECCBbjmm3ZuJ6LV57ZUMGAx/PHnlYXZJdKbPZ7uoGnNJ6f0vw64fSqhQXNLM5sRn0zMBZtXRX8hPa52eEYdRhpX0x266L/LMT5DHLiHrsCTxw/alwjIz4/Cqolukz0HgRIj1sUPJvbOSooTk65tk7lLuBWJAbDUS+JBSma/WlHcIrU55X8Zm1Ksh+SZ3wSNDkvP/VzFYCgbJT4XtUuEMUbXA0A+wk2lIqOiH5NwTEcdKgsb0jKkUX/ID1WkE1zMAQKBgQDSoh//tYt4KfKqT5DC2m3nJCux7cK2GnkW9p9figFPZ03Hp6lMYGwIKlks4UqFQghB/+i2iW53FHIBaOPpHnSzkQDavtF28W2xx9ukB1GmvkdFRlqGwlrYgvs0WvzwAogEptFEHfoBgV+OmH+4/PracU23jZMiF0ElZPXS3oVvQQKBgQC3YWlrK7YkI9yeuf5cwWWKIDYVH0IN/2xdLbWf6qXCSvnSyxpwoLIPZ/jxtBsL9G/jIlKvegturwp1Ml21fvpe5PC2hABpqAF3lg8US7sR3jEgjV9lg7EW5/ylwsWGTdX2Nmi1ttiypTZ1i/W4VER8gjKmFkUmUYqnxge6lfarxwKBgAhdrh2u7UIxkFTZYiuLF3BlzGJ9yg0HkiWqV/wodBLeTIWQkDXbYo8Ud4RD0nzmeYN9kZWmcb/DDSAnNV4t+n6jVMBbceBiiHPWN1AVhlW0GwqSV8ggchFFaorSzlcOEq70nT0yX5qts8jrH6+ORLxmYarXsa9Z8xl0IBtnAS/BAoGAEyOflQfi4rK7e/V9jsDAkEH8Ywf4JO3bqX3zztT2p+ibWlzITaj37JO1b8SUbhL8+n/CkX3zY4HxXXn8Pc0a5HyQgvwT7vMR9CE9QakFxT+jcPpQUpAEE5fyznodk37cLe5Pz2deBocnv+zzoGFrJHOSRbNdVDp+djQ86kRsklECgYAtWKT/GVRAccOdDo3uN87aAWb/awhDGIeh2emyOPKAb2Lil9F4ZgC+mpwGzLHqVr2zlpAEfWz6RKmTyh0UhclBRTiO0S1LW4p0c8SVS6fzBWGlwO8eLSK0S2/JqzVBhzyDCbFT0du9jffSCVbHYhOPjzAW0woNIxTS8zvKDQBlbQ=="; // TODO
    // 公钥
    private static final String customer_publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAluILCpeE1nSbat/kw6TjlKwfsPxhLVPJEOc40/TKTwtxUPTxrc5dzwDadkkBTSIcMUbNFDJ9jQucqwZW/8DYU6mFcesK++CNzvXMmiUGcuLYIqYH4h7It8xMn0Dh25i8DGsvtYNeiX7jVwj9tJiKV6CxabNX8Q5vLw1DSdJvv0PFrp42Y1UMxhRZ/mKXkUb40fiozuN+I+JwLTiuzdzoCXhtlxazvHWEDW3zNYD1t9WVat+tZTiRBmRhFThrY78AErdBKB6UxO2XYhzu7r3GDHZ55iExKBKIoAbuNuTkW1XO4Cof4tlyRtImyGkVS3ES5qvg4zuCWdM8GAvB2H/mhwIDAQAB"; // TODO
    // code
    private static final String customer_code = "WINTER_WELAB"; // TODO
    // Serving服务地址
    private static final String serverUrl = "https://xbd-fat.tianmiantech.com/serving-service-03/"; // TODO 参考readme.md 的serverUrl
    // Service Api name
    private static final String apiName = "api/both_disappear/query"; // TODO 参考readme.md 的apiName

	public static void main(String[] args) {
		CommunicationConfig communicationConfig = new CommunicationConfig();
		// 服务地址
		communicationConfig.setServerUrl(serverUrl);
		communicationConfig.setApiName(apiName);
		communicationConfig.setCommercialId(customer_code); // 客户code
		communicationConfig.setSignPrivateKey(customer_privateKey); // 客户私钥
		// params
		String idsStr = "[\n" +
				"  {\n" +
				"    \"member_id\": \"087973c99d26410683944bf3f46c8635\",\n" +
				"    \"model_id\": \"a4051c988e6647b988e4a41d49726556_VertLR_16420489239755507\"\n" +
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