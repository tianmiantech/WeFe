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

import com.welab.wefe.mpc.commom.Conversion;
import com.welab.wefe.mpc.pir.flow.BasePrivateInformationRetrieval;
import com.welab.wefe.mpc.pir.protocol.ot.ObliviousTransferKey;
import com.welab.wefe.mpc.pir.protocol.se.SymmetricKey;
import com.welab.wefe.mpc.pir.protocol.se.aes.AESDecryptKey;
import com.welab.wefe.mpc.pir.request.QueryKeysRequest;
import com.welab.wefe.mpc.pir.request.QueryKeysResponse;
import com.welab.wefe.mpc.pir.request.QueryPIRResultsRequest;
import com.welab.wefe.mpc.pir.request.QueryPIRResultsResponse;
import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;
import com.welab.wefe.mpc.pir.sdk.protocol.HauckObliviousTransferReceiver;
import com.welab.wefe.mpc.pir.sdk.trasfer.PrivateInformationRetrievalTransferVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

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
        LOG.info("send query keys");
        QueryKeysResponse response = mTransferVariable.queryKeys(request);
        uuid = response.getUuid();

        initObliviousTransfer(response.getS());

        LOG.info("uuid:{} start key derivation", uuid);
        ObliviousTransferKey targetKey = mObliviousTransfer.keyDerivation(targetIndex).get(0);
        LOG.info("uuid:{} query results", uuid);
        QueryPIRResultsRequest resultsRequest = new QueryPIRResultsRequest();
        resultsRequest.setUuid(uuid);
        QueryPIRResultsResponse resultsResponse = mTransferVariable.queryResults(resultsRequest);
        LOG.info("uuid:{} obtain results", uuid);
        String enResults = resultsResponse.getResults().get(targetIndex);
        String[] realResult = enResults.split(",");
        byte[] enResult = Conversion.hexStringToBytes(realResult[0]);
        byte[] iv = Conversion.hexStringToBytes(realResult[1]);
        SymmetricKey aesKey = new AESDecryptKey(targetKey.key, iv);
        byte[] result = aesKey.encrypt(enResult);
        String resultValue = new String(result, Charset.defaultCharset());
        LOG.info("uuid:{}, result:{} finish", uuid, resultValue);
        return resultValue;
    }

}
