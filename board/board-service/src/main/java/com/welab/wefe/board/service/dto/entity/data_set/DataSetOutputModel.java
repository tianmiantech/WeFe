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

package com.welab.wefe.board.service.dto.entity.data_set;

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;

import java.util.List;
import java.util.TreeMap;

/**
 * @author Zane
 */
public class DataSetOutputModel extends AbstractOutputModel {
    /**
     * 数据集名称
     */
    private String name;
    /**
     * 标签
     */
    private String tags;
    /**
     * 描述
     */
    private String description;
    /**
     * 存储类型
     */
    private String storageType;
    /**
     * 命名空间
     */
    private String namespace;
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
     * 数据集的可见性
     */
    private DataSetPublicLevel publicLevel;
    /**
     * 使用次数
     */
    private Integer usageCountInJob = 0;
    /**
     * 使用次数
     */
    private Integer usageCountInFlow = 0;
    /**
     * 使用次数
     */
    private Integer usageCountInProject = 0;
    /**
     * 可见成员列表;只有在列表中的联邦成员才可以看到该数据集的基本信息
     */
    private String publicMemberList;

    private TreeMap<String, String> publicMemberInfoList = new TreeMap<>();
    /**
     * 来源类型，枚举（原始、对齐、分箱）
     */
    private ComponentType sourceType;
    /**
     * 来源任务id
     */
    private String sourceJobId;
    /**
     * 来源流程id
     */
    private String sourceFlowId;
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

    public List<String> getPublicMemberList() {
        if ("[\"*\"]".equals(publicMemberList) || "*".equals(publicMemberList)) {
            return null;
        }
        return StringUtil.splitWithoutEmptyItem(publicMemberList, ",");
    }

    public void setPublicMemberList(String publicMemberList) throws StatusCodeWithException {
        this.publicMemberList = publicMemberList;

        for (String id : StringUtil.splitWithoutEmptyItem(publicMemberList, ",")) {

            if ("[\"*\"]".equals(id) || "*".equals(id)) {
                continue;
            }

            String memberName = CacheObjects.getMemberName(id);
            if (memberName == null) {
                CacheObjects.refreshMemberMap();
                memberName = CacheObjects.getMemberName(id);
            }

            publicMemberInfoList.put(id, memberName);
        }

    }


    //region getter/setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

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

    public DataSetPublicLevel getPublicLevel() {
        return publicLevel;
    }

    public void setPublicLevel(DataSetPublicLevel publicLevel) {
        this.publicLevel = publicLevel;
    }

    public Integer getUsageCountInJob() {
        return usageCountInJob;
    }

    public void setUsageCountInJob(Integer usageCountInJob) {
        this.usageCountInJob = usageCountInJob;
    }

    public Integer getUsageCountInFlow() {
        return usageCountInFlow;
    }

    public void setUsageCountInFlow(Integer usageCountInFlow) {
        this.usageCountInFlow = usageCountInFlow;
    }

    public Integer getUsageCountInProject() {
        return usageCountInProject;
    }

    public void setUsageCountInProject(Integer usageCountInProject) {
        this.usageCountInProject = usageCountInProject;
    }

    public TreeMap<String, String> getPublicMemberInfoList() {
        return publicMemberInfoList;
    }

    public void setPublicMemberInfoList(TreeMap<String, String> publicMemberInfoList) {
        this.publicMemberInfoList = publicMemberInfoList;
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
