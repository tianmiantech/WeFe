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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.serving.sdk.config.Launcher;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.dto.ProviderParams;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class mainly demonstrates how to use the client
 *
 * @author hunter.zhao
 */
public class Example {

    static {
        try {
            Launcher.init("memberId", "rsaPrivateKey", "rsaPublicKey");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        List<ProviderParams> providers = Lists.newArrayList();
        providers.add(ProviderParams.of("member01", "10.0.0.1/provider"));
        providers.add(ProviderParams.of("member02", "10.0.0.2/provider"));

        Map<String, Object> featureData = new HashMap<>(16);
        featureData.put("x0", 0.100016);
        featureData.put("x1", 1.210);
        featureData.put("x2", 2.321);
        featureData.put("x3", 3.432);
        featureData.put("x4", 4.543);
        featureData.put("x5", 5.654);
        PredictParams predictParams = PredictParams.of("15555555555", featureData);

        try {

            /**
             * promoter
             */
            ExamplePromoterPredicter promoter = new ExamplePromoterPredicter("modelId", predictParams, new JSONObject(), providers, "memberId");
            PredictResult promoterResult = promoter.predict();
            System.err.println(JSON.toJSONString(promoterResult));


            /**
             * provider
             */
            ExampleProviderPredicter provider = new ExampleProviderPredicter(
                    FederatedParams.of("", "modelId-02", "memberId"),
                    predictParams,
                    new JSONObject());
            PredictResult providerResult = provider.predict();
            System.err.println(JSON.toJSONString(providerResult));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("over");
    }


}
