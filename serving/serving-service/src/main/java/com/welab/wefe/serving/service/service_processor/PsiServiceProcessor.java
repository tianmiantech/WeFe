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
package com.welab.wefe.serving.service.service_processor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionRequest;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.psi.sdk.ecdh.EcdhPsiServer;
import com.welab.wefe.mpc.psi.sdk.util.EcdhUtil;
import com.welab.wefe.mpc.psi.sdk.util.PartitionUtil;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;

/**
 * @author hunter.zhao
 */
public class PsiServiceProcessor extends AbstractServiceProcessor<TableServiceMySqlModel> {

    private static final ConcurrentHashMap<String, EcdhPsiServer> ECDH_SERVER_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Map<Long, String>> CLIENT_IDS_MAP = new ConcurrentHashMap<>();
    private static final List<String> serverDataset;
    private static final int BATCH_SIZE = 500000;

    static {
        serverDataset = new ArrayList<>();
        for (long i = 0; i < 1000000; i++) {
            serverDataset.add("SERVER-ONLY-" + i);
            if (i % 10000 == 0) {
                serverDataset.add("MATCHING-" + i);
            }
        }
    }

    @Override
    public JObject process(JObject data, TableServiceMySqlModel model) throws StatusCodeWithException {
        String idsTableName = model.getIdsTableName();
        // ID在mysql表中
        if (!idsTableName.contains("#")) {
            String p = data.getString("p");
            List<String> clientIds = JObject.parseArray(data.getString("client_ids"), String.class);
            if (CollectionUtils.isEmpty(clientIds)) {
                clientIds = JObject.parseArray(data.getString("clientIds"), String.class);
            }
            QueryPrivateSetIntersectionRequest request = new QueryPrivateSetIntersectionRequest();
            request.setClientIds(clientIds);
            request.setP(p);
            QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
            BigInteger mod = new BigInteger(request.getP(), 16);
            int keySize = 1024;
            BigInteger serverKey = new BigInteger(keySize, new Random());
            JSONObject dataSource = JObject.parseObject(model.getDataSource());
            String sql = "select id from " + model.getIdsTableName();
            List<String> needFields = new ArrayList<>();
            needFields.add("id");

            DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSource.getString("id"));
            if (dataSourceModel == null) {
                return JObject.create(response);
            }

            List<Map<String, String>> result = dataSourceService.queryList(dataSourceModel, sql, needFields);
            List<String> serverIds = new ArrayList<>();
            for (Map<String, String> item : result) {
                serverIds.add(item.get("id"));
            }
            List<String> encryptServerIds = new ArrayList<>(serverIds.size());
            serverIds.forEach(
                    serverId -> encryptServerIds.add(DiffieHellmanUtil.encrypt(serverId, serverKey, mod).toString(16)));
            response.setServerEncryptIds(encryptServerIds);

            List<String> encryptClientIds = new ArrayList<>(request.getClientIds().size());
            request.getClientIds().forEach(
                    id -> encryptClientIds.add(DiffieHellmanUtil.encrypt(id, serverKey, mod, false).toString(16)));
            response.setClientIdByServerKeys(encryptClientIds);
            return JObject.create(response);
        } else {
            String databaseType = idsTableName.split("#")[0];
            String dbConnUrl = idsTableName.split("#")[1];
            String requestId = data.getString("requestId");
            String callbackUrl = data.getString("callback_url");
            // 当前批次
            int currentBatch = data.getIntValue("currentBatch");
            Map<Long, String> doubleEncryptedClientDatasetMap = null;
            EcdhPsiServer server = ECDH_SERVER_MAP.get(requestId);
            if (server == null) {
                LOG.info("ecdh server not found, new one");
                server = new EcdhPsiServer();
                ECDH_SERVER_MAP.put(requestId, server);
            }
            if (CLIENT_IDS_MAP.get(requestId) != null) {
                LOG.info("get clientIds in cache");
                doubleEncryptedClientDatasetMap = CLIENT_IDS_MAP.get(requestId);
            } else {
                List<String> clientIds = JObject.parseArray(data.getString("client_ids"), String.class);
                if (CollectionUtils.isEmpty(clientIds)) {
                    clientIds = JObject.parseArray(data.getString("clientIds"), String.class);
                }
                if (CollectionUtils.isEmpty(clientIds)) {
                    QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
                    response.setCode(-1);
                    response.setMessage("client_ids is empty");
                    return JObject.create(response);
                }
                Map<Long, String> clientEncryptedDatasetMap = EcdhUtil.convert2Map(clientIds);
                // 对客户端数据进行二次加密
                doubleEncryptedClientDatasetMap = server.encryptDatasetMap(clientEncryptedDatasetMap);
                CLIENT_IDS_MAP.put(requestId, doubleEncryptedClientDatasetMap);
            }
            Set<String> batchData = getBatchData(currentBatch);
            List<String> doubleEncryptedClientDataset = EcdhUtil.convert2List(doubleEncryptedClientDatasetMap);
            // 服务端对自己的数据集进行加密
            List<String> serverEncryptedDataset = server.encryptDataset(new ArrayList<>(batchData));
            // 把上面两个set发给客户端
            QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
            response.setClientIdByServerKeys(doubleEncryptedClientDataset);
            response.setRequestId(requestId);
            response.setServerEncryptIds(serverEncryptedDataset);
            response.setCurrentBatch(currentBatch);
            response.setHasNext(currentBatch < getNumPartitions() - 1);
            response.setSplitData(isSplitData());
            return JObject.create(response);

        }
    }

    private Set<String> getBatchData(int currentBatch) {
        int numPartitions = serverDataset.size() / BATCH_SIZE;
        List<Set<String>> partitions = PartitionUtil.partitionList(serverDataset, numPartitions);
        return partitions.get(currentBatch);
    }

    /**
     * 是否需要分批求交
     */
    private boolean isSplitData() {
        int numPartitions = serverDataset.size() / BATCH_SIZE;
        if (numPartitions <= 0) {
            numPartitions = 1;
        }
        return numPartitions > 1;
    }

    /**
     * 获取服务端数据总批次数
     */
    private int getNumPartitions() {
        int numPartitions = serverDataset.size() / BATCH_SIZE;
        return numPartitions;
    }
}
