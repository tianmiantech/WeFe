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

package com.welab.wefe.board.service.dto.entity.data_set;

import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * @author Zane
 */
public class TableDataSetOutputModel extends AbstractDataSetOutputModel {

    @Check(name = "表名")
    private String tableName;
    @Check(name = "数据行数")
    private Long rowCount;
    @Check(name = "主键字段")
    private String primaryKeyColumn;
    @Check(name = "数据集列数")
    private Integer columnCount;
    @Check(name = "数据集字段列表")
    private String columnNameList;
    @Check(name = "特征数量")
    private Integer featureCount;
    @Check(name = "特征列表")
    private String featureNameList;
    @Check(name = "是否包含 Y 值")
    private Boolean containsY;
    @Check(name = "y列的数量")
    private Integer yCount;
    @Check(name = "y列名称列表")
    private String yNameList;

    @Check(name = "来源类型，枚举（原始、对齐、分箱）")
    private ComponentType sourceType;
    @Check(name = "来源任务id")
    private String sourceJobId;
    @Check(name = "来源流程id")
    private String sourceFlowId;
    @Check(name = "来源子任务id")
    private String sourceTaskId;

    @Check(name = "正例样本数量")
    private Long yPositiveExampleCount = 0L;

    @Check(name = "正例样本比例")
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

    public String getSourceJobId() {
        return sourceJobId;
    }

    public void setSourceJobId(String sourceJobId) {
        this.sourceJobId = sourceJobId;
    }

    public String getSourceFlowId() {
        return sourceFlowId;
    }

    public void setSourceFlowId(String sourceFlowId) {
        this.sourceFlowId = sourceFlowId;
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