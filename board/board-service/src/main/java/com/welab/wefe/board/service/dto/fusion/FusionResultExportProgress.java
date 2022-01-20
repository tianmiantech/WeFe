package com.welab.wefe.board.service.dto.fusion;

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


/**
 * @author hunter.zhao
 */
public class FusionResultExportProgress {
    String business;

    int progress;

    int totalDataCount;

    int processedCount;

    public FusionResultExportProgress(String business, int totalDataCount) {
        this.business = business;
        this.totalDataCount = totalDataCount;
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
    }
}
