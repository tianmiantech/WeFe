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

package com.welab.wefe.serving.sdk.dto;

/**
 * @author hunter.zhao
 */
public class FederatedParams {

    private String modelId;

    private String partnerCode;

    private FederatedParams() {
    }

    public FederatedParams(String modelId, String partnerCode) {
        this.modelId = modelId;
        this.partnerCode = partnerCode;
    }

    public static FederatedParams of(String modelId, String partnerCode) {
        FederatedParams federatedParams = new FederatedParams();
        federatedParams.modelId = modelId;
        federatedParams.partnerCode = partnerCode;
        return federatedParams;
    }

    public String getModelId() {
        return modelId;
    }

    public FederatedParams setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public FederatedParams setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
        return this;
    }
}
