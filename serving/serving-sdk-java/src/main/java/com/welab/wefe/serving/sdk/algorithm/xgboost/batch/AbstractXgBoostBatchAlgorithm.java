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

package com.welab.wefe.serving.sdk.algorithm.xgboost.batch;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.algorithm.AbstractBatchAlgorithm;
import com.welab.wefe.serving.sdk.dto.BatchPredictParams;
import com.welab.wefe.serving.sdk.model.xgboost.BaseXgboostModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
public abstract class AbstractXgBoostBatchAlgorithm<T extends BaseXgboostModel, R> extends AbstractBatchAlgorithm<T, R> {

    /**
     * userId : featureDataMap
     */
    protected Map<String, Map<String, Object>> fidValueMapping = new HashMap<>();


    /**
     * Feature entry conversion
     * <p>
     * {"x0":0.11111,"x1":0.2222,"x2":0.33333}->{"0":0.11111,"1":0.2222,"2":0.33333}
     */
    private void setFidValueMapping(BatchPredictParams batchPredictParams) throws StatusCodeWithException {

        Map<String, String> tempMap = new HashMap<>(16);

        for (Map.Entry<String, String> map : modelParam.getModelParam().getFeatureNameFidMapping().entrySet()) {
            tempMap.put(map.getValue(), map.getKey());
        }

        batchPredictParams.getPredictParamsList().stream()
                .forEach(
                        x -> {
//                            Map<String, Object> featureMap = new HashMap<>(16);
//                            Map<String, Object> temp = x.getFeatureDataModel().getFeatureDataMap();
//                            if (temp != null) {
//                                for (String key : temp.keySet()) {
//                                    if (tempMap.containsKey(key)) {
//                                        featureMap.put(tempMap.get(key), temp.get(key));
//                                    }
//                                }
//                            }
                            Map<String, Object> featureMap = x.getFeatureDataModel().getFeatureDataMap().entrySet().stream()
                                    .filter(y -> tempMap.containsKey(y.getKey()))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                            fidValueMapping.put(x.getUserId(), featureMap);
                        }
                );
    }

    @Override
    protected R handle(BatchPredictParams batchPredictParams, List<JObject> federatedResult) throws StatusCodeWithException {
        setFidValueMapping(batchPredictParams);

        return handlePredict(batchPredictParams, federatedResult);
    }

    /**
     * Model execution method
     *
     * @param batchPredictParams
     * @return predict result
     * @throws StatusCodeWithException
     */
    protected abstract R handlePredict(BatchPredictParams batchPredictParams, List<JObject> federatedResult) throws StatusCodeWithException;
}
