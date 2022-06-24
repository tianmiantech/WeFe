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
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;


/**
 * @author hunter.zhao
 */
public class Predictor {

    private static ModelMemberService modelMemberService;

    static {
        modelMemberService = Launcher.CONTEXT.getBean(ModelMemberService.class);
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
                                        String userId,
                                        Map<String, Object> featureData) throws StatusCodeWithException {

        AbstractBasePredictor predictor = constructPredictor(requestId, modelId, userId, featureData);
        return predictor.predict();
    }

    public static PredictResult batch(String requestId,
                                      String modelId,
                                      List<String> userIds,
                                      Map<String, Map<String, Object>> featureDataMap) throws StatusCodeWithException {

        AbstractBasePredictor predictor = constructPredictor(requestId, modelId, userIds, featureDataMap);
        return predictor.predict();
    }

    private static AbstractBasePredictor constructPredictor(String requestId,
                                                            String modelId,
                                                            String userId,
                                                            Map<String, Object> featureData) throws StatusCodeWithException {
        JobMemberRole myRole = findMyRole(modelId);

        return myRole.equals(JobMemberRole.promoter) ?
                new PromoterPredictor(requestId, modelId, userId, featureData)
                : new ProviderPredictor(modelId, userId, featureData);
    }

    private static AbstractBasePredictor constructPredictor(String requestId,
                                                            String modelId,
                                                            List<String> userIds,
                                                            Map<String, Map<String, Object>> featureDataMap) throws StatusCodeWithException {
        JobMemberRole myRole = findMyRole(modelId);

        return myRole.equals(JobMemberRole.promoter) ?
                new BatchPromoterPredictor(requestId, modelId, userIds, featureDataMap)
                : new BatchProviderPredictor(modelId, userIds, featureDataMap);
    }

    private static JobMemberRole findMyRole(String modelId) throws StatusCodeWithException {
        List<ModelMemberMySqlModel> model = modelMemberService.findListByModelIdAndMemberId(modelId, CacheObjects.getMemberId());
        if (CollectionUtils.isEmpty(model)) {
            StatusCode.DATA_NOT_FOUND.throwException("未查找到模型数据！");
        }
        return model.get(0).getRole();
    }


    /**
     * predict Interface
     *
     * @param modelId model id
     */
    public static PredictResult debug(String modelId,
                                      String userId,
                                      Map<String, Object> featureData,
                                      PredictFeatureDataSource featureSource,
                                      JSONObject extendParams) throws Exception {

        AbstractBasePredictor predictor = constructDebugPredictor(modelId, userId, featureData, featureSource, extendParams);
        return predictor.predict();
    }

    private static AbstractBasePredictor constructDebugPredictor(String modelId,
                                                                 String userId,
                                                                 Map<String, Object> featureData,
                                                                 PredictFeatureDataSource featureDataSource,
                                                                 JSONObject extendParams) throws StatusCodeWithException {
        JobMemberRole myRole = findMyRole(modelId);

        return myRole.equals(JobMemberRole.promoter) ?
                new DebugPromoterPredictor(ServiceResultOutput.buildId(), modelId, userId, featureData, featureDataSource, extendParams)
                : new DebugProviderPredictor(modelId, userId, featureData, featureDataSource, extendParams);
    }
}
