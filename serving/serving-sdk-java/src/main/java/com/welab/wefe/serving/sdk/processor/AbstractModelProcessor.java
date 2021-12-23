/*
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

package com.welab.wefe.serving.sdk.processor;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.model.BaseModel;


/**
 * Model processor, used to inject additional logic during model prediction.
 *
 * @author Zane
 */

public abstract class AbstractModelProcessor {

    /**
     * Actions performed before model prediction
     * @param model
     * @param federatedParams
     * @param predictParams
     * @param params
     */
    public abstract void preprocess(BaseModel model, FederatedParams federatedParams, PredictParams predictParams, JSONObject params);

    /**
     * Actions performed after model prediction
     * @param result
     * @param model
     * @param federatedParams
     * @param predictParams
     * @param params
     */
    public abstract void postprocess(PredictResult result, BaseModel model, FederatedParams federatedParams, PredictParams predictParams, JSONObject params);
}
