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

package com.welab.wefe.mpc.sa.sdk;

import com.welab.wefe.mpc.key.DiffieHellmanKey;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyRequest;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyResponse;
import com.welab.wefe.mpc.sa.request.QuerySAResultRequest;
import com.welab.wefe.mpc.sa.request.QuerySAResultResponse;
import com.welab.wefe.mpc.sa.sdk.config.ServerConfig;
import com.welab.wefe.mpc.sa.sdk.transfer.SecureAggregationTransferVariable;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author eval
 */
public class SecureAggregation {

    public Double query(List<ServerConfig> serverConfigs, List<SecureAggregationTransferVariable> transferVariables) {
        Double result = 0.0;
        String uuid = UUID.randomUUID().toString().replace("-", "");
        DiffieHellmanKey dhKey = DiffieHellmanUtil.generateKey(1024);

        List<String> diffieHellmanValues = new ArrayList<>(serverConfigs.size());
        for (int i = 0; i < serverConfigs.size(); i++) {
            ServerConfig serverConfig = serverConfigs.get(i);
            QueryDiffieHellmanKeyRequest request = new QueryDiffieHellmanKeyRequest();
            request.setUuid(uuid);
            request.setP(dhKey.getP().toString(16));
            request.setG(dhKey.getG().toString(16));
            request.setQueryParams(serverConfig.getQueryParams());
            QueryDiffieHellmanKeyResponse response = transferVariables.get(i).queryDiffieHellmanKey(request);
            diffieHellmanValues.add(response.getDiffieHellmanValue());
        }

        for (int i = 0; i < serverConfigs.size(); i++) {
            ServerConfig serverConfig = serverConfigs.get(i);
            QuerySAResultRequest saResultRequest = new QuerySAResultRequest();
            saResultRequest.setUuid(uuid);
            saResultRequest.setDiffieHellmanValues(diffieHellmanValues);
            saResultRequest.setIndex(i);
            saResultRequest.setP(dhKey.getP().toString(16));
            saResultRequest.setOperator(serverConfig.getOperator());
            saResultRequest.setWeight(serverConfig.getWeight());
            QuerySAResultResponse response = transferVariables.get(i).queryResult(saResultRequest);
            result += response.getResult();
        }

        return result;
    }

}
