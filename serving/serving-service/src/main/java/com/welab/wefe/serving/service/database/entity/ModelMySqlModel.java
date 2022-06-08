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

package com.welab.wefe.serving.service.database.entity;

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

    private static final long serialVersionUID = -9177967935224674890L;

    @Column(name = "model_id")
    private String modelId;

    // 机器学习模型字段
    @Enumerated(EnumType.STRING)
    private Algorithm algorithm;

    @Enumerated(EnumType.STRING)
    @Column(name = "fl_type")
    private FederatedLearningType flType;

    @Column(name = "model_param")
    private String modelParam;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_source")
    private PredictFeatureDataSource featureSource = PredictFeatureDataSource.code;

    // 深度学习模型字段

    @Column(name = "source_path")// 文件路径
    String sourcePath;

    String filename;// 文件名

    @Column(name = "use_count")// 使用计数
    int useCount;

    // 通用字段
    private boolean enable;

    private String name;

    private String url;

    @Column(name = "service_type")
    private Integer serviceType;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

}
