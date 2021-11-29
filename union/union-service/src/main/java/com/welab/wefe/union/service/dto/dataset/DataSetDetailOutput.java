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

package com.welab.wefe.union.service.dto.dataset;

import com.welab.wefe.common.enums.DataSetType;
import com.welab.wefe.common.web.dto.AbstractTimedApiOutput;

/**
 * @author Jervis
 */
public class DataSetDetailOutput extends AbstractTimedApiOutput {
    private String id;
    private String name;
    private String memberId;
    private int containsY;
    private Long rowCount;
    private Integer columnCount;
    private String columnNameList;
    private Integer featureCount;
    private String featureNameList;
    private String publicLevel;
    private String publicMemberList;
    private Integer usageCountInJob = -1;
    private Integer usageCountInFlow = -1;
    private Integer usageCountInProject = -1;
    private String description;
    private String tags;
    private long logTime;
    private String dataSetType = DataSetType.TableDataSet.name();

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", memberId='" + memberId + '\'' +
                ", containsY=" + containsY +
                ", rowCount=" + rowCount +
                ", columnCount=" + columnCount +
                ", columnNameList='" + columnNameList + '\'' +
                ", featureCount=" + featureCount +
                ", featureNameList='" + featureNameList + '\'' +
                ", publicLevel='" + publicLevel + '\'' +
                ", publicMemberList='" + publicMemberList + '\'' +
                ", usageCountInJob=" + usageCountInJob +
                ", usageCountInFlow=" + usageCountInFlow +
                ", usageCountInProject=" + usageCountInProject +
                ", description='" + description + '\'' +
                ", tags='" + tags + '\'' +
                ", logTime=" + logTime +
                ", dataSetType='" + dataSetType + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public int getContainsY() {
        return containsY;
    }

    public void setContainsY(int containsY) {
        this.containsY = containsY;
    }

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
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

    public String getPublicLevel() {
        return publicLevel;
    }

    public void setPublicLevel(String publicLevel) {
        this.publicLevel = publicLevel;
    }

    public String getPublicMemberList() {
        return publicMemberList;
    }

    public void setPublicMemberList(String publicMemberList) {
        this.publicMemberList = publicMemberList;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getLogTime() {
        return logTime;
    }

    public void setLogTime(long logTime) {
        this.logTime = logTime;
    }

    public String getDataSetType() {
        return dataSetType;
    }

    public void setDataSetType(String dataSetType) {
        this.dataSetType = dataSetType;
    }
}
