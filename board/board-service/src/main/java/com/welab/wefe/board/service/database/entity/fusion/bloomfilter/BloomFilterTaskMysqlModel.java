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

package com.welab.wefe.board.service.database.entity.fusion.bloomfilter;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;

import javax.persistence.Entity;

/**
 * @author jacky.jiang
 */
@Entity(name = "bloom_filter_task")
public class BloomFilterTaskMysqlModel extends AbstractBaseMySqlModel {

    /**
     * 过滤器名
     */
    private String bloomFilterName;

    /**
     * 过滤器id
     */
    private String bloomFilterId;

    /**
     * 总数据行数
     */
    private long totalRowCount = 0;
    /**
     * 已写入数据行数
     */
    private long addedRowCount = 0;

    /**
     * 任务进度百分比
     */
    private int progress;

    /**
     * 预计剩余耗时
     */
    private long estimateTime;

    /**
     * 主键重复条数
     */
    private long repeatIdRowCount;
    /**
     * 错误消息
     */
    private String errorMessage;

    // region getter/setter

    public String getBloomfilterName() {
        return bloomFilterName;
    }

    public void setBloomfilterName(String bloomfilterName) {
        this.bloomFilterName = bloomfilterName;
    }

    public String getBloomFilterName() {
        return bloomFilterName;
    }

    public void setBloomFilterName(String bloomFilterName) {
        this.bloomFilterName = bloomFilterName;
    }

    public String getBloomFilterId() {
        return bloomFilterId;
    }

    public void setBloomFilterId(String bloomFilterId) {
        this.bloomFilterId = bloomFilterId;
    }

    public long getTotalRowCount() {
        return totalRowCount;
    }

    public void setTotalRowCount(long totalRowCount) {
        this.totalRowCount = totalRowCount;
    }

    public long getAddedRowCount() {
        return addedRowCount;
    }

    public void setAddedRowCount(long addedRowCount) {
        this.addedRowCount = addedRowCount;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getEstimateTime() {
        return estimateTime;
    }

    public void setEstimateTime(long estimateTime) {
        this.estimateTime = estimateTime;
    }

    public long getRepeatIdRowCount() {
        return repeatIdRowCount;
    }

    public void setRepeatIdRowCount(long repeatIdRowCount) {
        this.repeatIdRowCount = repeatIdRowCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // endregion
}
