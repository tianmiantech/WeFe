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

package com.welab.wefe.serving.service.predicter.single;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.predicter.single.AbstractSinglePredictor;
import com.welab.wefe.serving.service.feature.CodeFeatureDataHandler;
import com.welab.wefe.serving.service.manager.FeatureManager;
import com.welab.wefe.serving.service.manager.ModelManager;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

import static com.welab.wefe.common.StatusCode.UNEXPECTED_ENUM_CASE;

/**
 * Debug model method
 *
 * @author hunter.zhao
 */
public class DebugPredictor extends AbstractSinglePredictor {

    protected PredictFeatureDataSource featureSource;

    protected JobMemberRole myRole;

    public DebugPredictor setFeatureSource(PredictFeatureDataSource featureSource) {
        this.featureSource = featureSource;
        return this;
    }

    public DebugPredictor setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
        return this;
    }

    @Override
    public BaseModel getModel() throws StatusCodeWithException {
        return ModelManager.getModelParam(modelId, myRole);
    }

    @Override
    public List<JObject> federatedResultByProviders() throws StatusCodeWithException {
        return null;
    }

    @Override
    public Map<String, Object> findFeatureData() throws StatusCodeWithException {
        if (MapUtils.isNotEmpty(predictParams.getFeatureData())) {
            return predictParams.getFeatureData();
        }

        switch (featureSource) {
            case api:
                return predictParams.getFeatureData();
            case code:
                return new CodeFeatureDataHandler().handle(modelId, predictParams);
            case sql:
                return FeatureManager.getFeatureData(extendParams);
            default:
                throw new StatusCodeWithException(UNEXPECTED_ENUM_CASE);
        }
    }
}
