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

package com.welab.wefe.serving.sdk.algorithm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.ClassUtils;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.serving.sdk.config.Config;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.model.PredictModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeMap;

import static java.lang.Math.exp;

/**
 * @author Zane
 */
public abstract class AbstractAlgorithm<T, R> {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected T modelParam;

    /**
     * single sigmod function
     */
    protected PredictModel sigmod(PredictModel model) {
        model.setScore(1. / (1. + exp(-model.getScore())));
        return model;
    }

    /**
     * batch sigmod function
     */
    protected List<PredictModel> sigmod(List<PredictModel> models) {
        models.forEach(model ->
                model.setScore(1. / (1. + exp(-model.getScore())))
        );
        return models;
    }

    /**
     * A single prediction
     *
     * @param predictParams
     * @return predict result
     * @throws StatusCodeWithException
     */
    protected abstract R handle(PredictParams predictParams, List<JObject> federatedResult) throws StatusCodeWithException;

    public PredictResult execute(BaseModel model, PredictParams predictParams, List<JObject> federatedResult) throws StatusCodeWithException {

        // Convert the parameter list stored in the database into a well-defined entity object
        modelParam = (T) JSON.parseObject(model.params).toJavaObject(ClassUtils.getGenericClass(getClass(), 0));

        R value = handle(predictParams, federatedResult);

        return new PredictResult(model.getAlgorithm(), model.getFlType(), model.getMyRole(), value);
    }
//
//    /**
//     * Set request body
//     */
//    protected String setFederatedBatchPredictBody(FederatedParams federatedParams, List<String> userIds) throws StatusCodeWithException {
//
//        /**
//         * params
//         * <p>predictParams will be sent to the provider. You need to be cautious about sensitive data</p>
//         */
//        TreeMap<String, Object> params = new TreeMap<>();
////        params.put("seqNo", federatedParams.getSeqNo());
//        params.put("modelId", federatedParams.getModelId());
//        params.put("memberId", federatedParams.getMemberId());
//        params.put("isBatch", true);
//        params.put("predictParams", PredictParams.ofUserIds(userIds));
//
//        /**
//         * Prevent map disorder, resulting in signature verification failure
//         */
//        String data = new JSONObject(params).toJSONString();
//
//        /**
//         * sign
//         */
//        String sign;
//        try {
//            sign = RSAUtil.sign(data, Config.RSA_PRIVATE_KEY);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
//        }
//
//        JSONObject body = new JSONObject();
//        body.put("memberId", Config.MEMBER_ID);
//        body.put("sign", sign);
//        body.put("data", data);
//
//        return body.toJSONString();
//    }
}
