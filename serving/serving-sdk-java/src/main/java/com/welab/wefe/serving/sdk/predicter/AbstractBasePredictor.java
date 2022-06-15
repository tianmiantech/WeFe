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
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.model.FeatureDataModel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hunter.zhao
 */
public abstract class AbstractBasePredictor implements PredictBehavior {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public FederatedParams federatedParams;
    public String modelId;

    public AbstractBasePredictor(String modelId, FederatedParams federatedParams) {
        this.modelId = modelId;
        this.federatedParams = federatedParams;
    }

    /**
     * Model prediction function
     */
    public abstract PredictResult predict() throws StatusCodeWithException;

    /**
     * Feature engineering treatment
     */
    protected void featureEngineering() {

    }
}
