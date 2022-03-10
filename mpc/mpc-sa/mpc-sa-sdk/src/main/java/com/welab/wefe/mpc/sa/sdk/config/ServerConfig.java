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

package com.welab.wefe.mpc.sa.sdk.config;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.commom.Operator;
import com.welab.wefe.mpc.config.CommunicationConfig;

/**
 * @Author: eval
 * @Date: 2021-12-22
 **/
public class ServerConfig {
    /**
     * 服务名称
     */
    private String serverName;
    /**
     * 服务url
     */
    private String serverUrl;
    /**
     * 服务操作方法，ADD：加法，SUB：减法
     */
    private Operator operator = Operator.ADD;
    /**
     * 服务权重
     */
    private float weight = 1.0f;

    /**
     * 服务查询参数
     */
    private JSONObject queryParams;

    /**
     * 与服务器通信配置
     */
    private CommunicationConfig communicationConfig;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public JSONObject getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(JSONObject queryParams) {
        this.queryParams = queryParams;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public CommunicationConfig getCommunicationConfig() {
        return communicationConfig;
    }

    public void setCommunicationConfig(CommunicationConfig communicationConfig) {
        this.communicationConfig = communicationConfig;
    }
}
