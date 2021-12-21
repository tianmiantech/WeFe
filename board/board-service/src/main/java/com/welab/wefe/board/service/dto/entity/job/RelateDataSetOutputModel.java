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

package com.welab.wefe.board.service.dto.entity.job;


import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

/**
 * @author lonnie
 */
public class RelateDataSetOutputModel extends AbstractApiOutput {

    @Check(name = "任务 Id")
    private String businessId;
    @Check(name = "成员 Id")
    private String memberId;
    @Check(name = "成员名称")
    private String memberName;
    @Check(name = "在任务中的角色;枚举（promoter/provider/arbiter）")
    private JobMemberRole jobRole;
    @Check(name = "数据集名称")
    private String dataSetName;
    @Check(name = "数据量")
    private Long dataSetRows;
    @Check(name = "特征列")
    private String featureColumnList;
    @Check(name = "主键列")
    private String primaryKeyColumn;

    @Check(name = "字段列表")
    private String columnNameList;

    @Check(name = "来源数据集id")
    private String sourceJobId;

    @Check(name = "是否包含 Y 值")
    private Boolean containsY;

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public JobMemberRole getJobRole() {
        return jobRole;
    }

    public void setJobRole(JobMemberRole jobRole) {
        this.jobRole = jobRole;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public Long getDataSetRows() {
        return dataSetRows;
    }

    public void setDataSetRows(Long dataSetRows) {
        this.dataSetRows = dataSetRows;
    }

    public String getFeatureColumnList() {
        return featureColumnList;
    }

    public void setFeatureColumnList(String featureColumnList) {
        this.featureColumnList = featureColumnList;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(String primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public String getColumnNameList() {
        return columnNameList;
    }

    public void setColumnNameList(String columnNameList) {
        this.columnNameList = columnNameList;
    }

    public String getSourceJobId() {
        return sourceJobId;
    }

    public void setSourceJobId(String sourceJobId) {
        this.sourceJobId = sourceJobId;
    }

    public Boolean getContainsY() {
        return containsY;
    }

    public void setContainsY(Boolean containsY) {
        this.containsY = containsY;
    }
}
