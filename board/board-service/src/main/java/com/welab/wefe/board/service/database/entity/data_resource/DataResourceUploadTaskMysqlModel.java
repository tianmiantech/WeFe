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

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.DataResourceUploadStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * @author zane
 * @date 2021/12/1
 */
@Entity(name = "data_resource_upload_task")
@Table(name = "data_resource_upload_task")
public class DataResourceUploadTaskMysqlModel extends AbstractBaseMySqlModel {
    /**
     * 数据资源id
     */
    private String dataResourceId;
    /**
     * 数据资源名称
     */
    private String dataResourceName;
    /**
     * 资源类型
     */
    private DataResourceType dataResourceType;
    /**
     * 总数据行数
     */
    private Long totalDataCount;
    /**
     * 已写入数据行数
     */
    private long completedDataCount;
    /**
     * 任务进度百分比
     */
    private Integer progressRatio;
    /**
     * 预计剩余耗时
     */
    private long estimateRemainingTime;
    /**
     * 无效数据量;主键重复条数）
     */
    private long invalidDataCount;
    /**
     * 错误消息
     */
    private String errorMessage;
    /**
     * 状态：上传中、已完成、已失败
     */
    @Enumerated(EnumType.STRING)
    private DataResourceUploadStatus status;

    // region getter/setter

    public String getDataResourceId() {
        return dataResourceId;
    }

    public void setDataResourceId(String dataResourceId) {
        this.dataResourceId = dataResourceId;
    }

    public String getDataResourceName() {
        return dataResourceName;
    }

    public void setDataResourceName(String dataResourceName) {
        this.dataResourceName = dataResourceName;
    }

    public DataResourceType getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(DataResourceType dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    public Long getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(Long totalDataCount) {
        this.totalDataCount = totalDataCount;
    }

    public long getCompletedDataCount() {
        return completedDataCount;
    }

    public void setCompletedDataCount(long completedDataCount) {
        this.completedDataCount = completedDataCount;
    }

    public Integer getProgressRatio() {
        return progressRatio;
    }

    public void setProgressRatio(Integer progressRatio) {
        this.progressRatio = progressRatio;
    }

    public long getEstimateRemainingTime() {
        return estimateRemainingTime;
    }

    public void setEstimateRemainingTime(long estimateRemainingTime) {
        this.estimateRemainingTime = estimateRemainingTime;
    }

    public long getInvalidDataCount() {
        return invalidDataCount;
    }

    public void setInvalidDataCount(long invalidDataCount) {
        this.invalidDataCount = invalidDataCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public DataResourceUploadStatus getStatus() {
        return status;
    }

    public void setStatus(DataResourceUploadStatus status) {
        this.status = status;
    }

    // endregion
}
