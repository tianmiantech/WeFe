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
package com.welab.wefe.board.service.dto.fusion;



import com.welab.wefe.board.service.fusion.enums.ExportStatus;

/**
 * @author hunter.zhao
 */
public class FusionResultExportProgress {
    String businessId;

    String tableName;

    int progress;

    int totalDataCount;

    int processedCount;

    ExportStatus status;

    long finishTime;

    public FusionResultExportProgress() {
    }

    public FusionResultExportProgress(String businessId, String tableName, int totalDataCount) {
        this.businessId = businessId;
        this.totalDataCount = totalDataCount;
        this.tableName = tableName;
        this.status = ExportStatus.exporting;
    }

    public int getProgress() {
        return Double.valueOf(
                Double.valueOf(processedCount) / Double.valueOf(totalDataCount) * 100
        ).intValue();
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(int totalDataCount) {
        this.totalDataCount = totalDataCount;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(int processedCount) {
        this.processedCount = processedCount;
    }

    public synchronized void increment() {
        processedCount++;

        if (processedCount == totalDataCount) {
            this.finishTime = System.currentTimeMillis();
            this.status = ExportStatus.success;
        }
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ExportStatus getStatus() {
        return status;
    }

    public void setStatus(ExportStatus status) {
        this.status = status;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }
}
