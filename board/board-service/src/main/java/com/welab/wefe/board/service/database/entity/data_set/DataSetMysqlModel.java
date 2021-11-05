/**
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

package com.welab.wefe.board.service.database.entity.data_set;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.DataSetPublicLevel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author Zane
 */
@Entity(name = "data_set")
public class DataSetMysqlModel extends AbstractDataSetMysqlModel {

    /**
     * 表名
     */
    private String tableName;
    /**
     * 数据行数
     */
    private Long rowCount;
    /**
     * 主键字段
     */
    private String primaryKeyColumn;
    /**
     * 数据集列数
     */
    private Integer columnCount;
    /**
     * 数据集字段列表
     */
    private String columnNameList;
    /**
     * 特征数量
     */
    private Integer featureCount;
    /**
     * 特征列表
     */
    private String featureNameList;
    /**
     * 是否包含 Y 值
     */
    @Column(name = "contains_y")
    private Boolean containsY;
    /**
     * y列的数量
     */
    private Integer yCount;
    /**
     * y列名称列表
     */
    private String yNameList;

    /**
     * 来源类型，枚举（原始、对齐、分箱）
     */
    @Enumerated(EnumType.STRING)
    private ComponentType sourceType;
    /**
     * 来源流程id
     */
    private String sourceFlowId;
    /**
     * 来源任务id
     */
    private String sourceJobId;
    /**
     * 来源子任务id
     */
    private String sourceTaskId;

    /**
     * 正例样本数量
     */
    private Long yPositiveExampleCount = 0L;

    /**
     * 正例样本比例
     */
    private Double yPositiveExampleRatio = 0D;


    //region getter/setter

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(String primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    public String getColumnNameList() {
        return columnNameList;
    }

    public void setColumnNameList(String columnNameList) {
        this.columnNameList = columnNameList;
    }

    public Integer getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(Integer featureCount) {
        this.featureCount = featureCount;
    }

    public String getFeatureNameList() {
        return featureNameList;
    }

    public void setFeatureNameList(String featureNameList) {
        this.featureNameList = featureNameList;
    }

    public Boolean getContainsY() {
        return containsY;
    }

    public void setContainsY(Boolean containsY) {
        this.containsY = containsY;
    }

    public Integer getyCount() {
        return yCount;
    }

    public void setyCount(Integer yCount) {
        this.yCount = yCount;
    }

    public String getyNameList() {
        return yNameList;
    }

    public void setyNameList(String yNameList) {
        this.yNameList = yNameList;
    }

    public ComponentType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ComponentType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceFlowId() {
        return sourceFlowId;
    }

    public void setSourceFlowId(String sourceFlowId) {
        this.sourceFlowId = sourceFlowId;
    }

    public String getSourceJobId() {
        return sourceJobId;
    }

    public void setSourceJobId(String sourceJobId) {
        this.sourceJobId = sourceJobId;
    }

    public String getSourceTaskId() {
        return sourceTaskId;
    }

    public void setSourceTaskId(String sourceTaskId) {
        this.sourceTaskId = sourceTaskId;
    }

    public Long getyPositiveExampleCount() {
        return yPositiveExampleCount;
    }

    public void setyPositiveExampleCount(Long yPositiveExampleCount) {
        this.yPositiveExampleCount = yPositiveExampleCount;
    }

    public Double getyPositiveExampleRatio() {
        return yPositiveExampleRatio;
    }

    public void setyPositiveExampleRatio(Double yPositiveExampleRatio) {
        this.yPositiveExampleRatio = yPositiveExampleRatio;
    }


    //endregion
}
