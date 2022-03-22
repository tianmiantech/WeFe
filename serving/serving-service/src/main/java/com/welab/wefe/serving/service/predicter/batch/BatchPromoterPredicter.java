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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.ProviderParams;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.predicter.batch.AbstractBatchPromoterPredicter;
import com.welab.wefe.serving.service.manager.FeatureManager;
import com.welab.wefe.serving.service.manager.ModelManager;

import java.util.List;
import java.util.Map;

/**
 * Batch model call promoter
 *
 * @author hunter.zhao
 */
public class BatchPromoterPredicter extends AbstractBatchPromoterPredicter {

    public BatchPromoterPredicter(String modelId, PredictParams predictParam, JSONObject params, List<ProviderParams> providers, String memberId) {
        super(modelId, predictParam, params, providers, memberId);
    }


    @Override
    public BaseModel getModel() throws StatusCodeWithException {
        return ModelManager.getModelParam(modelId);
    }

    @Override
    public void featureEngineering() {

    }


    @Override
    public Map<String, Map<String, Object>> batchFillFeatureData() throws StatusCodeWithException {
        return FeatureManager.getFeatureDataByBatch(modelId, predictParams);
    }
}
