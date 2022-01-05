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

package com.welab.wefe.mpc.psi.sdk;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

public class PrivateSetIntersectionTest1 {

	@Test
	public void test() {
		JSONObject data = new JSONObject();
		data.put("phone_number", "xxxxxx");
		data.put("nickname", "xxxxxx");
		data.put("email", "xxxxx@xxxxx");
		String clientId = MD5Util.getMD5String(data.getString("phone_number") + data.getString("nickname"))
				+ SHA256Utils.getSHA256(data.getString("email"));
		List<String> clientIds = Arrays.asList(new String[] { clientId });
		int keySize = 1024;
		PrivateSetIntersection privateSetIntersection = new PrivateSetIntersection();
		List<String> result = privateSetIntersection.query("localhost:8080/serving-service/api/query/black-list-match",
				clientIds, keySize);
		System.out.println(clientId);
		System.out.println(result);
	}
}