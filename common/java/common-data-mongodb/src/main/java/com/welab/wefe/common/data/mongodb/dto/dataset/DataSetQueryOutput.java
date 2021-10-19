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

package com.welab.wefe.common.data.mongodb.dto.dataset;

import com.welab.wefe.common.data.mongodb.entity.union.DataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetExtJSON;

/**
 * @author yuxin.zhang
 **/
public class DataSetQueryOutput {
    private String dataSetId;
    private String memberName;
    private String name;
    private String memberId;
    private String containsY;
    private String rowCount;
    private String columnCount;
    private String columnNameList;
    private String featureCount;
    private String featureNameList;
    private String publicLevel;
    private String publicMemberList;
    private String usageCountInJob;
    private String usageCountInFlow;
    private String usageCountInProject;
    private String description;
    private String tags;
    protected String createdTime;
    protected String updatedTime;

    private DataSetExtJSON extJson;


    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
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

    public String getContainsY() {
        return containsY;
    }

    public void setContainsY(String containsY) {
        this.containsY = containsY;
    }

    public String getRowCount() {
        return rowCount;
    }

    public void setRowCount(String rowCount) {
        this.rowCount = rowCount;
    }

    public String getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(String columnCount) {
        this.columnCount = columnCount;
    }

    public String getColumnNameList() {
        return columnNameList;
    }

    public void setColumnNameList(String columnNameList) {
        this.columnNameList = columnNameList;
    }

    public String getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(String featureCount) {
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

    public String getUsageCountInJob() {
        return usageCountInJob;
    }

    public void setUsageCountInJob(String usageCountInJob) {
        this.usageCountInJob = usageCountInJob;
    }

    public String getUsageCountInFlow() {
        return usageCountInFlow;
    }

    public void setUsageCountInFlow(String usageCountInFlow) {
        this.usageCountInFlow = usageCountInFlow;
    }

    public String getUsageCountInProject() {
        return usageCountInProject;
    }

    public void setUsageCountInProject(String usageCountInProject) {
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

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public DataSetExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(DataSetExtJSON extJson) {
        this.extJson = extJson;
    }
}
