/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.serving.service.api.service.UnionServiceApi;
import com.welab.wefe.serving.service.api.service.UnionServiceApi.Input;
import com.welab.wefe.serving.service.api.service.UnionServiceApi.Output;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UnionServiceService {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * cache
     */
    protected static final ExpiringMap<String, Object> CACHE_MAP = ExpiringMap
            .builder()
            .expiration(60, TimeUnit.SECONDS)
            .maxSize(500)
            .build();

    public PagingOutput<Output> query(Input input) throws StatusCodeWithException {
        JSONObject result = query4Union(input);
        LOG.info("union query result = " + JSONObject.toJSONString(result));
        List<UnionServiceApi.Output> list = new ArrayList<>();
        if (result != null && result.getInteger("code") == 0) {
            JSONObject data = result.getJSONObject("data");
            JSONArray arr = data.getJSONArray("list");
            for (int i = 0; i < arr.size(); i++) {
                JSONObject item = arr.getJSONObject(i);
                int service_status = item.getIntValue("service_status");
                if (service_status != 1) {
                    continue;
                }
                UnionServiceApi.Output output = new UnionServiceApi.Output();
                output.setId(item.getString("service_id"));
                output.setName(item.getString("name"));
                output.setSupplierId(item.getString("member_id"));
                output.setSupplierName(item.getString("member_name"));
                output.setBaseUrl(item.getString("base_url"));
                output.setApiName(item.getString("api_name"));
                output.setCreatedTime(new Date(item.getLongValue("created_time")));
                output.setServiceType(item.getIntValue("service_type"));
                output.setUpdatedTime(new Date(item.getLongValue("updated_time")));
                if (StringUtils.isNotBlank(item.getString("query_params"))) {
                    if (output.getServiceType() == ServiceTypeEnum.PSI.getCode()) {
                        output.setKeyCalcRule(item.getString("query_params"));
                    } else {
                        output.setParams(Arrays.asList(item.getString("query_params").split(",")));
                    }
                }
                output.setMemberInfo(memberQuery(output.getSupplierId()));
                list.add(output);
            }
            return PagingOutput.of(data.getInteger("total"), list);
        }
        return PagingOutput.of(0, list);
    }

    public JSONObject query4Union(Input input) throws StatusCodeWithException {
        JObject params = JObject.create().append("pageSize", input.getPageSize()).append("pageIndex",
                input.getPageIndex());
        if (input.getServiceType() != -1) {
            params.append("serviceType", input.getServiceType());
        }
        if (StringUtils.isNotBlank(input.getMemberName())) {
            params.append("memberName", input.getMemberName());
        }
        if (StringUtils.isNotBlank(input.getServiceName())) {
            params.append("serviceName", input.getServiceName());
        }
        if (StringUtils.isNotBlank(input.getId())) {
            params.append("serviceId", input.getId());
        }
        LOG.info("union query params = " + JSONObject.toJSONString(params));
        return request("member/service/query", params);
    }

    public JSONObject add2Union(TableServiceMySqlModel model) throws StatusCodeWithException {
        JObject params = JObject.create().put("queryParams", model.getQueryParams())
                .put("serviceType", model.getServiceType()).put("memberId", CacheObjects.getMemberId())
                .append("baseUrl", CacheObjects.getServingBaseUrl())
                .append("apiName", ServiceService.SERVICE_PRE_URL + model.getUrl()).append("serviceId", model.getId())
                .append("name", model.getName()).append("serviceStatus", model.getStatus());
        LOG.info("union add2union params = " + JSONObject.toJSONString(params));
        JSONObject response = request("member/service/put", params);
        LOG.info("union add2union response = " + JSONObject.toJSONString(response));
        return response;
    }

    public JSONObject offline2Union(TableServiceMySqlModel model) throws StatusCodeWithException {
        JObject params = JObject.create().put("queryParams", model.getQueryParams())
                .put("serviceType", model.getServiceType()).put("memberId", CacheObjects.getMemberId())
                .append("baseUrl", CacheObjects.getServingBaseUrl())
                .append("apiName", ServiceService.SERVICE_PRE_URL + model.getUrl()).append("serviceId", model.getId())
                .append("name", model.getName()).append("serviceStatus", model.getStatus());
        LOG.info("union offline2Union params = " + JSONObject.toJSONString(params));
        JSONObject response = request("member/service/put", params);
        LOG.info("union offline2Union response = " + JSONObject.toJSONString(response));
        return response;
    }

    public JSONObject updateServingBaseUrlOnUnion(String servingBaseUrl) throws StatusCodeWithException {
        JObject params = JObject.create().put("servingBaseUrl", servingBaseUrl);
        LOG.info("union updateServingBaseUrlOnUnion params = " + JSONObject.toJSONString(params));
        JSONObject response = request("member/update_serving_base_url", params);
        LOG.info("union updateServingBaseUrlOnUnion response = " + JSONObject.toJSONString(response));
        return response;
    }

    public JSONObject memberQuery(String memberId) throws StatusCodeWithException {
        if (CACHE_MAP.containsKey(memberId)) {
            return JSONObject.parseObject(CACHE_MAP.get(memberId).toString());
        }
        JObject params = JObject.create().put("id", memberId);
        LOG.info("union member query params = " + JSONObject.toJSONString(params));
        JSONObject response = request("member/query", params);
        LOG.info("union member query response = " + JSONObject.toJSONString(response));
        if (response != null && response.getIntValue("code") == 0) {
            JSONArray list = response.getJSONObject("data").getJSONArray("list");
            if (list != null && !list.isEmpty()) {
                JSONObject jo = list.getJSONObject(0);
                JSONObject memberInfo = new JSONObject();
                memberInfo.put("name", jo.getString("name"));
                memberInfo.put("mobile", jo.getString("mobile"));
                memberInfo.put("email", jo.getString("email"));
                memberInfo.put("id", jo.getString("id"));
                memberInfo.put("serving_base_url", jo.getJSONObject("ext_json").getString("serving_base_url"));
                CACHE_MAP.put(memberId, memberInfo);
            }
        }
        return (JSONObject) CACHE_MAP.get(memberId);
    }

    private JSONObject request(String api, JSONObject params) throws StatusCodeWithException {
        if (StringUtils.isBlank(CacheObjects.getUnionBaseUrl()) || !CacheObjects.isUnionModel()) {
            return new JSONObject();
        }
        return request(api, params, true);
    }

    private JSONObject request(String api, JSONObject params, boolean needSign) throws StatusCodeWithException {
        params = new JSONObject(new TreeMap(params));
        String data = params.toJSONString();
        // rsa signature
        if (needSign) {
            String sign = null;
            try {
                //sign = RSAUtil.sign(data, CacheObjects.getRsaPrivateKey(), "UTF-8");
				sign = SignUtil.sign(data, CacheObjects.getRsaPrivateKey(), CacheObjects.getSecretKeyType());
            } catch (Exception e) {
                e.printStackTrace();
                throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, e.getMessage());
            }
            JSONObject body = new JSONObject();
            body.put("member_id", CacheObjects.getMemberId());
            body.put("sign", sign);
            body.put("data", data);
            data = body.toJSONString();
        }
        HttpResponse response = HttpRequest.create(CacheObjects.getUnionBaseUrl() + api).setBody(data).postJson();
        if (!response.success()) {
            throw new StatusCodeWithException(StatusCode.REMOTE_SERVICE_ERROR, response.getMessage());
        }
        JSONObject json;
        try {
            json = response.getBodyAsJson();
        } catch (JSONException e) {
            throw new StatusCodeWithException(
                    StatusCode.REMOTE_SERVICE_ERROR,
                    "union 响应失败：" + response.getBodyAsString());
        }
        if (json == null) {
            throw new StatusCodeWithException(
                    StatusCode.REMOTE_SERVICE_ERROR,
                    "union 响应失败：" + response.getBodyAsString());
        }
        Integer code = json.getInteger("code");
        if (code == null || !code.equals(0)) {
            if (code == 10031) {
                throw new StatusCodeWithException(StatusCode.PERMISSION_DENIED, "您尚未加入联邦：");
            }
            throw new StatusCodeWithException(
                    StatusCode.PERMISSION_DENIED,
                    "union 响应失败(" + code + ")：" + json.getString("message"));
        }
        return json;
    }
}
