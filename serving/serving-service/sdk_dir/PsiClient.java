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
import com.welab.wefe.mpc.psi.sdk.PrivateSetIntersection;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

// 交集查询 psi
public class PsiClient {
    // 私钥
    private static final String 测试客户1_privateKey="***";
    // 公钥
    private static final String 测试客户1_publicKey="***";
    // 客户code
    private static final String 测试客户1_code = "TEST***25";

	public static void main(String[] args) {
		// params
		JSONObject data = new JSONObject();
		data.put("phone_number", "******");
		String clientId = getMD5String(data.getString("phone_number"));
		List<String> clientIds = Arrays.asList(new String[] { clientId });

		PrivateSetIntersection privateSetIntersection = new PrivateSetIntersection();
		CommunicationConfig config = new CommunicationConfig();
		config.setNeedSign(true); // 是否需要签名
		config.setSignPrivateKey(测试客户1_privateKey);//私钥
		config.setCommercialId(测试客户1_code); // 客户ID
        // 服务地址
		config.setServerUrl("http://*********/serving-service-01/");
		config.setApiName("api/*****");

		List<String> result = privateSetIntersection.query(config, clientIds, 1024);
		System.out.println(clientId);
		System.out.println(result);
	}

	public static String getMD5String(String str) {
		try {
			// 生成一个MD5加密计算摘要
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 计算md5函数
			md.update(str.getBytes());
			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
