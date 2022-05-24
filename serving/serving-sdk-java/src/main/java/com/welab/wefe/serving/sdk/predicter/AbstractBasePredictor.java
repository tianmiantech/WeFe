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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hunter.zhao
 */
public abstract class AbstractBasePredictor {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public FederatedParams federatedParams;
    public PredictParams predictParams;
    public String modelId;
    public JSONObject extendParams;

    public AbstractBasePredictor setFederatedParams(FederatedParams federatedParams) {
        this.federatedParams = federatedParams;
        return this;
    }

    public AbstractBasePredictor setPredictParams(PredictParams predictParams) {
        this.predictParams = predictParams;
        return this;
    }

    public AbstractBasePredictor setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    public AbstractBasePredictor setExtendParams(JSONObject extendParams) {
        this.extendParams = extendParams;
        return this;
    }

    public abstract PredictResult predict() throws StatusCodeWithException;

    /**
     * Feature engineering treatment
     */
    protected void featureEngineering() {

    }
}
