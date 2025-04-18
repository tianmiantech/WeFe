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

package com.welab.wefe.serving.sdk.test;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.model.FeatureDataModel;
import com.welab.wefe.serving.sdk.predicter.single.AbstractSinglePredictor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class mainly demonstrates how to customize the promoter prediction class
 *
 * @author hunter.zhao
 */
public class ExamplePromoterPredicter extends AbstractSinglePredictor {

    public ExamplePromoterPredicter(String modelId, String userId, Map<String, Object> featureData) {
        super(modelId, userId, featureData);
    }

    @Override
    public BaseModel getModel() {

        /**
         * Custom example
         */
        BaseModel model = new BaseModel();
        model.setModelId(modelId);
        model.setAlgorithm(Algorithm.LogisticRegression);
        model.setFlType(FederatedLearningType.horizontal);
        model.setMyRole(JobMemberRole.promoter);
        model.setParams("{\n" +
                "  \"iters\": 1,\n" +
                "  \"weight\": {\n" +
                "    \"x0\": -0.90541326,\n" +
                "    \"x1\": -0.12530537,\n" +
                "    \"x2\": -0.36894084,\n" +
                "    \"x3\": -1.16595136,\n" +
                "    \"x4\": -0.81097973,\n" +
                "    \"x5\": -0.42861154\n" +
                "  },\n" +
                "  \"intercept\": -2.28208168,\n" +
                "  \"header\": [\n" +
                "    \"x0\",\n" +
                "    \"x1\",\n" +
                "    \"x2\",\n" +
                "    \"x3\",\n" +
                "    \"x4\",\n" +
                "    \"x5\"\n" +
                "  ]\n" +
                "}");

        return model;
    }

    @Override
    public List<JObject> federatedResultByProviders() throws StatusCodeWithException {

        List<JObject> federatedResult = new ArrayList<>();

//        for (ProviderParams obj : ) {
//            AbstractBasePredictor provider = new ExampleProviderPredicter()
//                    .setPredictParams(predictParams)
//                    .setFederatedParams(FederatedParams.of("modelId-02", obj.getMemberId()));
//            PredictResult providerResult = provider.predict();
//            federatedResult.add(JObject.create(JSON.toJSONString(providerResult)));
//        }

        return federatedResult;
    }

    @Override
    public FeatureDataModel findFeatureData(String userId) throws StatusCodeWithException {
        return predictParams.getFeatureDataModel();
    }


}
