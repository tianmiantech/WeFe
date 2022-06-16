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

package com.welab.wefe.serving.service.predicter.single;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.serving.sdk.config.Config;
import com.welab.wefe.serving.sdk.dto.ProviderParams;
import com.welab.wefe.serving.service.api.serviceorder.SaveApi;
import com.welab.wefe.serving.service.database.entity.ServiceCallLogMysqlModel;
import com.welab.wefe.serving.service.enums.ServiceCallStatusEnum;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.ServiceCallLogService;
import com.welab.wefe.serving.service.service.ServiceOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeMap;

/**
 * Model call initiator
 *
 * @author hunter.zhao
 */
public class PromoterPredictHelper {

    protected static final Logger LOG = LoggerFactory.getLogger(PromoterPredictHelper.class);

    public static JObject callProviders(String modelId, String requestId, ProviderParams obj, String requestParam) throws StatusCodeWithException {

        HttpResponse response = null;
        ServiceOrderEnum orderStatus = ServiceOrderEnum.SUCCESS;
        try {
            check(obj);

            response = HttpRequest.create(obj.getApi())
                    .setBody(requestParam)
                    .setRetryCount(3)
                    .postJson();

            checkResponse(obj.getMemberId(), response);

            return extractData(response);
        } catch (StatusCodeWithException e) {
            //更新订单信息
            orderStatus = ServiceOrderEnum.FAILED;
            throw e;
        } finally {
            SaveApi.Input order = createOrder(modelId, obj.getMemberId(), orderStatus);
            callLog(modelId, requestId, obj.getMemberId(), order.getId(), requestParam, extractData(response), extractCode(response), extractResponseId(response));
        }
    }

    private static void check(ProviderParams obj) throws StatusCodeWithException {
        if (StringUtil.isEmpty(obj.getApi())) {
            LOG.error("未找到协作方的请求地址！");
            throw new StatusCodeWithException("未找到协作方预测地址！请配置" + CacheObjects.getPartnerName(obj.getMemberId()) + " 协作方地址后再尝试重试", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
        }
    }


    /**
     * Set request body(single)
     */
    protected static String buildFederatedPredictParam(String modelId, String requestId, String userId) throws StatusCodeWithException {

        /**
         * params
         * <p>predictParams will be sent to the provider. You need to be cautious about sensitive data</p>
         */
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("modelId", modelId);
        params.put("requestId", requestId);
        params.put("partnerCode", Config.MEMBER_ID);
        params.put("userId", userId);

        /**
         * Prevent map disorder, resulting in signature verification failure
         */
        String data = new JSONObject(params).toJSONString();

        /**
         * sign
         */
        String sign;
        try {
            sign = RSAUtil.sign(data, Config.RSA_PRIVATE_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        JSONObject body = new JSONObject();
        body.put("partnerCode", Config.MEMBER_ID);
        body.put("sign", sign);
        body.put("data", data);

        return body.toJSONString();
    }

    /**
     * Set request body(single)
     */
    public static String buildBatchFederatedPredictParam(String modelId, String requestId, List<String> userIds) throws StatusCodeWithException {

        /**
         * params
         * <p>predictParams will be sent to the provider. You need to be cautious about sensitive data</p>
         */
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("modelId", modelId);
        params.put("requestId", requestId);
        params.put("partnerCode", Config.MEMBER_ID);
        params.put("userIds", userIds);

        /**
         * Prevent map disorder, resulting in signature verification failure
         */
        String data = new JSONObject(params).toJSONString();

        /**
         * sign
         */
        String sign;
        try {
            sign = RSAUtil.sign(data, Config.RSA_PRIVATE_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        JSONObject body = new JSONObject();
        body.put("partnerCode", Config.MEMBER_ID);
        body.put("sign", sign);
        body.put("data", data);

        return body.toJSONString();
    }


    private static SaveApi.Input createOrder(String modelId, String partnerId, ServiceOrderEnum status) {
        SaveApi.Input order = new SaveApi.Input();
        order.setServiceId(modelId);
        order.setServiceName("");
        order.setServiceType(ServiceTypeEnum.MachineLearning.name());
        order.setRequestPartnerId(CacheObjects.getMemberId());
        order.setRequestPartnerName(CacheObjects.getMemberName());
        order.setResponsePartnerId(partnerId);
        order.setResponsePartnerName(CacheObjects.getPartnerName(partnerId));
        order.setOrderType(1);
        order.setStatus(status.getValue());

        ServiceOrderService serviceOrderService = Launcher.CONTEXT.getBean(ServiceOrderService.class);
        serviceOrderService.save(order);
        return order;
    }


    private static void callLog(String modelId, String requestId, String memberId, String orderId, String requestData, JObject result, Integer responseCode, String responseId) {
        ServiceCallLogMysqlModel callLog = new ServiceCallLogMysqlModel();
        callLog.setServiceType(ServiceTypeEnum.MachineLearning.name());
        callLog.setOrderId(orderId);
        callLog.setServiceId(modelId);
        //TODO 加服务名
        callLog.setServiceName("");
        callLog.setRequestData(requestData);
        callLog.setRequestPartnerId(CacheObjects.getMemberId());
        callLog.setRequestPartnerName(CacheObjects.getMemberName());
        callLog.setRequestId(requestId);
        callLog.setRequestIp("");
        callLog.setResponseCode(responseCode);
        callLog.setResponseId(responseId);
        callLog.setResponsePartnerId(memberId);
        callLog.setResponsePartnerName(CacheObjects.getPartnerName(memberId));
        callLog.setResponseData(result.toJSONString());
        callLog.setResponseStatus(getResponseStatus(result));
        callLog.setCallByMe(0);

        ServiceCallLogService serviceCallLogService = Launcher.CONTEXT.getBean(ServiceCallLogService.class);
        serviceCallLogService.save(callLog);
    }


    private static String getResponseStatus(JObject result) {
        return result == null ? ServiceCallStatusEnum.RESPONSE_ERROR.name() : ServiceCallStatusEnum.SUCCESS.name();
    }


    private static String extractResponseId(HttpResponse response) {
        if (response == null || !response.success() || response.getCode() != 200) {
            return "";
        }

        JSONObject json = response.getBodyAsJson();
        return json.getJSONObject("data").containsKey("response_id") ?
                json.getJSONObject("data").getString("response_id")
                : "";
    }


    private static JObject extractData(HttpResponse response) {
        if (response == null || !response.success() || response.getCode() != 200) {
            return JObject.create();
        }
        JSONObject json = response.getBodyAsJson();
        return JObject.create(json.getJSONObject("data").getJSONObject("data"));
    }

    private static void checkResponse(String memberId, HttpResponse response) throws StatusCodeWithException {
        if (response == null || !response.success() || response.getCode() != 200) {
            String message = "协作方 " + CacheObjects.getPartnerName(memberId) + " 响应失败(" + response.getCode() + ")," + response.getMessage();
            LOG.error(message);
            StatusCode.REMOTE_SERVICE_ERROR.throwException(message);
        }

        Integer code = extractCode(response);
        if (code == null || !code.equals(0) || !response.getBodyAsJson().containsKey("data")) {
            String message = "协作方 " + CacheObjects.getPartnerName(memberId) + " 响应失败(" + code + ")," + response.getBodyAsJson().getString("message");
            LOG.error(message);
            StatusCode.REMOTE_SERVICE_ERROR.throwException(message);
        }
    }

    private static Integer extractCode(HttpResponse response) {
        if (response == null || !response.success() || response.getCode() != 200) {
            return StatusCode.SYSTEM_ERROR.getCode();
        }
        JSONObject json = response.getBodyAsJson();
        return json.getInteger("code");
    }
}
