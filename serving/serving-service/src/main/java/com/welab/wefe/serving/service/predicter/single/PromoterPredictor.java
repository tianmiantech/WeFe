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
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.ProviderParams;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.predicter.single.AbstractSinglePromoterPredictor;
import com.welab.wefe.serving.service.api.serviceorder.SaveApi;
import com.welab.wefe.serving.service.database.entity.ServiceCallLogMysqlModel;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.manager.FeatureManager;
import com.welab.wefe.serving.service.manager.ModelManager;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.ModelMemberService;
import com.welab.wefe.serving.service.service.ServiceCallLogService;
import com.welab.wefe.serving.service.service.ServiceOrderService;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Model call initiator
 *
 * @author hunter.zhao
 */
public class PromoterPredictor extends AbstractSinglePromoterPredictor {

    private String requestId;

    public PromoterPredictor(String requestId,
                             String modelId,
                             PredictParams predictParams,
                             FederatedParams federatedParams) {
        super(modelId, predictParams, federatedParams);
        this.requestId = requestId;
    }

    @Override
    public BaseModel getModel() throws StatusCodeWithException {
        return ModelManager.getModelParam(modelId);
    }

    /**
     * Set request body(single)
     */
    protected String buildFederatedPredictParam() throws StatusCodeWithException {

        /**
         * params
         * <p>predictParams will be sent to the provider. You need to be cautious about sensitive data</p>
         */
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("modelId", federatedParams.getModelId());
        params.put("requestId", requestId);
        params.put("memberId", federatedParams.getMemberId());
        params.put("userId", predictParams.getUserId());

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
        body.put("memberId", Config.MEMBER_ID);
        body.put("sign", sign);
        body.put("data", data);

        return body.toJSONString();
    }

    @Override
    public List<JObject> federatedResultByProviders() throws StatusCodeWithException {

        if (CollectionUtils.isEmpty(findProviders())) {
            LOG.error("未找到纵向联邦的协作方！");
            throw new StatusCodeWithException("未找到纵向联邦的协作方！", StatusCode.DATA_NOT_FOUND);
        }

        List<JObject> federatedResult = new ArrayList<>();
        for (ProviderParams provider : findProviders()) {
            federatedResult.add(callProviders(provider));
        }

        return federatedResult;
    }

    private JObject callProviders(ProviderParams obj) throws StatusCodeWithException {

        HttpResponse response = null;
        SaveApi.Input order = null;
        try {
            if (StringUtil.isEmpty(obj.getApi())) {
                LOG.error("未找到协作方的请求地址！");
                throw new StatusCodeWithException("未找到协作方预测地址！请配置" + obj.getMemberId() + " 协作方地址后再尝试重试", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
            }

            order = createOrder(obj.getMemberId());

            response = HttpRequest.create(obj.getApi())
                    .setBody(buildFederatedPredictParam())
                    .setRetryCount(3)
                    .postJson();

            responseCheck(obj.getMemberId(), response);

            return extractData(response);
        } finally {
            callLog(order.getId(), buildFederatedPredictParam(), extractData(response), extractCode(response), extractResponseId(response));
        }
    }

    private JObject extractData(HttpResponse response) {
        if (response == null || response.getBodyAsJson() == null) {
            return JObject.create();
        }
        JSONObject json = response.getBodyAsJson();
        return JObject.create(json.getJSONObject("data"));
    }

    private void responseCheck(String memberId, HttpResponse response) throws StatusCodeWithException {
        JSONObject json = response.getBodyAsJson();
        Integer code = extractCode(response);
        if (!response.success() || code == null || !code.equals(0) || !json.containsKey("data")) {
            LOG.error("协作方响应失败({}),{}", code, json.getString("message"));
            throw new StatusCodeWithException("协作方" + memberId + "响应失败," + json.getString("message"), StatusCode.REMOTE_SERVICE_ERROR);
        }
    }

    private Integer extractCode(HttpResponse response) {
        if (response == null || response.getBodyAsJson() == null) {
            return StatusCode.SYSTEM_ERROR.getCode();
        }
        JSONObject json = response.getBodyAsJson();
        return json.getInteger("code");
    }

    private String extractResponseId(HttpResponse response) {
        if (response == null || response.getBodyAsJson() == null) {
            return "";
        }
        JSONObject json = response.getBodyAsJson();
        return json.getJSONObject("data").getString("responseId");
    }

    @Override
    public Map<String, Object> findFeatureData() throws StatusCodeWithException {
        return FeatureManager.getFeatureData(modelId, predictParams);
    }


    /**
     * Get partner information
     */
    private List<ProviderParams> findProviders() {
        ModelMemberService modelMemberService = Launcher.CONTEXT.getBean(ModelMemberService.class);
        return modelMemberService.findProviders(modelId);
    }


    private void callLog(String orderId, String requestData, JObject result, Integer responseCode, String responseId) {
        ServiceCallLogMysqlModel callLog = new ServiceCallLogMysqlModel();
        callLog.setServiceType(ServiceTypeEnum.MachineLearning.name());
        callLog.setOrderId(orderId);
        callLog.setServiceId(modelId);
        callLog.setRequestData(requestData);
        callLog.setRequestPartnerId(CacheObjects.getMemberId());
        callLog.setRequestId(requestId);
//        callLog.setRequestIp(ServiceUtil.getIpAddr(input.request));
        callLog.setCallByMe(0);
        callLog.setResponseCode(responseCode);
        callLog.setResponseId(responseId);
        callLog.setResponseData(result.toJSONString());

        ServiceCallLogService serviceCallLogService = Launcher.CONTEXT.getBean(ServiceCallLogService.class);
        serviceCallLogService.save(callLog);
    }


    private SaveApi.Input createOrder(String partnerId) {
        SaveApi.Input order = new SaveApi.Input();
        order.setServiceId(modelId);
        order.setServiceName("");
        order.setServiceType(ServiceTypeEnum.MachineLearning.name());
        order.setRequestPartnerId(CacheObjects.getMemberId());
        order.setRequestPartnerName(CacheObjects.getMemberName());
        order.setResponsePartnerId(partnerId);
        order.setRequestPartnerName(CacheObjects.getPartnerName(partnerId));
//        order.setOrderType();

        ServiceOrderService serviceOrderService = Launcher.CONTEXT.getBean(ServiceOrderService.class);
        serviceOrderService.save(order);
        return order;
    }
}
