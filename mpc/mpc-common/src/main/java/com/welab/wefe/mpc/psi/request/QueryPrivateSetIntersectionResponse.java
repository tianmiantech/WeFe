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

/**
 * @Author: eval
 * @Date: 2021-12-24
 **/
public class QueryPrivateSetIntersectionResponse {

    private String requestId;
    private List<String> serverEncryptIds;
    private List<String> clientIdByServerKeys;
    private List<String> fieldResults;

    /**
     * 当前批次
     */
    private int currentBatch;
    /**
     * 是否有下一个批次
     */
    private boolean hasNext;

    private String message;
    private int code;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<String> getServerEncryptIds() {
        return serverEncryptIds;
    }

    public void setServerEncryptIds(List<String> serverEncryptIds) {
        this.serverEncryptIds = serverEncryptIds;
    }

    public List<String> getClientIdByServerKeys() {
        return clientIdByServerKeys;
    }

    public void setClientIdByServerKeys(List<String> clientIdByServerKeys) {
        this.clientIdByServerKeys = clientIdByServerKeys;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCurrentBatch() {
        return currentBatch;
    }

    public void setCurrentBatch(int currentBatch) {
        this.currentBatch = currentBatch;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public List<String> getFieldResults() {
        return fieldResults;
    }

    public void setFieldResults(List<String> fieldResults) {
        this.fieldResults = fieldResults;
    }
}
