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

package com.welab.wefe.serving.service.predicter.single;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.sdk.model.FeatureDataModel;
import com.welab.wefe.serving.service.feature.CodeFeatureDataHandler;
import com.welab.wefe.serving.service.manager.FeatureManager;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

import static com.welab.wefe.common.StatusCode.UNEXPECTED_ENUM_CASE;

/**
 * Debug model method
 *
 * @author hunter.zhao
 */
public class DebugPromoterPredictor extends PromoterPredictor {

    protected String requestId;

    protected PredictFeatureDataSource featureSource;

    public JSONObject extendParams;

    public DebugPromoterPredictor(String requestId,
                                  String modelId,
                                  String userId,
                                  Map<String, Object> featureData,
                                  PredictFeatureDataSource featureSource,
                                  JSONObject extendParams) {
        super(requestId, modelId, userId, featureData);
        this.extendParams = extendParams;
        this.featureSource = featureSource;
    }

    public DebugPromoterPredictor setFeatureSource(PredictFeatureDataSource featureSource) {
        this.featureSource = featureSource;
        return this;
    }

    @Override
    public FeatureDataModel findFeatureData(String userId) throws StatusCodeWithException {
        if (MapUtils.isNotEmpty(predictParams.getFeatureDataModel().getFeatureDataMap())) {
            return predictParams.getFeatureDataModel();
        }

        switch (featureSource) {
            case code:
                return new CodeFeatureDataHandler().handle(modelId, userId);
            case sql:
                return FeatureManager.getFeatureData(userId, extendParams);
            default:
                throw new StatusCodeWithException(UNEXPECTED_ENUM_CASE);
        }
    }
}
