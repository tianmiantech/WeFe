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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.AuditStatus;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.fusion.core.enums.CallbackType;
import com.welab.wefe.fusion.core.enums.PSIActuatorRole;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hunter.zhao
 */
@Service
public class ThirdPartyService {
    @Autowired
    private GatewayService gatewayService;

    /**
     * Align the request
     */
    public void alignApply(FusionTaskMySqlModel task) throws StatusCodeWithException {

        JObject params = JObject
                .create()
                .put("project_id", task.getBusinessId())
                .put("dst_member_id", CacheObjects.getMemberId())
                .put("business_id", task.getBusinessId())
                .put("name", task.getName())
                .put("row_count", task.getRowCount())
                .put("psi_actuator_role", PSIActuatorRole.server.equals(task.getPsiActuatorRole()) ? PSIActuatorRole.client : PSIActuatorRole.server)
                .put("algorithm", task.getAlgorithm())
                .put("description", task.getDescription())
                .put("data_resource_id", task.getPartnerDataResourceId())
                .put("data_resource_type", task.getPartnerDataResourceType())
                .put("partner_data_resource_id", task.getDataResourceId())
                .put("partner_data_resource_type", task.getDataResourceType());

        request(task.getDstMemberId(), "task/receive", params);
    }

    /**
     * psi-callback
     */
    public void callback(String dstMemberId, String businessId, AuditStatus auditStatus, String auditComment) throws StatusCodeWithException {

        JObject params = JObject
                .create()
                .put("business_id", businessId)
                .put("project_id", "test")
                .put("audit_status", auditStatus)
                .put("audit_comment", auditComment);

        request(dstMemberId, "fusion/audit/callback", params);
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


    private JSONObject request(String dstMemberId, String api, JSONObject params) throws StatusCodeWithException {
        return request(dstMemberId, api, params, true);
    }

    private JSONObject request(String dstMemberId, String api, JSONObject params, boolean needSign) throws StatusCodeWithException {
        /**
         * Prevent the map from being out of order, which may cause the check failure
         */
        ApiResult<JSONObject> result = gatewayService.callOtherMemberBoard(dstMemberId, api, params);

        if (!result.success()) {
            throw new StatusCodeWithException(result.getMessage(), StatusCode.RPC_ERROR);
        }

        Log.info("result is {}", JSON.toJSONString(result));

        return result.data;

//        HttpResponse result = HttpRequest.create("http://172.29.25.148:8080/board-service/fusion/audit/callback").appendParameters(params).postJson();

//        if (!result.success()) {
//            throw new StatusCodeWithException(result.getMessage(), StatusCode.RPC_ERROR);
//        }

//        JSONObject json = JObject.create(result.getBodyAsJson());
//        return json;
    }
}
