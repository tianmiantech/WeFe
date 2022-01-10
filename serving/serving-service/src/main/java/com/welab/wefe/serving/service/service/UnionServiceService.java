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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Config config;

	public PagingOutput<Output> query(Input input) throws StatusCodeWithException {
//		JSONObject result = query4Union(input);
//		LOG.info("union query result = " + JSONObject.toJSONString(result));
		List<UnionServiceApi.Output> list = new ArrayList<>();
//		if (result.getInteger("code") == 0) {
//			JSONObject data = result.getJSONObject("data");
//			JSONArray arr = data.getJSONArray("list");
//			for (int i = 1; i <= arr.size(); i++) {
//				JSONObject item = arr.getJSONObject(i);
//
//				UnionServiceApi.Output output = new UnionServiceApi.Output();
//				output.setId(item.getString("service_id"));
//				output.setName(item.getString("name"));
//				output.setSupplierId(item.getString("member_id"));
//				output.setSupplierName(item.getString("member_name"));
//				output.setBaseUrl(item.getString("base_url"));
//				output.setApiName("api_name");
//				if (StringUtils.isNotBlank(item.getString("query_params"))) {
//					output.setParams(Arrays.asList(item.getString("query_params").split(",")));
//				}
//				output.setCreatedTime(new Date(item.getLongValue("created_time")));
//				output.setServiceType(item.getIntValue("service_type"));
//				list.add(output);
//			}
//			return PagingOutput.of(data.getInteger("total"), list);
//		}
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

	public JSONObject query4Union(Input input) throws StatusCodeWithException {
		JObject params = JObject.create().append("page_size", input.getPageSize()).append("page_index",
				input.getPageIndex());
		if (input.getServiceType() != -1) {
			params.append("service_type", input.getServiceType());
		}
		LOG.info("union query params = " + JSONObject.toJSONString(params));
		return request("member/service/query", params);
	}

	public JSONObject add2Union(ServiceMySqlModel model) throws StatusCodeWithException {
		JObject params = JObject.create().put("query_params", model.getQueryParams())
				.put("service_type", model.getServiceType()).put("member_id", CacheObjects.getMemberId())
				.append("base_url", config.getSERVING_BASE_URL()).append("api_name", "api/" + model.getUrl())
				.append("service_id", model.getId()).append("name", model.getName())
				.append("create_time", model.getCreatedTime()).append("service_status", model.getStatus());
		LOG.info("union add2union params = " + JSONObject.toJSONString(params));
		return request("member/service/put", params);
	}

	public JSONObject online2Union(ServiceMySqlModel model) throws StatusCodeWithException {
		String supplierId = CacheObjects.getMemberId();
		String serviceId = model.getId();
		JObject params = JObject.create().put("supplier_id", supplierId).put("service_id", serviceId)
				.put("service_status", model.getStatus());
		return request("member/service/update_service_status", params);
	}

	public JSONObject offline2Union(ServiceMySqlModel model) throws StatusCodeWithException {
		String supplierId = CacheObjects.getMemberId();
		String serviceId = model.getId();
		JObject params = JObject.create().put("supplier_id", supplierId).put("service_id", serviceId)
				.put("service_status", model.getStatus());
		return request("member/service/update_service_status", params);
	}

	private JSONObject request(String api, JSONObject params) throws StatusCodeWithException {
		return request(api, params, true);
	}

	private JSONObject request(String api, JSONObject params, boolean needSign) throws StatusCodeWithException {
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
