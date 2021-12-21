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

package com.welab.wefe.serving.service.database.serving.entity;

import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author hunter.zhao
 */
@Entity(name = "model")
public class ModelMySqlModel extends AbstractBaseMySqlModel {
    @Column(name = "model_id")
    private String modelId;

    @Enumerated(EnumType.STRING)
    private Algorithm algorithm;

    @Enumerated(EnumType.STRING)
    @Column(name = "fl_type")
    private FederatedLearningType flType;

    @Column(name = "model_param")
    private String modelParam;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_source")
    private PredictFeatureDataSource featureSource = PredictFeatureDataSource.api;

    private boolean enable;

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public FederatedLearningType getFlType() {
        return flType;
    }

    public void setFlType(FederatedLearningType flType) {
        this.flType = flType;
    }

    public String getModelParam() {
        return modelParam;
    }

    public void setModelParam(String modelParam) {
        this.modelParam = modelParam;
    }

    public PredictFeatureDataSource getFeatureSource() {
        return featureSource;
    }

    public void setFeatureSource(PredictFeatureDataSource featureSource) {
        this.featureSource = featureSource;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
