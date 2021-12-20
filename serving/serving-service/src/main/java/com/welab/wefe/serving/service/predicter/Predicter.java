/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.dto.ProviderParams;
import com.welab.wefe.serving.service.database.serving.entity.ModelMemberBaseModel;
import com.welab.wefe.serving.service.predicter.batch.BatchPromoterPredicter;
import com.welab.wefe.serving.service.predicter.batch.BatchProviderPredicter;
import com.welab.wefe.serving.service.predicter.single.DebugPredicter;
import com.welab.wefe.serving.service.predicter.single.PromoterPredicter;
import com.welab.wefe.serving.service.predicter.single.ProviderPredicter;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.ModelMemberService;
import com.welab.wefe.serving.service.service.PredictLogService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author Zane
 */
public class Predicter {

    private static PredictLogService predictLogService;

    private static ModelMemberService modelMemberService;

    static {
        predictLogService = Launcher.CONTEXT.getBean(PredictLogService.class);
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


    /**
     * Initiator batch call logic
     */
    public static PredictResult batchPromoterPredict(String modelId, Map<String, Map<String, Object>> featureDataMap) throws Exception {


        long start = System.currentTimeMillis();

        String seqNo = "", memberId = "";
        PredictResult result;

        boolean requestResult = false;
        PredictParams predictParams = PredictParams.of(featureDataMap);

        try {
            //Generation predicter
            BatchPromoterPredicter promoterPredict = new BatchPromoterPredicter(modelId, predictParams, null, providers(modelId), CacheObjects.getMemberId());
            seqNo = promoterPredict.seqNo;
            memberId = promoterPredict.memberId;

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
            BatchProviderPredicter providerPredicter = new BatchProviderPredicter(federatedParams, predictParams, null);

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
    public static PredictResult promoter(String modelId,
                                         String userId,
                                         Map<String, Object> featureData,
                                         JSONObject params) throws Exception {

        long start = System.currentTimeMillis();

        String seqNo = "", memberId = "";
        PredictResult result = null;

        PredictParams predictParams = PredictParams.of(userId, featureData);
        boolean requestResult = false;

        try {

            //Generate predicter
            PromoterPredicter promoter = new PromoterPredicter(modelId, predictParams, params, providers(modelId), CacheObjects.getMemberId());
            seqNo = promoter.seqNo;
            memberId = promoter.memberId;

            //start predict
            result = promoter.predict();


            //Call succeeded
            requestResult = true;

        } finally {
            log(seqNo, modelId, memberId, userId, featureData, params, result, System.currentTimeMillis() - start, requestResult);
        }

        return result;
    }


    /**
     * provider predict
     */
    public static PredictResult provider(String seqNo,
                                         String modelId,
                                         String memberId,
                                         String userId,
                                         Map<String, Object> featureData,
                                         JSONObject params) throws Exception {

        long start = System.currentTimeMillis();

        PredictResult result = null;
        PredictParams predictParams = PredictParams.of(userId, featureData);
        FederatedParams federatedParams = FederatedParams.of(seqNo, modelId, memberId);
        boolean requestResult = false;

        try {

            ProviderPredicter provider = new ProviderPredicter(federatedParams, predictParams, params);
            result = provider.predict();


            //Call succeeded
            requestResult = true;

        } finally {
            log(seqNo, modelId, memberId, userId, featureData, params, result, System.currentTimeMillis() - start, requestResult);
        }

        return result;
    }

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
        boolean requestResult = false;

        try {


            DebugPredicter debug = new DebugPredicter(modelId, predictParams, params, providers(modelId), featureSource, myRole, CacheObjects.getMemberId());
            seqNo = debug.seqNo;
            memberId = debug.memberId;
            result = debug.predict();


            //Call succeeded
            requestResult = true;

        } finally {
            log(seqNo, modelId, memberId, userId, featureData, params, result, System.currentTimeMillis() - start, requestResult);
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
    private static List<ProviderParams> providers(String modelId) {
        /*---↓↓↓↓↓ Get partner information↓↓↓↓↓---*/

        List<ModelMemberBaseModel> modelMember = modelMemberService.findModelMemberBase(modelId, JobMemberRole.provider.name());

        return modelMember
                .stream()
                .map(x -> ModelMapper.map(x, ProviderParams.class))
                .collect(Collectors.toList());

        /*---↑↑↑↑↑Get partner information↑↑↑↑↑---*/

    }
}
