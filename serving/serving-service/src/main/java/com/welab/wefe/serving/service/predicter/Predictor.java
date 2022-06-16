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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.sdk.dto.BatchPredictParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.predicter.AbstractBasePredictor;
import com.welab.wefe.serving.service.database.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.dto.ServiceResultOutput;
import com.welab.wefe.serving.service.predicter.batch.BatchPromoterPredictor;
import com.welab.wefe.serving.service.predicter.batch.BatchProviderPredictor;
import com.welab.wefe.serving.service.predicter.single.DebugPromoterPredictor;
import com.welab.wefe.serving.service.predicter.single.DebugProviderPredictor;
import com.welab.wefe.serving.service.predicter.single.PromoterPredictor;
import com.welab.wefe.serving.service.predicter.single.ProviderPredictor;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.ModelMemberService;
import com.welab.wefe.serving.service.service.ModelService;
import com.welab.wefe.serving.service.service.ServiceOrderService;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;


/**
 * @author hunter.zhao
 */
public class Predictor {

    private static ModelMemberService modelMemberService;

    private static ModelService modelService;

    private static ServiceOrderService serviceOrderService;

    static {
        modelMemberService = Launcher.CONTEXT.getBean(ModelMemberService.class);
        modelService = Launcher.CONTEXT.getBean(ModelService.class);
        serviceOrderService = Launcher.CONTEXT.getBean(ServiceOrderService.class);
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


    public static PredictResult predict(String requestId,
                                        String modelId,
                                        PredictParams predictParams) throws StatusCodeWithException {

        AbstractBasePredictor predictor = constructPredictor(requestId, modelId, predictParams);
        return predictor.predict();
    }

    public static PredictResult batch(String requestId,
                                        String modelId,
                                        BatchPredictParams batchPredictParams) throws StatusCodeWithException {

        AbstractBasePredictor predictor = constructPredictor(requestId, modelId, batchPredictParams);
        return predictor.predict();
    }

    private static AbstractBasePredictor constructPredictor(String requestId, String modelId, PredictParams predictParams) throws StatusCodeWithException {
        JobMemberRole myRole = findMyRole(modelId);

        return myRole.equals(JobMemberRole.promoter) ?
                new PromoterPredictor(requestId, modelId, predictParams)
                : new ProviderPredictor(modelId, predictParams);
    }

    private static AbstractBasePredictor constructPredictor(String requestId, String modelId, BatchPredictParams batchPredictParams) throws StatusCodeWithException {
        JobMemberRole myRole = findMyRole(modelId);

        return myRole.equals(JobMemberRole.promoter) ?
                new BatchPromoterPredictor(requestId, modelId, batchPredictParams)
                : new BatchProviderPredictor(modelId, batchPredictParams);
    }

    private static JobMemberRole findMyRole(String modelId) throws StatusCodeWithException {
        List<ModelMemberMySqlModel> model = modelMemberService.findListByModelIdAndMemberId(modelId, CacheObjects.getMemberId());
        if (CollectionUtils.isEmpty(model)) {
            StatusCode.DATA_NOT_FOUND.throwException("未查找到模型数据！");
        }
        return model.get(0).getRole();
    }

//    /**
//     * Initiator batch call logic
//     */
//    public static PredictResult batchPromoterPredict(String modelId, String memberId, Map<String, Map<String, Object>> featureDataMap) throws Exception {
//
//        long start = System.currentTimeMillis();
//
//        String seqNo = "";
//        PredictResult result;
//
//        boolean requestResult = false;
//        PredictParams predictParams = PredictParams.of(featureDataMap);
//
//        try {
//            //Generation predicter
//            AbstractBasePredictor promoterPredict = new BatchPredicter()
//                    .setPredictParams(predictParams)
//                    .setFederatedParams(FederatedParams.of(modelId, CacheObjects.getMemberId(), findProviders(modelId)));
//
//            //start predict
//            result = promoterPredict.predict();
//
//            //Call succeeded
//            requestResult = true;
//
//        } finally {
////            log(seqNo, modelId, memberId, predictParams, null, System.currentTimeMillis() - start, requestResult);
//        }
//
//        return result;
//    }

    /**
     * provider batch call logic
     */
//    public static PredictResult batchProviderPredict(String seqNo,
//                                                     String modelId,
//                                                     String memberId,
//                                                     PredictParams predictParams) throws Exception {
//
//
//        long start = System.currentTimeMillis();
//
//        PredictResult result;
//
//        boolean requestResult = false;
//
//        try {
//            //Generate predicter
//            FederatedParams federatedParams = FederatedParams.of(modelId, memberId);
//            AbstractBasePredictor providerPredicter = new BatchPredicter()
//                    .setFederatedParams(federatedParams)
//                    .setPredictParams(predictParams);
//
//            //Start prediction
//            result = providerPredicter.predict();
//
//            //Call succeeded
//            requestResult = true;
//
//        } finally {
////            log(seqNo, modelId, memberId, predictParams, null, System.currentTimeMillis() - start, requestResult);
//        }
//
//        return result;
//    }

    /**
     * predict Interface
     *
     * @param modelId model id
     */
    public static PredictResult debug(String modelId,
                                      PredictParams predictParams,
                                      PredictFeatureDataSource featureSource,
                                      JSONObject extendParams) throws Exception {

        AbstractBasePredictor predictor = constructDebugPredictor(modelId, predictParams, featureSource, extendParams);
        return predictor.predict();
    }

    private static AbstractBasePredictor constructDebugPredictor(String modelId,
                                                                 PredictParams predictParams,
                                                                 PredictFeatureDataSource featureDataSource,
                                                                 JSONObject extendParams) throws StatusCodeWithException {
        JobMemberRole myRole = findMyRole(modelId);

        return myRole.equals(JobMemberRole.promoter) ?
                new DebugPromoterPredictor(ServiceResultOutput.buildId(), modelId, predictParams, featureDataSource, extendParams)
                : new DebugProviderPredictor(modelId, predictParams, featureDataSource, extendParams);
    }
}
