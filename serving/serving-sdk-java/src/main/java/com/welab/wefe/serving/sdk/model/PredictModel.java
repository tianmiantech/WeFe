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

package com.welab.wefe.serving.sdk.model;

/**
 * @author hunter.zhao
 */
public class PredictModel {
    public String userId;
    public String error = "";
    private BaseFeatureResultModel featureResult;

    public static PredictModel fail(String userId, String error) {
        PredictModel model = new PredictModel();
        model.userId = userId;
        model.error = error;
        return model;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public BaseFeatureResultModel getFeatureResult() {
        return featureResult;
    }

    public void setFeatureResult(BaseFeatureResultModel featureResult) {
        this.featureResult = featureResult;
    }

    public static BaseFeatureResultModel extractFeatureResult(FeatureDataModel featureDataModel) {
        return BaseFeatureResultModel.of(featureDataModel.isFound(), featureDataModel.getError());
    }

}
