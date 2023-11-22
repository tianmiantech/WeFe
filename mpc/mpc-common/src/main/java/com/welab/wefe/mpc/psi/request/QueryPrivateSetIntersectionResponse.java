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

    private String uuid;
    private List<String> serverEncryptIds;
    private List<String> clientIdByServerKeys;
    private String message;
    private int code;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
}
