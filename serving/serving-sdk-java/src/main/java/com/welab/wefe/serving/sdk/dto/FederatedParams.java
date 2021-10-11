/**
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

package com.welab.wefe.serving.sdk.dto;

import java.util.List;

/**
 * @author hunter.zhao
 */
public class FederatedParams {

    private String seqNo;

    private String modelId;

    private String memberId;

    private List<ProviderParams> providers;

    private FederatedParams() {
    }

    public FederatedParams(String seqNo, String modelId, String memberId) {
        this.seqNo = seqNo;
        this.modelId = modelId;
        this.memberId = memberId;
    }

    public static FederatedParams of(String seqNo, String modelId, String memberId) {
        FederatedParams federatedParams = new FederatedParams();
        federatedParams.seqNo = seqNo;
        federatedParams.modelId = modelId;
        federatedParams.memberId = memberId;
        return federatedParams;
    }

    public static FederatedParams of(String seqNo, String modelId, String memberId, List<ProviderParams> providers) {
        FederatedParams federatedParams = new FederatedParams();
        federatedParams.seqNo = seqNo;
        federatedParams.modelId = modelId;
        federatedParams.memberId = memberId;
        federatedParams.providers = providers;
        return federatedParams;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public List<ProviderParams> getProviders() {
        return providers;
    }

    public void setProviders(List<ProviderParams> providers) {
        this.providers = providers;
    }
}
