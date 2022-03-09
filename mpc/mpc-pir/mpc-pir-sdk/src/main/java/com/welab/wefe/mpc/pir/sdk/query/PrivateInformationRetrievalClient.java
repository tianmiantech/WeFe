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

package com.welab.wefe.mpc.pir.sdk.query;

import com.welab.wefe.mpc.pir.flow.BasePrivateInformationRetrieval;
import com.welab.wefe.mpc.pir.protocol.ot.ObliviousTransferKey;
import com.welab.wefe.mpc.pir.request.QueryKeysRequest;
import com.welab.wefe.mpc.pir.request.QueryKeysResponse;
import com.welab.wefe.mpc.pir.request.QueryPIRResultsRequest;
import com.welab.wefe.mpc.pir.request.QueryPIRResultsResponse;
import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;
import com.welab.wefe.mpc.pir.sdk.crypt.CryptUtil;
import com.welab.wefe.mpc.pir.sdk.protocol.HauckObliviousTransferReceiver;
import com.welab.wefe.mpc.pir.sdk.trasfer.PrivateInformationRetrievalTransferVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用隐私信息检索协议查询用户数据
 *
 * @author eval
 */
public class PrivateInformationRetrievalClient extends BasePrivateInformationRetrieval {
    private static final Logger LOG = LoggerFactory.getLogger(PrivateInformationRetrievalClient.class);

    private PrivateInformationRetrievalTransferVariable mTransferVariable;

    private PrivateInformationRetrievalConfig mConfig;

    public PrivateInformationRetrievalClient(PrivateInformationRetrievalTransferVariable transferVariable,
                                             PrivateInformationRetrievalConfig config) {
        mTransferVariable = transferVariable;
        mConfig = config;
    }

    @Override
    public void initObliviousTransfer() {
    }

    private void initObliviousTransfer(String s) {
        this.mObliviousTransfer = new HauckObliviousTransferReceiver(uuid, s, mTransferVariable);
    }

    public String query() throws Exception {
        LOG.info("start query id");
        int targetIndex = mConfig.getTargetIndex();
        QueryKeysRequest request = new QueryKeysRequest();
        request.setIds(mConfig.getPrimaryKeys());
        QueryKeysResponse response = mTransferVariable.queryKeys(request);
        uuid = response.getUuid();

        initObliviousTransfer(response.getS());

        LOG.info("uuid:{} start key derivation", uuid);
        ObliviousTransferKey targetKey = mObliviousTransfer.keyDerivation(targetIndex).get(0);
        LOG.info("uuid:{} query results", uuid);
        if (!targetKey.getResult().isEmpty()) {
            return targetKey.getResult();
        }
        QueryPIRResultsRequest resultsRequest = new QueryPIRResultsRequest();
        resultsRequest.setUuid(uuid);
        QueryPIRResultsResponse resultsResponse = mTransferVariable.queryResults(resultsRequest);
        LOG.info("uuid:{} obtain results", uuid);
        return CryptUtil.decrypt(resultsResponse.getResults().get(targetIndex), targetKey.key);
    }

}
