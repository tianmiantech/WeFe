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

package com.welab.wefe.mpc.pir.server.flow;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.commom.Conversion;
import com.welab.wefe.mpc.pir.flow.BasePrivateInformationRetrieval;
import com.welab.wefe.mpc.pir.protocol.ot.ObliviousTransferKey;
import com.welab.wefe.mpc.pir.protocol.se.SymmetricKey;
import com.welab.wefe.mpc.pir.protocol.se.aes.AESEncryptKey;
import com.welab.wefe.mpc.pir.server.protocol.HauckObliviousTransferSender;
import com.welab.wefe.mpc.pir.server.trasfer.PrivateInformationRetrievalTransferVariable;
import com.welab.wefe.mpc.pir.server.trasfer.impl.CacheTransferVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author eval
 */
public class PrivateInformationRetrievalFlowServer extends BasePrivateInformationRetrieval {
    private static final Logger LOG = LoggerFactory.getLogger(PrivateInformationRetrievalFlowServer.class);

    PrivateInformationRetrievalTransferVariable mTransferVariable = new CacheTransferVariable();

    @Override
    public void setUuid(String uuid) {
        super.setUuid(uuid);
        initObliviousTransfer();
    }

    @Override
    public void initObliviousTransfer() {
        mObliviousTransfer = new HauckObliviousTransferSender(uuid);
    }

    public void process(List<Object> ids, String idCryptMethod) {
        int num = ids.size();
        LOG.info("uuid:{} start process data size:{}", uuid, num);
        CacheOperation<Map<String, String>> queryDataResult = CacheOperationFactory.getCacheOperation();
        CompletableFuture<Map<String, String>> cf = CompletableFuture.supplyAsync(
                () -> {
                    Map<String, String> result = queryDataResult.get(uuid, Constants.RESULT);
                    while (ObjectUtil.isNull(result)) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            LOG.error(e.getMessage(), e);
                        }
                        result = queryDataResult.get(uuid, Constants.RESULT);
                    }
                    LOG.info("get result finish");
                    return result;
                });
        CompletableFuture<List<ObliviousTransferKey>> keyFuture = CompletableFuture.supplyAsync(
                () -> mObliviousTransfer.keyDerivation(num));
        LOG.info("uuid:{} keyDerivation finish", uuid);
        CompletableFuture.allOf(cf, keyFuture).join();
        Map<String, String> results = null;
        List<ObliviousTransferKey> keyList = null;
        try {
            results = cf.get();
            keyList = keyFuture.get();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info("uuid:{} encrypt results", uuid);
        List<String> enResults = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            SymmetricKey aesKey = new AESEncryptKey(keyList.get(i).key);
            byte[] enResult = aesKey.encrypt(results.getOrDefault(ids.get(i), "").getBytes());
            String value = Conversion.bytesToHexString(enResult) + "," + Conversion.bytesToHexString(aesKey.getIv());
            enResults.add(value);
        }
        LOG.info("uuid:{} send results", uuid);
        mTransferVariable.processResult(uuid, JSON.toJSONString(enResults));
        LOG.info("uuid:{} finish", uuid);
    }
}
