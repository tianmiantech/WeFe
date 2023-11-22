
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

package com.welab.wefe.mpc.sa.sdk.transfer.impl;

import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.sa.SecureAggregationApiName;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyRequest;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyResponse;
import com.welab.wefe.mpc.sa.request.QuerySAResultRequest;
import com.welab.wefe.mpc.sa.request.QuerySAResultResponse;
import com.welab.wefe.mpc.sa.sdk.config.ServerConfig;
import com.welab.wefe.mpc.sa.sdk.transfer.SecureAggregationTransferVariable;
import com.welab.wefe.mpc.trasfer.AbstractHttpTransferVariable;

/**
 * @author eval
 */
public class HttpTransferVariable extends AbstractHttpTransferVariable implements SecureAggregationTransferVariable {

    ServerConfig config;

    public HttpTransferVariable(ServerConfig config) {
        this.config = config;
        CommunicationConfig communicationConfig = config.getCommunicationConfig();
        if (communicationConfig == null) {
            communicationConfig = new CommunicationConfig();
            communicationConfig.setServerUrl(config.getServerUrl());
            communicationConfig.setApiName(config.getServerName());
            communicationConfig.setNeedSign(false);
            config.setCommunicationConfig(communicationConfig);
        }
    }

    @Override
    public QueryDiffieHellmanKeyResponse queryDiffieHellmanKey(QueryDiffieHellmanKeyRequest request) {
        return query(request, config.getServerName(), QueryDiffieHellmanKeyResponse.class);
    }

    @Override
    public QuerySAResultResponse queryResult(QuerySAResultRequest request) {
        return query(request, SecureAggregationApiName.SA_RESULT, QuerySAResultResponse.class);
    }

    private <T> T query(Object request, String apiName, Class<T> clz) {
        return query(request, apiName, config.getCommunicationConfig(), clz);
    }

}
