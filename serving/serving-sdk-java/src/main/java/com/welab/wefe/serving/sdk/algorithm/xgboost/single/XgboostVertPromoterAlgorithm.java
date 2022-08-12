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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.serving.sdk.algorithm.xgboost.XgboostAlgorithmHelper;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.enums.XgboostWorkMode;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.xgboost.BaseXgboostModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgbProviderPredictResultModel;
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
    private Map<String, Object> getPartnerTreeStructure(List<JObject> federatedResult) throws StatusCodeWithException {

        Map<String, Object> remoteDecisionTreeMap = new HashMap<>();

        if (CollectionUtils.isEmpty(federatedResult)) {
            throw new StatusCodeWithException("federatedResult is null", StatusCode.REMOTE_SERVICE_ERROR);
        }

        for (JObject remote : federatedResult) {

            if (remote.isEmpty()) {
                continue;
            }

            XgbProviderPredictResultModel predictModel = remote.getJObject("result").toJavaObject(XgbProviderPredictResultModel.class);

            if (StringUtil.isNotEmpty(predictModel.getError())) {
                StatusCode.REMOTE_SERVICE_ERROR.throwException(predictModel.getError());
            }

            Map<String, Object> tree = (Map) predictModel.getXgboostTree();

            for (String key : tree.keySet()) {
                if (remoteDecisionTreeMap.containsKey(key)
                        && XgboostWorkMode.skip.name().equals(modelParam.getModelMeta().getWorkMode())) {
                    Map<String, Boolean> map = (Map) remoteDecisionTreeMap.get(key);
                    map.putAll((Map) tree.get(key));
                    remoteDecisionTreeMap.put(key, map);
                } else {
                    remoteDecisionTreeMap.put(key, tree.get(key));
                }
            }

        }

        return remoteDecisionTreeMap;
    }

    @Override
    protected PredictModel handlePredict(PredictParams predictParams, List<JObject> federatedResult) throws StatusCodeWithException {
        return XgboostAlgorithmHelper
                .promoterPredictByVert(
                        modelParam.getModelMeta().getWorkMode(),
                        modelParam.getModelParam(),
                        predictParams.getUserId(),
                        fidValueMapping,
                        getPartnerTreeStructure(federatedResult)
                );
    }

}
