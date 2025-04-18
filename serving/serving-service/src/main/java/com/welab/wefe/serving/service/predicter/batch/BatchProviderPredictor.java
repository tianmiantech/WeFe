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

package com.welab.wefe.serving.service.predicter.batch;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.model.FeatureDataModel;
import com.welab.wefe.serving.sdk.predicter.batch.AbstractBatchProviderPredictor;
import com.welab.wefe.serving.service.manager.FeatureManager;
import com.welab.wefe.serving.service.manager.ModelManager;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * model call provider
 *
 * @author hunter.zhao
 */
public class BatchProviderPredictor extends AbstractBatchProviderPredictor {

    public BatchProviderPredictor(String modelId, List<String> userIds, Map<String, Map<String, Object>> featureDataMap) {
        super(modelId, userIds, featureDataMap);
    }

    @Override
    public BaseModel getModel() throws StatusCodeWithException {
        return ModelManager.getModelParam(modelId);
    }

    @Override
    public FeatureDataModel findFeatureData(String userId) throws StatusCodeWithException {
        if (batchPredictParams.getPredictParamsByUserId(userId) != null &&
                MapUtils.isNotEmpty(batchPredictParams.getPredictParamsByUserId(userId).getFeatureDataModel().getFeatureDataMap())) {
            return batchPredictParams.getPredictParamsByUserId(userId).getFeatureDataModel();
        }

        return FeatureManager.getFeatureData(modelId, userId);
    }
}
