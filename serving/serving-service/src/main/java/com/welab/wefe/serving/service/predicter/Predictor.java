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

package com.welab.wefe.serving.service.predicter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.dto.ProviderParams;
import com.welab.wefe.serving.sdk.predicter.AbstractBasePredictor;
import com.welab.wefe.serving.service.database.entity.ModelMySqlModel;
import com.welab.wefe.serving.service.predicter.batch.BatchPromoterPredicter;
import com.welab.wefe.serving.service.predicter.batch.BatchProviderPredicter;
import com.welab.wefe.serving.service.predicter.single.DebugPredicter;
import com.welab.wefe.serving.service.predicter.single.PromoterPredictor;
import com.welab.wefe.serving.service.predicter.single.ProviderPredictor;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.ModelMemberService;
import com.welab.wefe.serving.service.service.ModelService;
import com.welab.wefe.serving.service.service.PredictLogService;

import java.util.List;
import java.util.Map;


/**
 * @author Zane
 */
public class Predictor {

    private static PredictLogService predictLogService;

    private static ModelMemberService modelMemberService;

    private static ModelService modelService;

    static {
        predictLogService = Launcher.CONTEXT.getBean(PredictLogService.class);
        modelMemberService = Launcher.CONTEXT.getBean(ModelMemberService.class);
        modelService = Launcher.CONTEXT.getBean(ModelService.class);
    }

    /**
     * It can only be instantiated once, and a single instance can be maintained globally.
     */
    static {
        try {
            com.welab.wefe.serving.sdk.config.Launcher.init(
                    CacheObjects.getMemberId(),
                    CacheObjects.getRsaPrivateKey(),
                    CacheObjects.getRsaPublicKey()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static PredictResult predict(String modelId, PredictParams predictParams, FederatedParams federatedParams) throws Exception {

        long start = System.currentTimeMillis();
        PredictResult result;

        try {
            AbstractBasePredictor predictor = constructPredictor(modelId, predictParams, federatedParams);
            result = predictor.predict();

        } finally {
            //TODO 加日志
//            log(seqNo, modelId, memberId, predictParams, null, System.currentTimeMillis() - start, requestResult);
        }

        return result;
    }

    private static AbstractBasePredictor constructPredictor(String modelId, PredictParams predictParams, FederatedParams federatedParams) {
        ModelMySqlModel model = modelService.findOne(modelId);
        if (model.isEnable()) {
            return new PromoterPredictor()
                    .setPredictParams(predictParams)
                    .setFederatedParams(federatedParams.setRequestId("").setProviders(findProviders(modelId)))
                    .setModelId(modelId);
        } else {
            return new ProviderPredictor()
                    .setPredictParams(predictParams)
                    .setFederatedParams(federatedParams.setResponseId(""))
                    .setModelId(modelId);
        }
    }

    /**
     * Initiator batch call logic
     */
    public static PredictResult batchPromoterPredict(String modelId, String memberId, Map<String, Map<String, Object>> featureDataMap) throws Exception {

        long start = System.currentTimeMillis();

        String seqNo = "";
        PredictResult result;

        boolean requestResult = false;
        PredictParams predictParams = PredictParams.of(featureDataMap);

        try {
            //Generation predicter
            AbstractBasePredictor promoterPredict = new BatchPromoterPredicter()
                    .setPredictParams(predictParams)
                    .setFederatedParams(FederatedParams.of("", modelId, CacheObjects.getMemberId(), findProviders(modelId)));

            //start predict
            result = promoterPredict.predict();

            //Call succeeded
            requestResult = true;

        } finally {
            log(seqNo, modelId, memberId, predictParams, null, System.currentTimeMillis() - start, requestResult);
        }

        return result;
    }

    /**
     * provider batch call logic
     */
    public static PredictResult batchProviderPredict(String seqNo,
                                                     String modelId,
                                                     String memberId,
                                                     PredictParams predictParams) throws Exception {


        long start = System.currentTimeMillis();

        PredictResult result;

        boolean requestResult = false;

        try {
            //Generate predicter
            FederatedParams federatedParams = FederatedParams.of(seqNo, modelId, memberId);
            AbstractBasePredictor providerPredicter = new BatchProviderPredicter()
                    .setFederatedParams(federatedParams)
                    .setPredictParams(predictParams);

            //Start prediction
            result = providerPredicter.predict();

            //Call succeeded
            requestResult = true;

        } finally {
            log(seqNo, modelId, memberId, predictParams, null, System.currentTimeMillis() - start, requestResult);
        }

        return result;
    }

    /**
     * promoter batch call logic
     */
//    public static PredictResult promoter(String modelId,
//                                         String userId,
//                                         Map<String, Object> featureData,
//                                         JSONObject params) throws Exception {
//
//        long start = System.currentTimeMillis();
//
//        String seqNo = "", memberId = "";
//        PredictResult result = null;
//
//        PredictParams predictParams = PredictParams.of(userId, featureData);
//        boolean requestResult = false;
//
//        try {
//
//            //Generate predicter
//            PromoterPredictor promoter = new PromoterPredictor(modelId, predictParams, params, findProviders(modelId), CacheObjects.getMemberId());
//            seqNo = promoter.seqNo;
//            memberId = promoter.memberId;
//
//            //start predict
//            result = promoter.predict();
//
//
//            //Call succeeded
//            requestResult = true;
//
//        } finally {
//            log(seqNo, modelId, memberId, userId, featureData, params, result, System.currentTimeMillis() - start, requestResult);
//        }
//
//        return result;
//    }


    /**
     * provider predict
     */
//    public static PredictResult provider(String seqNo,
//                                         String modelId,
//                                         String memberId,
//                                         String userId,
//                                         Map<String, Object> featureData,
//                                         JSONObject params) throws Exception {
//
//        long start = System.currentTimeMillis();
//
//        PredictResult result = null;
//        PredictParams predictParams = PredictParams.of(userId, featureData);
//        FederatedParams federatedParams = FederatedParams.of(seqNo, modelId, memberId);
//        boolean requestResult = false;
//
//        try {
//
//            ProviderPredictor provider = new ProviderPredictor(federatedParams, predictParams, params);
//            result = provider.predict();
//
//            //Call succeeded
//            requestResult = true;
//
//        } finally {
//            log(seqNo, modelId, memberId, userId, featureData, params, result, System.currentTimeMillis() - start, requestResult);
//        }
//
//        return result;
//    }

    /**
     * predict Interface
     *
     * @param modelId     model id
     * @param userId      Predict the ID of the matching sample, such as device number or mobile phone number
     * @param featureData Characteristic data
     * @param params      Additional parameters
     */
    public static PredictResult debug(String modelId,
                                      String userId,
                                      Map<String, Object> featureData,
                                      JSONObject params,
                                      PredictFeatureDataSource featureSource,
                                      JobMemberRole myRole) throws Exception {

        long start = System.currentTimeMillis();
        String seqNo = "", memberId = "";
        PredictResult result = null;

        PredictParams predictParams = PredictParams.of(userId, featureData);
        FederatedParams federatedParams = FederatedParams.of(seqNo, modelId, memberId, findProviders(modelId));
        boolean requestResult = false;

        try {
            AbstractBasePredictor debug = new DebugPredicter()
                    .setFeatureSource(featureSource)
                    .setMyRole(myRole)
                    .setPredictParams(predictParams)
                    .setFederatedParams(federatedParams)
                    .setModelId(modelId);

            result = debug.predict();


            //Call succeeded
//            requestResult = true;

        } finally {
//            log(seqNo, modelId, memberId, userId, featureData, params, result, System.currentTimeMillis() - start, requestResult);
        }

        return result;
    }


    private static void log(String seqNo,
                            String modelId,
                            String memberId,
                            String userId,
                            Map<String, Object> featureData,
                            JSONObject params,
                            PredictResult result,
                            long spend,
                            boolean requestResult) {

        JSONObject request = new JSONObject();
        request.put("seqNo", seqNo);
        request.put("modelId", modelId);
        request.put("memberId", memberId);
        request.put("userId", userId);
        request.put("featureData", featureData);
        request.put("params", params);

        //Call record warehousing
        predictLogService.save(
                seqNo,
                modelId,
                memberId,
                result == null ? null : result.getAlgorithm(),
                result == null ? null : result.getType(),
                result == null ? null : result.getMyRole(),
                JSON.toJSONString(request),
                JSON.toJSONString(result),
                spend,
                requestResult
        );
    }

    private static void log(String seqNo,
                            String modelId,
                            String memberId,
                            PredictParams predictParams,
                            PredictResult result,
                            long spend,
                            boolean requestResult) {

        JSONObject request = new JSONObject();
        request.put("seqNo", seqNo);
        request.put("modelId", modelId);
        request.put("memberId", memberId);
        request.put("predictParams", predictParams);

        //Call record warehousing
        predictLogService.save(
                seqNo,
                modelId,
                memberId,
                result == null ? null : result.getAlgorithm(),
                result == null ? null : result.getType(),
                result == null ? null : result.getMyRole(),
                JSON.toJSONString(request),
                JSON.toJSONString(result),
                spend,
                requestResult
        );
    }

    /**
     * Get partner information
     */
    private static List<ProviderParams> findProviders(String modelId) {
        return modelMemberService.findProviders(modelId);
    }
}
