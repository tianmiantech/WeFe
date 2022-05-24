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
import com.welab.wefe.serving.sdk.config.Config;
import com.welab.wefe.serving.sdk.dto.ProviderParams;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.predicter.single.AbstractSinglePromoterPredictor;
import com.welab.wefe.serving.service.manager.FeatureManager;
import com.welab.wefe.serving.service.manager.ModelManager;
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

    private String responseId;

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

        if (CollectionUtils.isEmpty(federatedParams.getProviders())) {
            LOG.error("未找到纵向联邦的协作方！");
            throw new StatusCodeWithException("未找到纵向联邦的协作方！", StatusCode.DATA_NOT_FOUND);
        }

        List<JObject> federatedResult = new ArrayList<>();

        for (ProviderParams obj : federatedParams.getProviders()) {

            //TODO 生成callLog

            if (StringUtil.isEmpty(obj.getApi())) {
                LOG.error("未找到协作方的请求地址！");
                throw new StatusCodeWithException("未找到协作方预测地址！请配置" + obj.getMemberId() + " 协作方地址后再尝试重试", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
            }

            HttpResponse response = HttpRequest.create(obj.getApi() == null ? "" : obj.getApi())
                    .setBody(buildFederatedPredictParam())
                    .setRetryCount(3)
                    .postJson();

            if (!response.success()) {
                LOG.error("请求协作方失败: {}" + response.getBodyAsJson().getString("message"));
                throw new StatusCodeWithException(response.getMessage(), StatusCode.REMOTE_SERVICE_ERROR);
            }

            JSONObject json = response.getBodyAsJson();
            Integer code = json.getInteger("code");
            if (code == null || !code.equals(0) || !json.containsKey("data")) {
                LOG.error("协作方响应失败({}),{}", code, json.getString("message"));
                throw new StatusCodeWithException("协作方" + obj.getMemberId() + "响应失败," + json.getString("message"), StatusCode.REMOTE_SERVICE_ERROR);
            }

            JObject resultData = JObject.create(json.getJSONObject("data"));

            federatedResult.add(resultData);

            //TODO 更新callLog
        }

        return federatedResult;
    }


    @Override
    public Map<String, Object> findFeatureData() throws StatusCodeWithException {
        return FeatureManager.getFeatureData(modelId, predictParams);
    }

    @Override
    public List<ProviderParams> findProviders() {
        return null;
    }
}
