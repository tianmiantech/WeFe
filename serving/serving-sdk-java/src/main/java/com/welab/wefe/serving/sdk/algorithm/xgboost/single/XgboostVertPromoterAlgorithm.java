/*
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

package com.welab.wefe.serving.sdk.algorithm.xgboost.single;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.algorithm.xgboost.XgboostAlgorithmHelper;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.xgboost.BaseXgboostModel;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vertical federation initiator(xgboost)
 *
 * @author hunter.zhao
 */
public class XgboostVertPromoterAlgorithm extends AbstractXgboostAlgorithm<BaseXgboostModel, PredictModel> {

    /**
     * Federal forecast decision tree results
     */
    private Map<String, Map<String, Boolean>> remoteDecisionTreeMap = new HashMap<>();


    /**
     * Call the provider to get the federated decision tree
     * Return structure
     * <p>
     * {
     * "0":{
     * "1":true,
     * "2":false,
     * "3":false,
     * "4":false
     * },
     * "1":{
     * "0":false,
     * "2":false,
     * "8":false
     * }
     * }
     * </>
     */
    private void getFederatedPredict(FederatedParams federatedParams, PredictParams predictParams) throws StatusCodeWithException {


        List<JObject> federatedResult = federatedPredict(federatedParams.getProviders(), setFederatedPredictBody(federatedParams, predictParams.getUserId()));

        if (CollectionUtils.isEmpty(federatedResult)) {
            throw new StatusCodeWithException("federatedResult is null", StatusCode.REMOTE_SERVICE_ERROR);
        }

        for (JObject remote : federatedResult) {

            PredictModel predictModel = remote.getJObject("data").toJavaObject(PredictModel.class);

            Map<String, Map<String, Boolean>> tree = (Map) predictModel.getData();

            for (String key : tree.keySet()) {
                if (remoteDecisionTreeMap.containsKey(key)) {
                    remoteDecisionTreeMap.get(key).putAll(tree.get(key));
                } else {
                    remoteDecisionTreeMap.put(key, tree.get(key));
                }
            }

        }
    }

    @Override
    protected PredictModel handlePredict(FederatedParams federatedParams, PredictParams predictParams, JSONObject params) throws StatusCodeWithException {

        //Get partner decision tree structure
        getFederatedPredict(federatedParams, predictParams);

        return XgboostAlgorithmHelper
                .promoterPredictByVert(
                        modelParam.getModelParam(),
                        predictParams.getUserId(),
                        fidValueMapping,
                        remoteDecisionTreeMap
                );
    }

}
