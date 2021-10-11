/**
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

package com.welab.wefe.serving.sdk.predicter.single;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.serving.sdk.algorithm.AbstractAlgorithm;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.manager.AlgorithmManager;
import com.welab.wefe.serving.sdk.manager.ModelProcessorManager;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.predicter.AbstractBasePredicter;
import com.welab.wefe.serving.sdk.predicter.SinglePredicter;
import com.welab.wefe.serving.sdk.processor.AbstractModelProcessor;

/**
 * Single prediction
 *
 * @author hunter.zhao
 */
public abstract class AbstractSinglePredicter extends AbstractBasePredicter implements SinglePredicter {


    /**
     * Get the processor of the corresponding model
     */
    @Override
    public AbstractModelProcessor getProcessor() {
        return ModelProcessorManager.getProcessor(modelId);
    }

    /**
     * predict
     */
    @Override
    public PredictResult predict() throws Exception {

        BaseModel model = getModel();

        predictParams.setFeatureData(fillFeatureData());

        featureEngineering();

        AbstractModelProcessor processor = getProcessor();

        processor.preprocess(model, federatedParams, predictParams, params);

        AbstractAlgorithm algorithm = AlgorithmManager.get(model);
        if(algorithm == null){
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID,"The corresponding model is not found. Please check whether the parameters are incorrect");
        }
        PredictResult result = algorithm.execute(model, federatedParams, predictParams, params);

        processor.postprocess(result, model, federatedParams, predictParams, params);

        return result;
    }

}
