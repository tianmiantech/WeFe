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

package com.welab.wefe.mpc.config;

import java.util.UUID;

/**
 * 与服务通信配置
 *
 * @Author: eval
 * @Date: 2021-12-30
 **/
public class CommunicationConfig {

    private String requestId = UUID.randomUUID().toString().replaceAll("-", "");

    /**
     * 查询的接口名
     */
    private String apiName;

    /**
     * 服务器接口地址
     */
    private String serverUrl;

    /**
     * 商户id
     */
    private String commercialId;
    /**
     * 签名私钥
     */
    private String signPrivateKey;
    
    /**
     * 是否要返回结果标签
     * */
    private boolean needReturnFields;

    /**
     * 是否续跑
     */
    private boolean isContinue;

    /**
     * 是否需要签名
     */
    private boolean needSign = true;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getCommercialId() {
        return commercialId;
    }

    public void setCommercialId(String commercialId) {
        this.commercialId = commercialId;
    }

    public String getSignPrivateKey() {
        return signPrivateKey;
    }

    public void setSignPrivateKey(String signPrivateKey) {
        this.signPrivateKey = signPrivateKey;
    }

    public boolean isNeedSign() {
        return needSign;
    }

    public void setNeedSign(boolean needSign) {
        this.needSign = needSign;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public void setContinue(boolean isContinue) {
        this.isContinue = isContinue;
    }

    public boolean isNeedReturnFields() {
        return needReturnFields;
    }

    public void setNeedReturnFields(boolean needReturnFields) {
        this.needReturnFields = needReturnFields;
    }
}
