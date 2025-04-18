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

package com.welab.wefe.serving.sdk.predicter.single;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.serving.sdk.algorithm.AbstractAlgorithm;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.manager.AlgorithmManager;
import com.welab.wefe.serving.sdk.manager.ModelProcessorManager;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.predicter.AbstractBasePredictor;
import com.welab.wefe.serving.sdk.processor.AbstractModelProcessor;

import java.util.Map;

/**
 * Single prediction
 *
 * @author hunter.zhao
 */
public abstract class AbstractSinglePredictor extends AbstractBasePredictor {

    protected PredictParams predictParams;


    public AbstractSinglePredictor(String modelId, String userId, Map<String, Object> featureData) {
        super(modelId);
        this.predictParams = PredictParams.create(userId, featureData);
    }

    /**
     * Get the processor of the corresponding model
     */
    public AbstractModelProcessor getProcessor() {
        return ModelProcessorManager.getProcessor(modelId);
    }

    /**
     * predict
     */
    @Override
    public PredictResult predict() throws StatusCodeWithException {

        BaseModel model = getModel();

        predictParams.setFeatureDataModel(findFeatureData(predictParams.getUserId()));

        AbstractModelProcessor processor = getProcessor();

        processor.preprocess(model, predictParams);

        AbstractAlgorithm algorithm = AlgorithmManager.get(model);

        Object result = algorithm.execute(model.getParams(), predictParams, federatedResultByProviders());

        processor.postprocess(result, model, predictParams);

        return new PredictResult(
                model.getAlgorithm(),
                model.getFlType(),
                model.getMyRole(),
                result
        );
    }

}
