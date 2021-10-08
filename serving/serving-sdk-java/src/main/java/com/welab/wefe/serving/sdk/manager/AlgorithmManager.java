/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.serving.sdk.manager;


import com.welab.wefe.serving.sdk.algorithm.AbstractAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.lr.batch.LrHorzPromoterBatchAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.lr.batch.LrVertPromoterBatchAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.lr.batch.LrVertProviderBatchAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.lr.single.LrHorzPromoterAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.lr.single.LrVertPromoterAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.lr.single.LrVertProviderAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.xgboost.batch.XgboostHorzPromoterBatchAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.xgboost.batch.XgboostVertPromoterBatchAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.xgboost.batch.XgboostVertProviderBatchAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.xgboost.single.XgboostHorzPromoterAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.xgboost.single.XgboostVertPromoterAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.xgboost.single.XgboostVertProviderAlgorithm;
import com.welab.wefe.serving.sdk.model.BaseModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zane
 */
public class AlgorithmManager {
    private static final Map<String, Class<? extends AbstractAlgorithm>> MAP = new HashMap<>();
    private static final Map<String, Class<? extends AbstractAlgorithm>> BATCH_MAP = new HashMap<>();

    static {
        MAP.put("LogisticRegression_horizontal_promoter", LrHorzPromoterAlgorithm.class);
        MAP.put("LogisticRegression_vertical_promoter", LrVertPromoterAlgorithm.class);
        MAP.put("LogisticRegression_vertical_provider", LrVertProviderAlgorithm.class);
        MAP.put("LogisticRegression_horizontal_provider", LrHorzPromoterAlgorithm.class);

        MAP.put("XGBoost_vertical_promoter", XgboostVertPromoterAlgorithm.class);
        MAP.put("XGBoost_vertical_provider", XgboostVertProviderAlgorithm.class);
        MAP.put("XGBoost_horizontal_promoter", XgboostHorzPromoterAlgorithm.class);
        MAP.put("XGBoost_horizontal_provider", XgboostHorzPromoterAlgorithm.class);
    }

    static {
        BATCH_MAP.put("LogisticRegression_horizontal_promoter", LrHorzPromoterBatchAlgorithm.class);
        BATCH_MAP.put("LogisticRegression_vertical_promoter", LrVertPromoterBatchAlgorithm.class);
        BATCH_MAP.put("LogisticRegression_vertical_provider", LrVertProviderBatchAlgorithm.class);
        BATCH_MAP.put("LogisticRegression_horizontal_provider", LrHorzPromoterBatchAlgorithm.class);

        BATCH_MAP.put("XGBoost_vertical_promoter", XgboostVertPromoterBatchAlgorithm.class);
        BATCH_MAP.put("XGBoost_vertical_provider", XgboostVertProviderBatchAlgorithm.class);
        BATCH_MAP.put("XGBoost_horizontal_promoter", XgboostHorzPromoterBatchAlgorithm.class);
        BATCH_MAP.put("XGBoost_horizontal_provider", XgboostHorzPromoterBatchAlgorithm.class);
    }


    public static AbstractAlgorithm get(BaseModel model) {

        String key = model.algorithm.name() + "_" + model.flType.name() + "_" + model.myRole.name();

        Class<? extends AbstractAlgorithm> clazz = MAP.get(key);

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static AbstractAlgorithm getBatch(BaseModel model) {

        String key = model.algorithm.name() + "_" + model.flType.name() + "_" + model.myRole.name();

        Class<? extends AbstractAlgorithm> clazz = BATCH_MAP.get(key);

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
