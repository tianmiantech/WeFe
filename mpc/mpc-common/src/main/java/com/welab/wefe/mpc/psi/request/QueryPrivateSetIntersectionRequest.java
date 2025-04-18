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

package com.welab.wefe.mpc.psi.request;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @Author: eval
 * @Date: 2021-12-24
 **/
public class QueryPrivateSetIntersectionRequest {
    private String p;

    @JSONField(name = "client_ids")
    private List<String> clientIds;

    private String requestId;

    /**
     * 当前批次
     */
    private int currentBatch;

    /**
     * 批次大小
     */
    private int batchSize;

    /**
     * psi 类型
     */
    private String type; // Psi

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public List<String> getClientIds() {
        return clientIds;
    }

    public void setClientIds(List<String> clientIds) {
        this.clientIds = clientIds;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getCurrentBatch() {
        return currentBatch;
    }

    public void setCurrentBatch(int currentBatch) {
        this.currentBatch = currentBatch;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public String toString() {
        return "QueryPrivateSetIntersectionRequest [p=" + p + ", requestId=" + requestId + ", currentBatch="
                + currentBatch + ", batchSize=" + batchSize + ", type=" + type + "]";
    }

}
