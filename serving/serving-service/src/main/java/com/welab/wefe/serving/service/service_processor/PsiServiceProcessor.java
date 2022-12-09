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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.psi.sdk.dh.DhPsiServer;
import com.welab.wefe.mpc.psi.sdk.ecdh.EcdhPsiServer;
import com.welab.wefe.mpc.psi.sdk.util.EcdhUtil;
import com.welab.wefe.mpc.psi.sdk.util.PartitionUtil;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;

/**
 * @author hunter.zhao
 */
public class PsiServiceProcessor extends AbstractServiceProcessor<TableServiceMySqlModel> {

    private static final ConcurrentHashMap<String, EcdhPsiServer> ECDH_SERVER_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, DhPsiServer> DH_SERVER_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> CLIENT_IDS_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> SERVER_DATASET_SIZE = new ConcurrentHashMap<>();
    private static List<String> ECDH_SERVER_DATA_SET;
    private static final int BATCH_SIZE = 200000;
    private String type; // psi 算法类型 dh or ecdh
    private int numPartitions; // 服务端数据批次
    private String requestId;

    static {
        int total = 1000000;
        ECDH_SERVER_DATA_SET = new ArrayList<>();
        for (long i = 0; i < total; i++) {
            ECDH_SERVER_DATA_SET.add("SERVER-ONLY-" + i);
            if (i % (total / 100) == 0) {
                ECDH_SERVER_DATA_SET.add("MATCHING-" + i);
            }
        }
    }

    @Override
    public JObject process(JObject data, TableServiceMySqlModel model) throws StatusCodeWithException {
        String idsTableName = model.getIdsTableName();
        String rid = data.getString("requestId");
        this.requestId = rid;
        // 当前批次
        int currentBatch = data.getIntValue("currentBatch");
        // ID在mysql表中
        if (!idsTableName.contains("#")) {
            type = "DH";
            String p = data.getString("p");
            // 当前批次
            DhPsiServer server = DH_SERVER_MAP.get(requestId);
            List<String> encryptClientIds = null;
            if (server == null) {
                LOG.info("dh server not found, new one");
                server = new DhPsiServer(p);
                DH_SERVER_MAP.put(requestId, server);
            }
            if (CLIENT_IDS_MAP.get(requestId) == null) {
                List<String> clientIds = JObject.parseArray(data.getString("client_ids"), String.class);
                if (CollectionUtils.isEmpty(clientIds)) {
                    clientIds = JObject.parseArray(data.getString("clientIds"), String.class);
                }
                // 对客户端数据进行加密
                encryptClientIds = server.encryptClientDatasetMap(clientIds);
                CLIENT_IDS_MAP.put(this.requestId, true);
            }
            Set<String> batchData = getBatchData(model, currentBatch);
            // 对自己的数据集进行加密
            List<String> encryptServerIds = server.encryptDataset(new ArrayList<>(batchData));
            QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
            response.setClientIdByServerKeys(encryptClientIds);
            response.setServerEncryptIds(encryptServerIds);
            response.setRequestId(this.requestId);
            response.setCurrentBatch(currentBatch);
            response.setHasNext(currentBatch < (this.numPartitions - 1) - 1);
            return JObject.create(response);
        } else {
            type = "ECDH";
            String databaseType = idsTableName.split("#")[0];
            String dbConnUrl = idsTableName.split("#")[1];
            String callbackUrl = data.getString("callback_url");
            Map<Long, String> doubleEncryptedClientDatasetMap = null;
            List<String> doubleEncryptedClientDataset = null;
            EcdhPsiServer server = ECDH_SERVER_MAP.get(requestId);
            if (server == null) {
                LOG.info("ecdh server not found, new one");
                server = new EcdhPsiServer();
                ECDH_SERVER_MAP.put(this.requestId, server);
            }
            if (CLIENT_IDS_MAP.get(this.requestId) == null) {
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
                CLIENT_IDS_MAP.put(this.requestId, true);
                doubleEncryptedClientDataset = EcdhUtil.convert2List(doubleEncryptedClientDatasetMap);
            }
            Set<String> batchData = getBatchData(model, currentBatch);
            // 服务端对自己的数据集进行加密
            List<String> serverEncryptedDataset = server.encryptDataset(new ArrayList<>(batchData));
            // 把上面两个set发给客户端
            QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
            response.setClientIdByServerKeys(doubleEncryptedClientDataset); // 第一批次会传，后续批次为空
            response.setRequestId(this.requestId);
            response.setServerEncryptIds(serverEncryptedDataset);
            response.setCurrentBatch(currentBatch);
            response.setHasNext(currentBatch < (this.numPartitions - 1));
            return JObject.create(response);

        }
    }

    private List<String> getECDHServerData() {
        int serverDatasetSize = ECDH_SERVER_DATA_SET.size();
        SERVER_DATASET_SIZE.put(this.requestId, serverDatasetSize);
        this.numPartitions = Math.max(SERVER_DATASET_SIZE.get(this.requestId) / BATCH_SIZE, 1);
        return ECDH_SERVER_DATA_SET;
    }

    private List<String> getDHData(TableServiceMySqlModel model, int currentBatch) throws StatusCodeWithException {
        List<String> serverDataSet = new ArrayList<>();
        JSONObject dataSource = JObject.parseObject(model.getDataSource());
        String sql = "select id from " + model.getIdsTableName() + " limit " + currentBatch * BATCH_SIZE + ", "
                + BATCH_SIZE;
        List<String> needFields = new ArrayList<>();
        needFields.add("id");
        DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSource.getString("id"));
        if (dataSourceModel == null) {
            return new ArrayList<>();
        }
        List<Map<String, String>> result = dataSourceService.queryList(dataSourceModel, sql, needFields);
        for (Map<String, String> item : result) {
            serverDataSet.add(item.get("id"));
        }
        if (SERVER_DATASET_SIZE.get(this.requestId) == null) {
            int serverDatasetSize = (int) dataSourceService.count(dataSourceModel,
                    "select * from " + model.getIdsTableName());
            SERVER_DATASET_SIZE.put(this.requestId, serverDatasetSize);
        }
        this.numPartitions = Math.max(SERVER_DATASET_SIZE.get(this.requestId) / BATCH_SIZE, 1);
        LOG.info("get dh data end, serverDatasetSize = " + SERVER_DATASET_SIZE.get(this.requestId) + ", numPartitions="
                + numPartitions);
        return serverDataSet;
    }

    private Set<String> getBatchData(TableServiceMySqlModel model, int currentBatch) throws StatusCodeWithException {
        if ("DH".equalsIgnoreCase(this.type)) {
            return new HashSet<>(getDHData(model, currentBatch));
        } else { // ecdh
            List<Set<String>> partitions = PartitionUtil.partitionList(getECDHServerData(), this.numPartitions);
            return partitions.get(currentBatch);
        }
    }
}
