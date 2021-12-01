/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.service.fusion;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.fusion.task.ReceiveApi;
import com.welab.wefe.board.service.api.member.ServiceStatusCheckApi;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.database.repository.fusion.FusionTaskRepository;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.fusion.core.enums.CallbackType;
import com.welab.wefe.fusion.core.enums.PSIActuatorRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.TreeMap;

/**
 * @author hunter.zhao
 */
@Service
public class ThirdPartyService {
    @Autowired
    private FusionTaskService fusionService;

    @Autowired
    private FusionTaskRepository fusionTaskRepository;

    @Autowired
    private GatewayService gatewayService;

    /**
     * Align the request
     */
    public void alignApply(FusionTaskMySqlModel task) throws StatusCodeWithException {

        JObject params = JObject
                .create()
                .put("project_id", task.getBusinessId())
                .put("business_id", task.getBusinessId())
                .put("name", task.getName())
                .put("row_count", task.getRowCount())
                .put("psi_actuator_role", PSIActuatorRole.server.equals(task.getPsiActuatorRole()) ? PSIActuatorRole.client : PSIActuatorRole.server)
                .put("algorithm", task.getAlgorithm())
                .put("description", task.getDescription())
                .put("data_resource_id", task.getPartnerDataResourceId())
                .put("data_resource_type", task.getPartnerDataResourceType())
                .put("partner_data_resource_id", task.getDataResourceId())
                .put("partner_data_resource_type", task.getDataResourceType())
//                .put("providerDataSetId", task.getProviderDataSetId())
                ;

        //Find Partner information
        //PartnerMySqlModel partner = fusionTaskRepository.findByPartnerId(task.getPartnerId());
//        if (partner == null) {
//            throw new StatusCodeWithException("No partner information was found", StatusCode.DATA_NOT_FOUND);
//        }
//
        request("task/receive", params);
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

       // request(url, "thirdparty/callback", params);
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

      // request(url, "thirdparty/callback", params);
    }


    private JSONObject request(String api, JSONObject params) throws StatusCodeWithException {
        return request(api, params, true);
    }

    private JSONObject request(String api, JSONObject params, boolean needSign) throws StatusCodeWithException {
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
            body.put("member_id", CacheObjects.getMemberId());
            body.put("sign", sign);
            body.put("data", data);

            data = body.toJSONString();
        }


        ApiResult<JSONObject> test=  gatewayService.callOtherMemberBoard("8896e74890a5459386287ec817e8b4f3", api, JObject.create(data));

        if (!test.success()) {
            throw new StatusCodeWithException(test.getMessage(), StatusCode.RPC_ERROR);
        }

        JSONObject json = JObject.create(test.data);
        Integer code = json.getInteger("code");
        if (code == null || !code.equals(0)) {
            throw new StatusCodeWithException("合作方信息 响应失败(" + code + ")：" + test.getMessage(), StatusCode.RPC_ERROR);
        }
        return json;
    }
}
