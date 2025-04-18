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

package com.welab.wefe.serving.sdk.model.xgboost;

import com.welab.wefe.serving.sdk.model.PredictModel;

/**
 * @author hunter.zhao
 */
public class XgboostPredictResultModel extends PredictModel {
    public Object scores;

    public static XgboostPredictResultModel ofScores(String userId, Object scores) {
        XgboostPredictResultModel model = new XgboostPredictResultModel();
        model.userId = userId;
        model.scores = scores;
        return model;
    }

    public static XgboostPredictResultModel fail(String userId, String error) {
        XgboostPredictResultModel model = new XgboostPredictResultModel();
        model.userId = userId;
        model.error = error;
        return model;
    }

    public Object getScores() {
        return scores;
    }

    public void setScores(Object scores) {
        this.scores = scores;
    }
}
