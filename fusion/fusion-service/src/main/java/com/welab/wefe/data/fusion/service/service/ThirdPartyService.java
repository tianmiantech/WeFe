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

package com.welab.wefe.data.fusion.service.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.data.fusion.service.database.entity.PartnerMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.TaskMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.TaskRepository;
import com.welab.wefe.data.fusion.service.enums.CallbackType;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.TreeMap;

/**
 * @author hunter.zhao
 */
@Service
public class ThirdPartyService {
    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;


    @Autowired
    private PartnerService partnerService;

    /**
     * Align the request
     */
    public void alignApply(TaskMySqlModel task) throws StatusCodeWithException {

        JObject params = JObject
                .create()
                .put("business_id", task.getBusinessId())
                .put("name", task.getName())
                .put("data_count", task.getDataCount())
                .put("psi_actuator_role", PSIActuatorRole.server.equals(task.getPsiActuatorRole()) ? PSIActuatorRole.client : PSIActuatorRole.server)
                .put("algorithm", task.getAlgorithm())
                .put("description", task.getDescription())
//                .put("providerDataSetId", task.getProviderDataSetId())
                ;

        //Find Partner information
        PartnerMySqlModel partner = partnerService.findByPartnerId(task.getPartnerId());
        if (partner == null) {
            throw new StatusCodeWithException("No partner information was found", StatusCode.DATA_NOT_FOUND);
        }

        request(partner.getBaseUrl(), "task/receive", params);
    }

    /**
     * psi-callback
     */
    public void callback(String url, String businessId, CallbackType callbackType, String socketIp, int sockerPort) throws StatusCodeWithException {

        JObject params = JObject
                .create()
                .put("business_id", businessId)
                .put("type", callbackType)
                .put("socket_ip", socketIp)
                .put("socket_port", sockerPort);

        request(url, "thirdparty/callback", params);
    }

    /**
     * psi-callback
     */
    public void callback(String url, String businessId, CallbackType callbackType, Integer dataCount) throws StatusCodeWithException {

        JObject params = JObject
                .create()
                .put("business_id", businessId)
                .put("type", callbackType)
                .put("data_count", dataCount);

        request(url, "thirdparty/callback", params);
    }


    private JSONObject request(String url, String api, JSONObject params) throws StatusCodeWithException {
        return request(url, api, params, true);
    }

    private JSONObject request(String url, String api, JSONObject params, boolean needSign) throws StatusCodeWithException {
        /**
         * Prevent the map from being out of order, which may cause the check failure
         */
        params = new JSONObject(new TreeMap(params));

        String data = params.toJSONString();

        // Rsa signature
        if (needSign) {

            String sign = null;
            try {
                sign = RSAUtil.sign(data, CacheObjects.getRsaPrivateKey());
            } catch (Exception e) {
                e.printStackTrace();
                throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
            }


            JSONObject body = new JSONObject();
            body.put("partner_id", CacheObjects.getPartnerId());
            body.put("sign", sign);
            body.put("data", data);

            data = body.toJSONString();
        }

        HttpResponse response = HttpRequest
                .create(url + "/" + api)
                .setBody(data)
                .postJson();

        if (!response.success()) {
            throw new StatusCodeWithException(response.getMessage(), StatusCode.RPC_ERROR);
        }

        JSONObject json = response.getBodyAsJson();
        Integer code = json.getInteger("code");
        if (code == null || !code.equals(0)) {
            throw new StatusCodeWithException("合作方信息 响应失败(" + code + ")：" + response.getMessage(), StatusCode.RPC_ERROR);
        }
        return json;
    }
}
