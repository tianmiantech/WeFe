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

package com.welab.wefe.serving.sdk.predicter;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.model.FeatureDataModel;
import com.welab.wefe.serving.sdk.processor.AbstractModelProcessor;

import java.util.List;

/**
 * @author hunter.zhao
 */
public interface PredictBehavior {

    /**
     * get Model
     *
     * @return BaseModel
     * @throws StatusCodeWithException
     */
    BaseModel getModel() throws StatusCodeWithException;


    /**
     * processor
     *
     * @return Model Processor
     */
    List<JObject> federatedResultByProviders() throws StatusCodeWithException;


    /**
     * Find features
     * <p>
     * Build format must be{"x0":"0.12231","x1":"2.056412",...}
     * </p>
     *
     * @return featureMap
     * @throws StatusCodeWithException
     */
    FeatureDataModel findFeatureData(String userId) throws StatusCodeWithException;
}
