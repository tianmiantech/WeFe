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

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.algorithm.xgboost.XgboostAlgorithmHelper;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.enums.StateCode;
import com.welab.wefe.serving.sdk.model.xgboost.BaseXgboostModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgbProviderPredictResultModel;

import java.util.List;

/**
 * Vertically federated Provider(xgboost)
 *
 * @author hunter.zhao
 */
public class XgboostVertProviderAlgorithm extends AbstractXgboostAlgorithm<BaseXgboostModel, XgbProviderPredictResultModel> {

    @Override
    protected XgbProviderPredictResultModel handlePredict(PredictParams predictParams, List<JObject> federatedResult) {
        if (fidValueMapping.isEmpty()) {
            return XgbProviderPredictResultModel.fail(predictParams.getUserId(), StateCode.FEATURE_ERROR.getMessage());
        }

        return XgboostAlgorithmHelper.providerPredict(modelParam.getModelMeta().getWorkMode(), modelParam.getModelParam(), predictParams.getUserId(), fidValueMapping);
    }
}
