/**
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

package com.welab.wefe.serving.service.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.serving.service.api.service.UnionServiceApi;
import com.welab.wefe.serving.service.api.service.UnionServiceApi.Input;
import com.welab.wefe.serving.service.api.service.UnionServiceApi.Output;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.serving.entity.ServiceMySqlModel;
import com.welab.wefe.serving.service.dto.PagingOutput;

@Service
public class UnionServiceService {

	@Autowired
	private Config config;

	public PagingOutput<Output> query(Input input) {
		if (input.getServiceType() != -1) {
			// TODO
		}

		List<UnionServiceApi.Output> list = new ArrayList<>();

		// mock
		int size = 2;
		for (int i = 1; i <= size; i++) {
			UnionServiceApi.Output output = new UnionServiceApi.Output();
			output.setId("" + i);
			output.setName("信用卡数量查询");
			output.setSupplierId("06198105b8c647289177cf057a15bdb" + i);
			output.setSupplierName("鹏元");
			output.setBaseUrl("http://xbd-dev.wolaidai.com/serving-service-0" + i + "/");
			output.setApiName("api/query/credit_card_count");
			output.setParams(Arrays.asList("member", "model"));
			output.setCreatedTime(new Date());
			output.setServiceType(3);
			list.add(output);
		}
		return PagingOutput.of(2, list);
	}

	public JSONObject query4Union() throws StatusCodeWithException {
		JObject params = JObject.create();
		return request("member/service/query", params);
	}

	public JSONObject add2Union(ServiceMySqlModel model) throws StatusCodeWithException {
		model.getId();
		model.getName();
		String supplierId = "";
		String supplierName = "";
		String baseUrl = config.getSERVING_BASE_URL();
		String apiName = "api/" + model.getUrl();
		List<String> userParams = Arrays.asList(model.getQueryParams().split(","));
		model.getCreatedTime();
		model.getServiceType();
		// TODO publish
		JObject params = JObject.create().put("model_id", model.getId()).put("model_name", model.getName())
				.put("supplier_id", supplierId);
		return request("member/service/add", params);
	}

	public JSONObject publish2Union(ServiceMySqlModel model) throws StatusCodeWithException {
		model.getId();
		model.getName();
		String supplierId = "";
		String supplierName = "";
		String baseUrl = config.getSERVING_BASE_URL();
		String apiName = "api/" + model.getUrl();
		List<String> userParams = Arrays.asList(model.getQueryParams().split(","));
		model.getCreatedTime();
		model.getServiceType();
		// TODO publish
		JObject params = JObject.create().put("model_id", model.getId()).put("model_name", model.getName())
				.put("supplier_id", supplierId);
		return request("member/service/update_service_status", params);
	}

	public JSONObject offline2Union(ServiceMySqlModel model) throws StatusCodeWithException {
		String supplierId = "";
		// TODO offline
		JObject params = JObject.create().put("model_id", model.getId()).put("model_name", model.getName())
				.put("supplier_id", supplierId);
		return request("member/service/update_service_status", params);
	}

	private JSONObject request(String api, JSONObject params) throws StatusCodeWithException {
		return request(api, params, true);
	}

	private JSONObject request(String api, JSONObject params, boolean needSign) throws StatusCodeWithException {
		/**
		 * Prevent the map from being out of order, causing the verification to fail.
		 */
		params = new JSONObject(new TreeMap(params));
		String data = params.toJSONString();
		// rsa signature
		if (needSign) {
			String sign = null;
			try {
				sign = RSAUtil.sign(data, CacheObjects.getRsaPrivateKey(), "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
				throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
			}
			JSONObject body = new JSONObject();
			body.put("member_id", CacheObjects.getMemberId());
			body.put("sign", sign);
			body.put("data", data);
			data = body.toJSONString();
		}
		HttpResponse response = HttpRequest.create(config.getUNION_BASE_URL() + "/" + api).setBody(data).postJson();
		if (!response.success()) {
			throw new StatusCodeWithException(response.getMessage(), StatusCode.RPC_ERROR);
		}
		JSONObject json;
		try {
			json = response.getBodyAsJson();
		} catch (JSONException e) {
			throw new StatusCodeWithException("union 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
		}
		if (json == null) {
			throw new StatusCodeWithException("union 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
		}
		Integer code = json.getInteger("code");
		if (code == null || !code.equals(0)) {
			throw new StatusCodeWithException("union 响应失败(" + code + ")：" + json.getString("message"),
					StatusCode.RPC_ERROR);
		}
		return json;
	}
}
