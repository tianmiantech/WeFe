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

package com.welab.wefe.serving.sdk.dto;

import com.welab.wefe.serving.sdk.model.FeatureDataModel;

import java.util.Map;

/**
 * @author hunter.zhao
 */
public class PredictParams {
    private String userId;

//    private Map<String, Object> featureData;

    private FeatureDataModel featureDataModel;
//
//    private List<String> userIds;
//
//    private Map<String, Map<String, Object>> featureDataMap;

    private PredictParams() {

    }

    public PredictParams(String userId) {
        this.userId = userId;
    }

    public static PredictParams of(String userId, Map<String, Object> featureDataMap) {
        PredictParams predictParams = new PredictParams();
        predictParams.userId = userId;
        predictParams.featureDataModel = FeatureDataModel.of(featureDataMap);
        return predictParams;
    }


    public static PredictParams of(String userId, FeatureDataModel featureDataModel) {
        PredictParams predictParams = new PredictParams();
        predictParams.userId = userId;
        predictParams.featureDataModel = featureDataModel;
        return predictParams;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public FeatureDataModel getFeatureDataModel() {
        return featureDataModel;
    }

    public void setFeatureDataModel(FeatureDataModel featureDataModel) {
        this.featureDataModel = featureDataModel;
    }
}
