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
public class XgbProviderPredictResultModel extends PredictModel {
    private Object xgboostTree;

    public static XgbProviderPredictResultModel ofObject(String userId, Object xgboostTree) {
        XgbProviderPredictResultModel model = new XgbProviderPredictResultModel();
        model.userId = userId;
        model.xgboostTree = xgboostTree;
        return model;
    }

    public Object getXgboostTree() {
        return xgboostTree;
    }

    public void setXgboostTree(Object xgboostTree) {
        this.xgboostTree = xgboostTree;
    }
}
