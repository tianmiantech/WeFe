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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.DatabaseType;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.psi.sdk.Psi;
import com.welab.wefe.mpc.psi.sdk.dh.DhPsiServer;
import com.welab.wefe.mpc.psi.sdk.ecdh.EcdhPsiServer;
import com.welab.wefe.mpc.psi.sdk.util.EcdhUtil;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;
import com.welab.wefe.serving.service.utils.ServiceUtil;

/**
 * @author hunter.zhao
 */
public class PsiServiceProcessor extends AbstractServiceProcessor<TableServiceMySqlModel> {

    private static final ConcurrentHashMap<String, EcdhPsiServer> ECDH_SERVER_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, DhPsiServer> DH_SERVER_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> SERVER_DATASET_SIZE = new ConcurrentHashMap<>();

    protected final Config config = Launcher.getBean(Config.class);
    private int batchSize;
    private int numPartitions; // 服务端数据批次
    private String requestId;

    @Override
    public JObject process(JObject data, TableServiceMySqlModel model) throws StatusCodeWithException {
        String type = data.getString("type");
        initParams(data);
        if (Psi.DH_PSI.equalsIgnoreCase(type)) { // dh
            return processDH(data, model);
        } else if (Psi.ECDH_PSI.equalsIgnoreCase(type)) { // default ECDH
            return processECDH(data, model);
        } else {
            return processPSIResult(data, model);
        }
    }

    private void initParams(JObject data) {
        String rid = data.getString("requestId");
        this.requestId = rid;
        this.batchSize = config.getPsiBatchSize();
        int bs = data.getIntValue("batchSize");
        if (bs > 0) {
            this.batchSize = bs;
        }
    }

    private JObject processPSIResult(JObject data, TableServiceMySqlModel model) throws StatusCodeWithException {
        List<String> clientIds = JObject.parseArray(data.getString("client_ids"), String.class);
        if (CollectionUtils.isEmpty(clientIds)) {
            clientIds = JObject.parseArray(data.getString("clientIds"), String.class);
        }
        JSONObject dataSource = JObject.parseObject(model.getDataSource());
        DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSource.getString("id"));
        if (dataSourceModel == null) {
            throw new StatusCodeWithException("datasource not found", StatusCode.DATA_NOT_FOUND);
        }
        String tempId = clientIds.get(0);
        QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
        response.setRequestId(this.requestId);
        List<String> result = new ArrayList<>();
        // 这种情况 单个clientId 为json ,包含多个字段
        if (JObject.isValidObject(tempId)) { // id is json
            LOG.info("process psi result , id is json , first = " + tempId);
            String fieldStr = ServiceUtil.parseReturnFields(dataSource);
            List<String> resultFields = new ArrayList<>(Arrays.asList(fieldStr.split(",")));
            List<String> conditions = new ArrayList<>();
            List<String> fieldList = new LinkedList<>();
            JSONArray keyCalcRules = dataSource.getJSONArray("key_calc_rules");
            for (int i = 0; i < keyCalcRules.size(); i++) {
                JSONObject item = keyCalcRules.getJSONObject(i);
                String[] fields = item.getString("field").split(",");
                for (String f : fields) {
                    conditions.add(f + " = ?");
                    fieldList.add(f);
                    if (!resultFields.contains(f)) {
                        resultFields.add(f);
                    }
                }
            }
            List<Map<String, Object>> conditionFieldValues = new ArrayList<>();
            for (String idJson : clientIds) {// params
                JSONObject idObj = JSONObject.parseObject(idJson);
                Map<String, Object> idMap = new LinkedHashMap<>();
                for (String f : fieldList) {
                    idMap.put(f, idObj.get(f));
                }
                conditionFieldValues.add(idMap);
            }
            String tableName = dataSourceModel.getDatabaseName() + "." + dataSource.getString("table");
            String sql = "SELECT " + StringUtils.join(resultFields, ",") + " FROM " + tableName + " WHERE "
                    + StringUtils.join(conditions, " and ") + " limit 1";
            LOG.info("sql is " + sql);
            try {
                List<Map<String, String>> resultMaps = dataSourceService.queryListByConditions(dataSourceModel, sql,
                        conditionFieldValues, resultFields);
                resultMaps.stream().forEach(s -> {
                    result.add(JObject.toJSONString(s));
                });
            } catch (StatusCodeWithException e) {
                LOG.error("pir query data error", e);
            }
        } else {// 这种情况 单个clientId 为单个值
            LOG.info("process psi result , id is value , first = " + tempId);
            String tableName = dataSourceModel.getDatabaseName() + "." + dataSource.getString("table");
            String fieldStr = ServiceUtil.parseReturnFields(dataSource);
            List<String> resultFields = new ArrayList<>(Arrays.asList(fieldStr.split(",")));
            JSONArray keyCalcRules = dataSource.getJSONArray("key_calc_rules");
            if (keyCalcRules.size() != 1) {
                throw new StatusCodeWithException("参数错误", StatusCode.ILLEGAL_REQUEST);
            }
            JSONObject item = keyCalcRules.getJSONObject(0);
            String[] fields = item.getString("field").split(",");
            if (fields.length != 1) {
                throw new StatusCodeWithException("参数错误", StatusCode.ILLEGAL_REQUEST);
            }
            String keyField = fields[0];
            if (!resultFields.contains(keyField)) {
                resultFields.add(keyField);
            }
            // sql like SELECT xxx FROM MY_TABLE WHERE ID IN (?,?,?,?,?,?,?,?,?,?))
            String sql = "SELECT " + StringUtils.join(resultFields, ",") + " FROM " + tableName + " WHERE " + keyField
                    + " IN (?,?,?,?,?,?,?,?,?,?)";
            LOG.info("sql is " + sql);
            List<Map<String, String>> list = dataSourceService.queryListByIds(dataSourceModel, sql, clientIds,
                    resultFields);
            list.stream().forEach(s -> {
                result.add(JObject.toJSONString(s));
            });
        }
        response.setFieldResults(result);
        return JObject.create(response);
    }

    private JObject processDH(JObject data, TableServiceMySqlModel model) throws StatusCodeWithException {
        // 当前批次
        int currentBatch = data.getIntValue("currentBatch");
        String p = data.getString("p");
        DhPsiServer server = DH_SERVER_MAP.get(requestId);
        if (server == null) {
            LOG.info("dh server not found, new one");
            server = new DhPsiServer(p);
            DH_SERVER_MAP.put(requestId, server);
        }
        List<String> clientIds = JObject.parseArray(data.getString("client_ids"), String.class);
        if (CollectionUtils.isEmpty(clientIds)) {
            clientIds = JObject.parseArray(data.getString("clientIds"), String.class);
        }
        List<String> doubleEncryptedClientDataset = null;
        if (!CollectionUtils.isEmpty(clientIds)) {
            Map<Long, String> clientEncryptedDatasetMap = EcdhUtil.convert2Map(clientIds);
            // 对客户端数据进行加密
            Map<Long, String> doubleEncryptedClientDatasetMap = server
                    .encryptClientDatasetMap(clientEncryptedDatasetMap);
            doubleEncryptedClientDataset = EcdhUtil.convert2List(doubleEncryptedClientDatasetMap);
        }
        List<String> batchData = getBatchData(model, currentBatch);
        // 对自己的数据集进行加密
        List<String> encryptServerIds = server.encryptDataset(batchData);
        QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
        response.setClientIdByServerKeys(doubleEncryptedClientDataset);
        response.setServerEncryptIds(encryptServerIds);
        response.setRequestId(this.requestId);
        response.setCurrentBatch(currentBatch);
        response.setHasNext(currentBatch < (this.numPartitions - 1));
        return JObject.create(response);
    }

    private JObject processECDH(JObject data, TableServiceMySqlModel model) throws StatusCodeWithException {
        // 当前批次
        int currentBatch = data.getIntValue("currentBatch");
        Map<Long, String> doubleEncryptedClientDatasetMap = null;
        List<String> doubleEncryptedClientDataset = null;
        EcdhPsiServer server = ECDH_SERVER_MAP.get(requestId);
        if (server == null) {
            LOG.info("ecdh server not found, new one");
            server = new EcdhPsiServer();
            ECDH_SERVER_MAP.put(this.requestId, server);
        }
        List<String> clientIds = JObject.parseArray(data.getString("client_ids"), String.class);
        if (CollectionUtils.isEmpty(clientIds)) {
            clientIds = JObject.parseArray(data.getString("clientIds"), String.class);
        }
        if (!CollectionUtils.isEmpty(clientIds)) {
            Map<Long, String> clientEncryptedDatasetMap = EcdhUtil.convert2Map(clientIds);
            // 对客户端数据进行二次加密
            doubleEncryptedClientDatasetMap = server.encryptDatasetMap(clientEncryptedDatasetMap);
            doubleEncryptedClientDataset = EcdhUtil.convert2List(doubleEncryptedClientDatasetMap);
        }

        List<String> batchData = getBatchData(model, currentBatch);
        // 服务端对自己的数据集进行加密
        List<String> serverEncryptedDataset = server.encryptDataset(batchData);
        // 把上面两个set发给客户端
        QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
        response.setClientIdByServerKeys(doubleEncryptedClientDataset); // 第一批次会传，后续批次为空
        response.setRequestId(this.requestId);
        response.setServerEncryptIds(serverEncryptedDataset);
        response.setCurrentBatch(currentBatch);
        response.setHasNext(currentBatch < (this.numPartitions - 1));
        return JObject.create(response);
    }

    // doris
    private List<String> getDorisData(TableServiceMySqlModel model, DataSourceMySqlModel dataSourceModel,
            JSONObject dataSource, int currentBatch) throws StatusCodeWithException {
        String tableName = model.getIdsTableName();
        JSONArray keyCalcRules = dataSource.getJSONArray("key_calc_rules");
        List<String> needFields = new ArrayList<>();
        for (int i = 0; i < keyCalcRules.size(); i++) {
            JSONObject item = keyCalcRules.getJSONObject(i);
            String[] fields = item.getString("field").split(",");
            needFields.addAll(Arrays.asList(fields));
        }
        String sql = "select " + StringUtils.join(needFields, ",") + " from " + tableName + " order by id limit "
                + currentBatch * this.batchSize + ", " + this.batchSize;
        List<Map<String, String>> result = dataSourceService.queryList(dataSourceModel, sql, needFields);
        List<Queue<Map<String, String>>> partitionList = ServiceUtil.partitionList(result,
                Math.max(this.batchSize / 100000, 1));
        result = null;
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        for (int i = 0; i < partitionList.size(); i++) {
            final int finalI = i;
            Queue<Map<String, String>> partition = partitionList.get(i);
            executorService.submit(() -> {
                LOG.info("calcKey begin index = " + finalI + ", partition size = " + partition.size());
                while (!partition.isEmpty()) {
                    queue.add(ServiceUtil.calcKey(keyCalcRules, partition.poll()));
                }
                LOG.info("calcKey end index = " + finalI + ", queue size = " + queue.size());
            });
        }
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // wait
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        if (SERVER_DATASET_SIZE.get(this.requestId) == null) {
            int serverDatasetSize = (int) dataSourceService.count(dataSourceModel,
                    "select * from " + model.getIdsTableName());
            SERVER_DATASET_SIZE.put(this.requestId, serverDatasetSize);
        }
        this.numPartitions = Math.max(SERVER_DATASET_SIZE.get(this.requestId) / this.batchSize, 1);
        LOG.info("get doris data end, serverDatasetSize = " + SERVER_DATASET_SIZE.get(this.requestId)
                + ", numPartitions=" + numPartitions + ", serverDataSet size = " + queue.size());
        return new ArrayList<>(queue);
    }

    private List<String> getMysqlData(TableServiceMySqlModel model, DataSourceMySqlModel dataSourceModel,
            JSONObject dataSource, int currentBatch) throws StatusCodeWithException {
        List<String> needFields = new ArrayList<>(Arrays.asList("id"));
        List<String> serverDataSet = new ArrayList<>();
        String sql = "select " + StringUtils.join(needFields, ",") + " from " + model.getIdsTableName() + " limit "
                + currentBatch * this.batchSize + ", " + this.batchSize;
        List<Map<String, String>> result = dataSourceService.queryList(dataSourceModel, sql, needFields);
        for (Map<String, String> item : result) {
            serverDataSet.add(item.get("id"));
        }
        if (SERVER_DATASET_SIZE.get(this.requestId) == null) {
            int serverDatasetSize = (int) dataSourceService.count(dataSourceModel,
                    "select * from " + model.getIdsTableName());
            SERVER_DATASET_SIZE.put(this.requestId, serverDatasetSize);
        }
        this.numPartitions = Math.max(SERVER_DATASET_SIZE.get(this.requestId) / this.batchSize, 1);
        LOG.info("get mysql data end, serverDatasetSize = " + SERVER_DATASET_SIZE.get(this.requestId)
                + ", numPartitions=" + numPartitions + ", serverDataSet size = " + serverDataSet.size());
        return serverDataSet;
    }

    private List<String> getBatchData(TableServiceMySqlModel model, int currentBatch) throws StatusCodeWithException {
        JSONObject dataSource = JObject.parseObject(model.getDataSource());
        DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSource.getString("id"));
        if (dataSourceModel == null) {
            throw new StatusCodeWithException("datasource not found", StatusCode.DATA_NOT_FOUND);
        }
        if (dataSourceModel.getDatabaseType() == DatabaseType.MySql) {
            return getMysqlData(model, dataSourceModel, dataSource, currentBatch);
        } else if (dataSourceModel.getDatabaseType() == DatabaseType.Doris) {
            return getDorisData(model, dataSourceModel, dataSource, currentBatch);
        }
        throw new StatusCodeWithException("datasource type not support" + dataSourceModel.getDatabaseType(),
                StatusCode.INVALID_DATASET);
    }
}
