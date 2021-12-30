
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

package com.welab.wefe.mpc.pir.sdk.trasfer.impl;

import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.PrivateInformationRetrievalApiName;
import com.welab.wefe.mpc.pir.request.*;
import com.welab.wefe.mpc.pir.sdk.trasfer.PrivateInformationRetrievalTransferVariable;
import com.welab.wefe.mpc.trasfer.AbstractHttpTransferVariable;

/**
 * @author eval
 */
public class HttpTransferVariable extends AbstractHttpTransferVariable implements PrivateInformationRetrievalTransferVariable {

    private CommunicationConfig mConfig;

    public HttpTransferVariable(CommunicationConfig config) {
        mConfig = config;
    }

    @Override
    public QueryRandomResponse queryRandom(QueryRandomRequest request) {
        return query(request, PrivateInformationRetrievalApiName.RANDOM, QueryRandomResponse.class);
    }

    @Override
    public QueryRandomLegalResponse queryRandomLegal(QueryRandomLegalRequest request) {
        return query(request, PrivateInformationRetrievalApiName.RANDOM_LEGAL, QueryRandomLegalResponse.class);
    }

    @Override
    public QueryKeysResponse queryKeys(QueryKeysRequest request) {
        return query(request, mConfig.getApiName(), QueryKeysResponse.class);
    }

    @Override
    public QueryPIRResultsResponse queryResults(QueryPIRResultsRequest request) {
        return query(request, PrivateInformationRetrievalApiName.RESULTS, QueryPIRResultsResponse.class);
    }

    private <T> T query(Object request, String apiName, Class<T> clz) {
        return query(request, apiName, mConfig, clz);
    }

}
