/*
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

package com.welab.wefe.board.service.service.fusion;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.project.fusion.task.AuditCallbackApi;
import com.welab.wefe.board.service.api.project.fusion.task.ReceiveApi;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.fusion.core.enums.CallbackType;
import com.welab.wefe.fusion.core.enums.PSIActuatorRole;
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
                .put("project_id", task.getProjectId())
                .put("dst_member_id", CacheObjects.getMemberId())
                .put("business_id", task.getBusinessId())
                .put("name", task.getName())
                .put("psi_actuator_role", PSIActuatorRole.server.equals(task.getPsiActuatorRole()) ? PSIActuatorRole.client : PSIActuatorRole.server)
                .put("algorithm", task.getAlgorithm())
                .put("description", task.getDescription())
                .put("data_resource_id", task.getPartnerDataResourceId())
                .put("data_resource_type", task.getPartnerDataResourceType())
                .put("partner_data_resource_id", task.getDataResourceId())
                .put("partner_data_resource_type", task.getDataResourceType())
                .put("partner_row_count", task.getRowCount())
                .put("partner_hash_function", task.getHashFunction());

        request(task.getDstMemberId(), ReceiveApi.class, params);
    }

    /**
     * psi-callback
     */
    public void callback(String dstMemberId, String businessId, AuditStatus auditStatus, String auditComment) throws StatusCodeWithException {
        callback(dstMemberId, businessId, auditStatus, auditComment, null);
    }

    /**
     * psi-callback
     */
    public void callback(String dstMemberId, String businessId, AuditStatus auditStatus, String auditComment, String hashFunction) throws StatusCodeWithException {

        JObject params = JObject
                .create()
                .put("business_id", businessId)
                .put("audit_status", auditStatus)
                .put("audit_comment", auditComment)
                .put("partner_hash_function", hashFunction);

        request(dstMemberId, AuditCallbackApi.class, params);
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

    private JSONObject request(String dstMemberId, Class<?> api, JSONObject params) throws StatusCodeWithException {
        /**
         * Prevent the map from being out of order, which may cause the check failure
         */
        return gatewayService.callOtherMemberBoard(dstMemberId, api, params, JSONObject.class);

//        HttpResponse result = HttpRequest.create("http://172.29.25.148:8080/board-service/fusion/audit/callback").appendParameters(params).postJson();

//        if (!result.success()) {
//            throw new StatusCodeWithException(result.getMessage(), StatusCode.RPC_ERROR);
//        }

//        JSONObject json = JObject.create(result.getBodyAsJson());
//        return json;
    }
}
