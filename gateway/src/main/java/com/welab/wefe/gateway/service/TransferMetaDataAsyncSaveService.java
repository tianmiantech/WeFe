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

package com.welab.wefe.gateway.service;

import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.service.StorageService;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.common.wefe.enums.JobBackendType;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.util.SerializeUtil;
import com.welab.wefe.gateway.util.TransferMetaUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aaron.li
 **/
@Service
public class TransferMetaDataAsyncSaveService {
    private final Logger LOG = LoggerFactory.getLogger(TransferMetaDataAsyncSaveService.class);

    @Autowired
    private StorageService storageService;

    @Autowired
    private MessageService messageService;

    /**
     * BaseStorage method used for function calculation
     */
    @Value(value = "${fc.storage.type}")
    private String storageType;

    @Value(value = "${wefe.job.backend}")
    private JobBackendType flowBackend;

    /**
     * Save entity data to database
     */
    @Async("transferMetaDataAsyncExecutor")
    public void save(String sessionId, TransferMetaDataSink.ProcessingTransferMetaData processingTransferMetaData) {
        GatewayMetaProto.TransferMeta transferMeta = null;
        try {
            transferMeta = SerializeUtil.deserializationTransferMeta(processingTransferMetaData.serializePath);
        } catch (Exception e) {
            LOG.error("transferMeta deserialization fail, path: " + processingTransferMetaData.serializePath, e);

            processingTransferMetaData.status = TransferMetaDataSink.PROCESS_STATUS_FAIL;
            FileUtil.deleteFileOrDir(processingTransferMetaData.serializePath);
            messageService.saveError("数据反序列化失败", e.getMessage());
            return;
        }

        // Original databaseName
        String srcDbName = TransferMetaUtil.getDbName(transferMeta);
        // Original tableName
        String srcTableName = TransferMetaUtil.getTableName(transferMeta);
        // Target databaseName
        String dstDbName = TransferMetaUtil.getDstDbName(transferMeta);
        // Target tableName
        String dstTableName = TransferMetaUtil.getDstTableName(transferMeta);


        List<GatewayMetaProto.ConfigData> dataList = transferMeta.getContent().getConfigDatasList();
        LOG.info("Data sink finish, session id: {}, dstDbName: {}, dstTableName: {}, data size: {}", sessionId, dstDbName, dstTableName, CollectionUtils.isEmpty(dataList) ? 0 : dataList.size());
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }

        // Failed retries count
        int failTryCount = 3;
        int i = 0;
        for (; i < failTryCount; i++) {
            try {
                long startTime = System.currentTimeMillis();
                List<DataItemModel<byte[], byte[]>> dateItemModelList = new ArrayList<>();
                DataItemModel<byte[], byte[]> dateItemModel = null;
                for (GatewayMetaProto.ConfigData configData : dataList) {
                    dateItemModel = new DataItemModel<>();
                    dateItemModel.setK(configData.getKey().toByteArray());
                    dateItemModel.setV(configData.getValue().toByteArray());
                    dateItemModelList.add(dateItemModel);
                }

                String fcDbName = TransferMetaUtil.getFCNamespace(transferMeta);
                String fcTableName = TransferMetaUtil.getFCName(transferMeta);
                // Number of target partitions
                int fcPartitions = TransferMetaUtil.getFCPartitions(transferMeta);
                // Own use OTS
                Map<String, Object> args = new HashMap<>();
                args.put("fc_partitions", fcPartitions);
                args.put("fc_namespace", fcDbName);
                args.put("fc_name", fcTableName);
                LOG.info("The amount of data is：" + dateItemModelList.size());
                LOG.info("flowBackend is：" + flowBackend.toString());

                if (flowBackend == JobBackendType.FC && "ots".equals(storageType.toLowerCase())) {
                    LOG.info("The data from CK has been received and is now uploaded to OTS, fc_namespace: " + fcDbName + ", fc_name: " + fcTableName + ", fc_partitions: " + fcPartitions);
                    args.put("storage_type", "ots");
                    storageService.saveList(dateItemModelList, args);

                } else if (flowBackend == JobBackendType.FC && "oss".equals(storageType.toLowerCase())) {
                    LOG.info("The data from CK has been received and is now uploaded to OSS, fc_namespace: " + fcDbName + ", fc_name: " + fcTableName + ", fc_partitions: " + fcPartitions);
                    args.put("storage_type", "oss");
                    storageService.saveList(dateItemModelList, args);
                } else {
                    // Own use Ck
                    storageService.saveList(dstDbName, dstTableName, dateItemModelList);
                    LOG.info("Data sink finish, session id: {}, sequence no: {}, db name: {}, table name: {}, dst db name: {}, dst table name: {}, data size: {}, time spent: {}", transferMeta.getSessionId(), transferMeta.getSequenceNo(), srcDbName, srcTableName, dstDbName, dstTableName, dataList.size(), (System.currentTimeMillis() - startTime));

                }
                processingTransferMetaData.status = TransferMetaDataSink.PROCESS_STATUS_SUCCESS;
                break;
            } catch (Exception e) {
                messageService.saveError("保存数据块失败", e.getMessage(), transferMeta);
                LOG.error("sink data error, session id: " + sessionId + ", sequence no: " + transferMeta.getSequenceNo() + ", dstDbName: " + dstDbName + ", dstTableName: " + dstTableName + ", exception：", e);
            }
            ThreadUtil.sleep((i + 1) * 500);
        }

        // Processing failed
        if (i >= failTryCount) {
            LOG.error("Data sink finish, process fail, session id: {}, sequence no: {}", transferMeta.getSessionId(), transferMeta.getSequenceNo());
            // Update status is processing failed
            processingTransferMetaData.status = TransferMetaDataSink.PROCESS_STATUS_FAIL;
        }
        FileUtil.deleteFileOrDir(processingTransferMetaData.serializePath);
    }
}
