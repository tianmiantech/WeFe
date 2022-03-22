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
package com.welab.wefe.board.service.dto.entity.data_resource.output;

import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * @author zane
 * @date 2021/12/1
 */
public class TableDataSetOutputModel extends DataResourceOutputModel {
    @Check(name = "数据集字段列表")
    private String columnNameList;
    @Check(name = "数据集列数")
    private Integer columnCount;
    @Check(name = "主键字段")
    private String primaryKeyColumn;
    @Check(name = "特征列表")
    private String featureNameList;
    @Check(name = "特征数量")
    private Integer featureCount;
    @Check(name = "是否包含;Y 值")
    private boolean containsY;
    @Check(name = "y列名称列表")
    private String yNameList;
    @Check(name = "y列的数量")
    private Integer yCount;
    @Check(name = "正样本的值")
    private String positiveSampleValue;
    @Check(name = "正例数量")
    private Long yPositiveSampleCount;
    @Check(name = "正例比例")
    private Double yPositiveSampleRatio;

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


    // endregion
}
