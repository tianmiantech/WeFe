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

package com.welab.wefe.serving.sdk.predicter.batch;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.serving.sdk.algorithm.AbstractBatchAlgorithm;
import com.welab.wefe.serving.sdk.dto.BatchPredictParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.manager.AlgorithmManager;
import com.welab.wefe.serving.sdk.manager.ModelProcessorManager;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.predicter.AbstractBasePredictor;
import com.welab.wefe.serving.sdk.processor.AbstractBatchModelProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Batch prediction
 *
 * @author hunter.zhao
 */
public abstract class AbstractBatchPredictor extends AbstractBasePredictor {

    public BatchPredictParams batchPredictParams;

    public AbstractBatchPredictor(String modelId, List<String> userIds, Map<String, Map<String, Object>> featureDataMap) {
        super(modelId);
        this.batchPredictParams = BatchPredictParams.of(userIds, featureDataMap);
    }

    /**
     * Get the processor of the corresponding model
     */
    public AbstractBatchModelProcessor getProcessor() {
        return ModelProcessorManager.getBatchProcessor(modelId);
    }


    /**
     * predict
     */
    @Override
    public PredictResult predict() throws StatusCodeWithException {

        BaseModel model = getModel();

        batchPredictParams.setPredictParamsList(batchFindFeatureData());

        AbstractBatchModelProcessor processor = getProcessor();

        processor.preprocess(model, batchPredictParams);

        AbstractBatchAlgorithm algorithm = AlgorithmManager.getBatch(model);

        Object result = algorithm.execute(model.getParams(), batchPredictParams, federatedResultByProviders());

        processor.postprocess(result, model, batchPredictParams);

        return new PredictResult(
                model.getAlgorithm(),
                model.getFlType(),
                model.getMyRole(),
                result
        );
    }

    private List<PredictParams> batchFindFeatureData() {
        List<PredictParams> predictParamsList = batchPredictParams.getUserIds().stream()
                .map(x -> {
                    try {
                        return PredictParams.create(x, findFeatureData(x));
                    } catch (StatusCodeWithException e) {
                        e.printStackTrace();
                        return PredictParams.create(x, new HashMap<>());
                    }
                })
                .collect(Collectors.toList());
        return predictParamsList;
    }
}
