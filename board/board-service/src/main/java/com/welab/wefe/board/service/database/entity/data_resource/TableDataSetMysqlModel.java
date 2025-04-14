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
package com.welab.wefe.board.service.database.entity.data_resource;


import com.alibaba.fastjson.JSONObject;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.wefe.board.service.dto.vo.data_set.table_data_set.LabelDistribution;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author zane
 * @date 2021/12/1
 */
@Entity(name = "table_data_set")
@Table(name = "table_data_set")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class TableDataSetMysqlModel extends DataResourceMysqlModel {
    /**
     * 数据集字段列表
     */
    private String columnNameList;
    /**
     * 数据集列数
     */
    private Integer columnCount;
    /**
     * 主键字段
     */
    private String primaryKeyColumn;
    /**
     * 特征列表
     */
    private String featureNameList;
    /**
     * 特征数量
     */
    private Integer featureCount;
    /**
     * 是否包含;Y 值
     */
    @Column(name = "contains_y")
    private boolean containsY;
    /**
     * y列名称列表
     */
    private String yNameList;
    /**
     * y列的数量
     */
    private Integer yCount;
    /**
     * 正样本的值
     */
    private String positiveSampleValue;
    /**
     * 正例数量
     */
    private Long yPositiveSampleCount;
    /**
     * 正例比例
     */
    private Double yPositiveSampleRatio;
    /**
     * label 的分布情况
     */
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private JSONObject labelDistribution;


    public TableDataSetMysqlModel() {
        super.setDataResourceType(DataResourceType.TableDataSet);
    }

    public void setLabelDistribution(LabelDistribution labelDistribution) {
        this.labelDistribution = labelDistribution.toJson();
    }

    public JSONObject getLabelDistribution() {
        if (labelDistribution == null) {
            return new JSONObject();
        }
        return labelDistribution;
    }

    // region getter/setter

    public String getColumnNameList() {
        return columnNameList;
    }

    public void setColumnNameList(String columnNameList) {
        this.columnNameList = columnNameList;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(String primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public String getFeatureNameList() {
        return featureNameList;
    }

    public void setFeatureNameList(String featureNameList) {
        this.featureNameList = featureNameList;
    }

    public Integer getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(Integer featureCount) {
        this.featureCount = featureCount;
    }

    public boolean isContainsY() {
        return containsY;
    }

    public void setContainsY(boolean containsY) {
        this.containsY = containsY;
    }

    public String getyNameList() {
        return yNameList;
    }

    public void setyNameList(String yNameList) {
        this.yNameList = yNameList;
    }

    public Integer getyCount() {
        return yCount;
    }

    public void setyCount(Integer yCount) {
        this.yCount = yCount;
    }

    public String getPositiveSampleValue() {
        return positiveSampleValue;
    }

    public void setPositiveSampleValue(String positiveSampleValue) {
        this.positiveSampleValue = positiveSampleValue;
    }

    public Long getyPositiveSampleCount() {
        return yPositiveSampleCount;
    }

    public void setyPositiveSampleCount(Long yPositiveSampleCount) {
        this.yPositiveSampleCount = yPositiveSampleCount;
    }

    public Double getyPositiveSampleRatio() {
        return yPositiveSampleRatio;
    }

    public void setyPositiveSampleRatio(Double yPositiveSampleRatio) {
        this.yPositiveSampleRatio = yPositiveSampleRatio;
    }

    public void setLabelDistribution(JSONObject labelDistribution) {
        this.labelDistribution = labelDistribution;
    }

    // endregion
}
