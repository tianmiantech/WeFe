/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.gateway.service;

import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.service.fc.FcStorage;
import com.welab.wefe.common.data.storage.service.persistent.PersistentStorage;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.common.wefe.dto.global_config.GatewayConfigModel;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.TransferServiceGrpc;
import com.welab.wefe.gateway.cache.GrpcChannelCache;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.init.InitStorageManager;
import com.welab.wefe.gateway.util.GrpcUtil;
import com.welab.wefe.gateway.util.SerializeUtil;
import com.welab.wefe.gateway.util.TransferMetaUtil;
import io.grpc.ManagedChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author aaron.li
 **/
@Service
public class TransferMetaDataAsyncSaveService {
    private final Logger LOG = LoggerFactory.getLogger(TransferMetaDataAsyncSaveService.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private GlobalConfigService globalConfigService;

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


        List<BasicMetaProto.KeyValueData> dataList = transferMeta.getContent().getKeyValueDatasList();
        if (CollectionUtils.isEmpty(dataList)) {
            LOG.info("data is none, session id: {}, dstDbName: {}, dstTableName: {}, data size: {}", sessionId, dstDbName, dstTableName, CollectionUtils.isEmpty(dataList) ? 0 : dataList.size());
            return;
        }
        // 检查FC和Ck等配置
        checkStorageAndCallback(transferMeta);

        // Failed retries count
        int failTryCount = 3;
        int i = 0;
        for (; i < failTryCount; i++) {
            try {
                long startTime = System.currentTimeMillis();
                List<DataItemModel<byte[], byte[]>> dateItemModelList = new ArrayList<>();
                DataItemModel<byte[], byte[]> dateItemModel = null;
                for (BasicMetaProto.KeyValueData keyValueData : dataList) {
                    dateItemModel = new DataItemModel<>();
                    dateItemModel.setK(keyValueData.getKey().toByteArray());
                    dateItemModel.setV(keyValueData.getValue().toByteArray());
                    dateItemModelList.add(dateItemModel);
                }

                String fcDbName = TransferMetaUtil.getFCNamespace(transferMeta);
                String fcTableName = TransferMetaUtil.getFCName(transferMeta);
                // Number of target partitions
                int fcPartitions = TransferMetaUtil.getFCPartitions(transferMeta);
                String storageType = TransferMetaUtil.getStorageType(transferMeta);
                // Own use OTS
                Map<String, Object> args = new HashMap<>();
                args.put("fc_partitions", fcPartitions);
                args.put("fc_namespace", fcDbName);
                args.put("fc_name", fcTableName);
                LOG.info("The amount of data is：" + dateItemModelList.size());
                LOG.info("storageType: " + storageType);

                if ("ots".equalsIgnoreCase(storageType)) {
                    LOG.info("The data has been received and is now uploaded to OTS, fc_namespace: " + fcDbName + ", fc_name: " + fcTableName + ", fc_partitions: " + fcPartitions);
                    args.put("storage_type", "ots");
                    //storageService.saveList(dateItemModelList, args);
                    FcStorage.getInstance().putAll(dateItemModelList, args);

                } else if ("oss".equalsIgnoreCase(storageType)) {
                    LOG.info("The data has been received and is now uploaded to OSS, fc_namespace: " + fcDbName + ", fc_name: " + fcTableName + ", fc_partitions: " + fcPartitions);
                    args.put("storage_type", "oss");
                    //storageService.saveList(dateItemModelList, args);
                    FcStorage.getInstance().putAll(dateItemModelList, args);
                } else if ("clickhouse".equalsIgnoreCase(storageType)) {
                    // Own use Ck
                    //storageService.saveList(dstDbName, dstTableName, dateItemModelList);
                    PersistentStorage.getInstance().putAll(dstDbName, dstTableName, dateItemModelList);
                    LOG.info("Data sink finish, session id: {}, sequence no: {}, db name: {}, table name: {}, dst db name: {}, dst table name: {}, data size: {}, time spent: {}", transferMeta.getSessionId(), transferMeta.getSequenceNo(), srcDbName, srcTableName, dstDbName, dstTableName, dataList.size(), (System.currentTimeMillis() - startTime));

                } else if ("cos".equalsIgnoreCase(storageType)) {
                    LOG.info("The data has been received and is now uploaded to COS, fc_namespace: " + fcDbName + ", fc_name: " + fcTableName + ", fc_partitions: " + fcPartitions);
                    args.put("storage_type", "cos");
                    //storageService.saveList(dateItemModelList, args);
                    FcStorage.getInstance().putAll(dateItemModelList, args);
                } else {
                    LOG.error("storage type: " + storageType + " is undefined");
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

    /**
     * 检查本地FC和Clickhouse的配置情况,异常则通知对端成员
     */
    private void checkStorageAndCallback(GatewayMetaProto.TransferMeta transferMeta) {
        if (0 != transferMeta.getSequenceNo()) {
            return;
        }

        String storageType = TransferMetaUtil.getStorageType(transferMeta);
        String errorMsg = "";
        if ("clickhouse".equalsIgnoreCase(storageType)) {
            errorMsg = (!InitStorageManager.PERSISTENT_INIT.get() ? "Clickhouse未初始化完成" : errorMsg);
        } else if ("ots".equalsIgnoreCase(storageType) || "oss".equalsIgnoreCase(storageType)) {
            errorMsg = (!InitStorageManager.FC_INIT.get() ? "FC未初始化完成" : errorMsg);
        }
        if (StringUtil.isEmpty(errorMsg)) {
            return;
        }
        try {
            // 通知发送方
            String dstMemberId = transferMeta.getSrc().getMemberId();
            String dstMemberName = MemberCache.getInstance().get(dstMemberId).getName();
            GatewayMetaProto.Member dstMember = GatewayMetaProto.Member.newBuilder().setMemberId(dstMemberId)
                    .setMemberName(dstMemberName).build();

            GatewayMetaProto.Content content = GatewayMetaProto.Content.newBuilder()
                    .setStrData(errorMsg)
                    .build();
            GatewayMetaProto.TransferMeta callbackTransferMeta = GatewayMetaProto.TransferMeta.newBuilder()
                    .setDst(dstMember)
                    .setContent(content)
                    .setSessionId(UUID.randomUUID().toString().replaceAll("-", ""))
                    .setProcessor(GatewayProcessorType.remoteCallbackProcessor.name())
                    .build();

            String uri = globalConfigService.getModel(GatewayConfigModel.class).intranetBaseUri;
            GrpcChannelCache channelCache = GrpcChannelCache.getInstance();
            ManagedChannel channel = channelCache.getNonNull(uri, false, null);
            TransferServiceGrpc.TransferServiceBlockingStub clientStub = TransferServiceGrpc.newBlockingStub(channel);
            BasicMetaProto.ReturnStatus result = clientStub.send(callbackTransferMeta);
            LOG.info("Check storage and Callback response: " + result.getMessage());
        } catch (Exception e) {
            LOG.error("Check storage exception: ", e);
        }
    }
}
