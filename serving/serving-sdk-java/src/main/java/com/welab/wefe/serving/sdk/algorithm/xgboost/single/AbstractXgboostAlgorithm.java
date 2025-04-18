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

package com.welab.wefe.serving.sdk.algorithm.xgboost.single;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.algorithm.AbstractAlgorithm;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.xgboost.BaseXgboostModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public abstract class AbstractXgboostAlgorithm<T extends BaseXgboostModel, R extends PredictModel> extends AbstractAlgorithm<T, PredictModel> {

    /**
     * Characteristic mapping relation
     */
    protected Map<String, Object> fidValueMapping = new HashMap<>();


    /**
     * Feature entry conversion
     * <p>
     * {"x0":0.11111,"x1":0.2222,"x2":0.33333}->{"0":0.11111,"1":0.2222,"2":0.33333}
     */
    private void setFidValueMapping(PredictParams predictParams) {

        Map<String, String> tempMap = new HashMap<>(16);


        for (Map.Entry<String, String> map : modelParam.getModelParam().getFeatureNameFidMapping().entrySet()) {
            tempMap.put(map.getValue(), map.getKey());
        }

        Map<String, Object> features = predictParams.getFeatureDataModel().getFeatureDataMap();

        if (features != null) {
            for (String key : features.keySet()) {
                if (tempMap.containsKey(key)) {
                    fidValueMapping.put(tempMap.get(key), features.get(key));
                }
            }
        }

    }

    @Override
    protected PredictModel handle(PredictParams predictParams, List<JObject> federatedResult) throws StatusCodeWithException {

        setFidValueMapping(predictParams);

        PredictModel result = handlePredict(predictParams, federatedResult);

        result.setFeatureResult(
                PredictModel.extractFeatureResult(predictParams.getFeatureDataModel())
        );

        return result;
    }

    /**
     * Model execution method
     *
     * @param predictParams
     * @return predict result
     * @throws StatusCodeWithException
     */
    protected abstract R handlePredict(PredictParams predictParams, List<JObject> federatedResult) throws StatusCodeWithException;
}
