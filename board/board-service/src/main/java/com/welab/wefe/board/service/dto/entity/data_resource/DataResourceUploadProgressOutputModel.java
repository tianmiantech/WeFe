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
package com.welab.wefe.board.service.dto.entity.data_resource;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.enums.DataResourceUploadStatus;

/**
 * @author zane
 * @date 2021/12/1
 */
public class DataResourceUploadProgressOutputModel extends AbstractBaseMySqlModel {
    /**
     * 数据资源id
     */
    private String dataResourceId;
    /**
     * 数据资源名称
     */
    private String dataResourceName;
    /**
     * 总数据行数
     */
    private Long totalDataCount;
    /**
     * 已写入数据行数
     */
    private Long completedDataCount;
    /**
     * 任务进度百分比
     */
    private Integer progressRatio;
    /**
     * 预计剩余耗时
     */
    private Integer estimateRemainingTime;
    /**
     * 无效数据量;主键重复条数）
     */
    private Integer invalidDataCount;
    /**
     * 错误消息
     */
    private String errorMessage;
    /**
     * 状态：上传中、已完成、已失败
     */
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

    public Long getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(Long totalDataCount) {
        this.totalDataCount = totalDataCount;
    }

    public Long getCompletedDataCount() {
        return completedDataCount;
    }

    public void setCompletedDataCount(Long completedDataCount) {
        this.completedDataCount = completedDataCount;
    }

    public Integer getProgressRatio() {
        return progressRatio;
    }

    public void setProgressRatio(Integer progressRatio) {
        this.progressRatio = progressRatio;
    }

    public Integer getEstimateRemainingTime() {
        return estimateRemainingTime;
    }

    public void setEstimateRemainingTime(Integer estimateRemainingTime) {
        this.estimateRemainingTime = estimateRemainingTime;
    }

    public Integer getInvalidDataCount() {
        return invalidDataCount;
    }

    public void setInvalidDataCount(Integer invalidDataCount) {
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
