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

package com.welab.wefe.serving.sdk.predicter.batch;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;

/**
 * @author hunter.zhao
 */
public abstract class AbstractBatchProviderPredicter extends AbstractBatchPredicter {


    public AbstractBatchProviderPredicter(FederatedParams federatedParams,
                                          PredictParams predictParams,
                                          JSONObject params) {

        this.modelId = federatedParams.getModelId();

        this.federatedParams = federatedParams;
        this.predictParams = predictParams;
        this.params = params;
    }
}
