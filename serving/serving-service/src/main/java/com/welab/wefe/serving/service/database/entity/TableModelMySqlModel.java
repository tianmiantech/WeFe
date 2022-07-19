/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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

import javax.persistence.*;

/**
 * @author hunter.zhao
 */
@Entity(name = "table_model")
@Table(name = "table_model")
public class TableModelMySqlModel extends BaseServiceMySqlModel {

    private static final long serialVersionUID = -1320731560182386318L;

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

    @Column(name = "source_path") // 文件路径
    String sourcePath;

    String filename;// 文件名

    @Column(name = "use_count") // 使用计数
    int useCount;

    @Column(name = "sql_script")
    private String sqlScript;

    @Column(name = "sql_condition_field")
    private String sqlConditionField;

    @Column(name = "data_source_id")
    private String dataSourceId;

    @Column(name = "scores_distribution")
    private String scoresDistribution;

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

    public String getSqlScript() {
        return sqlScript;
    }

    public void setSqlScript(String sqlScript) {
        this.sqlScript = sqlScript;
    }

    public String getSqlConditionField() {
        return sqlConditionField;
    }

    public void setSqlConditionField(String sqlConditionField) {
        this.sqlConditionField = sqlConditionField;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getScoresDistribution() {
        return scoresDistribution;
    }

    public void setScoresDistribution(String scoresDistribution) {
        this.scoresDistribution = scoresDistribution;
    }
}
