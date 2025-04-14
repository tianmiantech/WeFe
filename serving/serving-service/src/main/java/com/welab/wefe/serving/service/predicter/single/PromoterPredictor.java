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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.serving.sdk.dto.ProviderParams;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.model.FeatureDataModel;
import com.welab.wefe.serving.sdk.predicter.single.AbstractSinglePredictor;
import com.welab.wefe.serving.service.manager.FeatureManager;
import com.welab.wefe.serving.service.manager.ModelManager;
import com.welab.wefe.serving.service.service.ClientServiceService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model call initiator
 *
 * @author hunter.zhao
 */
public class PromoterPredictor extends AbstractSinglePredictor {

    private String requestId;

    public PromoterPredictor(String requestId,
                             String modelId,
                             String userId,
                             Map<String, Object> featureData) {
        super(modelId, userId, featureData);
        this.requestId = requestId;
    }

    @Override
    public BaseModel getModel() throws StatusCodeWithException {
        return ModelManager.getModelParam(modelId);
    }

    @Override
    public List<JObject> federatedResultByProviders() throws StatusCodeWithException {

        ClientServiceService service = Launcher.CONTEXT.getBean(ClientServiceService.class);
        List<ProviderParams> providerList = service.findProviderList(modelId);

        if (CollectionUtils.isEmpty(providerList)) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "未找到纵向联邦的协作方！");
        }

        List<JObject> federatedResult = new ArrayList<>();
        for (ProviderParams provider : providerList) {
            String requestParam = PromoterPredictHelper.buildFederatedPredictParam(modelId, requestId, predictParams.getUserId());
            JObject response = PromoterPredictHelper.callProviders(modelId, requestId, provider, requestParam);
            federatedResult.add(response);
        }

        return federatedResult;
    }

    @Override
    public FeatureDataModel findFeatureData(String userId) throws StatusCodeWithException {
        if (MapUtils.isNotEmpty(predictParams.getFeatureDataModel().getFeatureDataMap())) {
            return predictParams.getFeatureDataModel();
        }

        return FeatureManager.getFeatureData(modelId, userId);
    }

}
