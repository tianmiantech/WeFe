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

package com.welab.wefe.serving.service.predicter.single;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.PredictFeatureDataSource;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.ProviderParams;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.predicter.single.AbstractPromoterPredicter;
import com.welab.wefe.serving.service.feature.CodeFeatureDataHandle;
import com.welab.wefe.serving.service.manager.FeatureManager;
import com.welab.wefe.serving.service.manager.ModelManager;

import java.util.List;
import java.util.Map;

import static com.welab.wefe.common.StatusCode.UNEXPECTED_ENUM_CASE;

/**
 * Debug model method
 *
 * @author hunter.zhao
 */
public class DebugPredicter extends AbstractPromoterPredicter {

    public DebugPredicter(String modelId,
                          PredictParams predictParams,
                          JSONObject params,
                          List<ProviderParams> providers,
                          PredictFeatureDataSource featureSource,
                          JobMemberRole myRole,
                          String memberId) {
        super(modelId, predictParams, params, providers, memberId);
        this.featureSource = featureSource;
        this.myRole = myRole;
    }

    protected PredictFeatureDataSource featureSource;

    protected JobMemberRole myRole;

    @Override
    public BaseModel getModel() throws StatusCodeWithException {
        return ModelManager.getModelParam(modelId, myRole);
    }


    @Override
    public Map<String, Object> fillFeatureData() throws StatusCodeWithException {
        switch (featureSource) {
            case api:
                return predictParams.getFeatureData();
            case code:
                return new CodeFeatureDataHandle().handle(modelId, predictParams);
            case sql:
                return FeatureManager.getFeatureData(params);
            default:
                throw new StatusCodeWithException(UNEXPECTED_ENUM_CASE);
        }
    }

    @Override
    public void featureEngineering() {
    }
}
