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

package com.welab.wefe.board.service.dto.entity.job.gateway;


import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.AuditStatus;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;


/**
 * @author zane.luo
 */
public class ProjectForGatewayDataSetOutputModel {
    @Check(name = "成员id")
    private String memberId;

    @Check(name = "成员id")
    private String memberName;

    @Check(name = "数据集 Id")
    private String dataSetId;
    @Check(name = "数据集名称")
    private String dataSetName;
    @Check(name = "数据量")
    private Long dataSetRows;
    @Check(name = "关键字")
    private String dataSetKeys;

    @Check(name = "数据集列数")
    private long dataSetColumnNum;

    @Check(name = "是否包含 Y 值")
    private boolean containsY;

    @Check(name = "特征列")
    private String featureColumnList;

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    private AuditStatus dataSetStatus;
    @Check(name = "状态更新时间")
    private Date statusUpdatedTime;

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

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
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

    public String getDataSetKeys() {
        return dataSetKeys;
    }

    public void setDataSetKeys(String dataSetKeys) {
        this.dataSetKeys = dataSetKeys;
    }

    public long getDataSetColumnNum() {
        return dataSetColumnNum;
    }

    public void setDataSetColumnNum(long dataSetColumnNum) {
        this.dataSetColumnNum = dataSetColumnNum;
    }

    public boolean isContainsY() {
        return containsY;
    }

    public void setContainsY(boolean containsY) {
        this.containsY = containsY;
    }

    public String getFeatureColumnList() {
        return featureColumnList;
    }

    public void setFeatureColumnList(String featureColumnList) {
        this.featureColumnList = featureColumnList;
    }

    public AuditStatus getDataSetStatus() {
        return dataSetStatus;
    }

    public void setDataSetStatus(AuditStatus dataSetStatus) {
        this.dataSetStatus = dataSetStatus;
    }

    public Date getStatusUpdatedTime() {
        return statusUpdatedTime;
    }

    public void setStatusUpdatedTime(Date statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
    }

}
