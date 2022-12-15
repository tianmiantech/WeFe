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

import com.google.common.util.concurrent.SettableFuture;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.service.persistent.PersistentStorage;
import com.welab.wefe.common.data.storage.service.persistent.PersistentStorageStreamHandler;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.NetworkDataTransferProxyServiceGrpc;
import com.welab.wefe.gateway.api.streammessage.PushDataSourceResponseStreamObserver;
import com.welab.wefe.gateway.cache.GrpcChannelCache;
import com.welab.wefe.gateway.common.EndpointBuilder;
import com.welab.wefe.gateway.common.KeyValueDataBuilder;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.config.ConfigProperties;
import com.welab.wefe.gateway.interceptor.RemoteGrpcProxyCallCredentials;
import com.welab.wefe.gateway.interceptor.SignVerifyMetadataBuilder;
import com.welab.wefe.gateway.interceptor.SystemTimestampMetadataBuilder;
import com.welab.wefe.gateway.service.base.AbstractTransferMetaDataSource;
import com.welab.wefe.gateway.util.GrpcUtil;
import com.welab.wefe.gateway.util.TlsUtil;
import com.welab.wefe.gateway.util.TransferMetaUtil;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * data source of dSourceProcessor type
 *
 * @author aaron.li
 **/
@Service
public class TransferMetaDataSourceStream extends AbstractTransferMetaDataSource {
    private final static Logger LOG = LoggerFactory.getLogger(TransferMetaDataSourceStream.class);

    /**
     * 失败重试次数
     */
    private static final int MAX_FAIL_RETRY_COUNT = 50;

    @Autowired
    private ConfigProperties configProperties;

    @Override
    public BasicMetaProto.ReturnStatus getDataAndPushToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        try {
            long startTime = System.currentTimeMillis();
            boolean success = pushToRemote(transferMeta);
            LOG.info("发送CK数据完成,库名：{}, 表名：{}, 成功与否：{}, 耗时：{}", TransferMetaUtil.getDbName(transferMeta), TransferMetaUtil.getTableName(transferMeta), success, (System.currentTimeMillis() - startTime));
            boolean completeSuccess = sendCompleteRequest(transferMeta, success);
            if (!completeSuccess) {
                return ReturnStatusBuilder.sysExc("重试" + MAX_FAIL_RETRY_COUNT + "次后发送CK完成标识数据到成员:" + transferMeta.getDst().getMemberName() + " 失败,请确认网络是否正常.", transferMeta.getSessionId());
            }
            if (!success) {
                return ReturnStatusBuilder.sysExc("重试" + MAX_FAIL_RETRY_COUNT + "次后发转发CK数据到成员:" + transferMeta.getDst().getMemberName() + " 失败,请确认网络是否正常.", transferMeta.getSessionId());
            }
            return ReturnStatusBuilder.ok(transferMeta.getSessionId());
        } catch (Exception e) {
            LOG.error("发送CK数据异常: ", e);
            return ReturnStatusBuilder.sysExc("pushToRemote exception", transferMeta.getSessionId());
        }
    }

    public boolean pushToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        try {
            long byteSize = (long) (configProperties.getPersistentStorageBatchInsertSize() * 1024 * 1024d);
            PersistentStorage storage = PersistentStorage.getInstance();
            String dbName = TransferMetaUtil.getDbName(transferMeta);
            String tableName = TransferMetaUtil.getTableName(transferMeta);
            int pageSize = storage.getCountByByteSize(dbName, tableName, byteSize);
            storage.getByStream(dbName, tableName, pageSize, new ClickhouseStorageStreamHandler(transferMeta));
            return true;
        } catch (Exception e) {
            LOG.error("pushToRemote exception: ", e);
        }
        return false;
    }

    public static class ClickhouseStorageStreamHandler implements PersistentStorageStreamHandler {
        private GatewayMetaProto.TransferMeta transferMeta;
        private int sequenceNo = 0;

        public ClickhouseStorageStreamHandler(GatewayMetaProto.TransferMeta transferMeta) {
            this.transferMeta = transferMeta;
        }

        @Override
        public void handler(List<DataItemModel<byte[], byte[]>> itemModelList) throws Exception {
            for (int i = 0; i <= MAX_FAIL_RETRY_COUNT; i++) {
                try {
                    long startTime = System.currentTimeMillis();
                    // Synchronizer
                    final SettableFuture<Void> finishFuture = SettableFuture.create();
                    // Response result collector
                    AsyncResponseCollector asyncResponseCollector = new AsyncResponseCollector();
                    ManagedChannel originalChannel = null;
                    StreamObserver<GatewayMetaProto.TransferMeta> requestStreamObserver = null;
                    GrpcChannelCache channelCache = GrpcChannelCache.getInstance();

                    boolean tlsEnable = GrpcUtil.checkTlsEnable(transferMeta);
                    GatewayMetaProto.Member dstMember = transferMeta.getDst();
                    originalChannel = channelCache.getNonNull(EndpointBuilder.endpointToUri(dstMember.getEndpoint()), tlsEnable, TlsUtil.getAllCertificates(tlsEnable));
                    // Set header
                    NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceStub asyncClientStub = NetworkDataTransferProxyServiceGrpc.newStub(originalChannel).withCallCredentials(new RemoteGrpcProxyCallCredentials(null, new SignVerifyMetadataBuilder(null), new SystemTimestampMetadataBuilder(null)));

                    // Get the request flow associated with the server
                    requestStreamObserver = asyncClientStub.pushDataSource(new PushDataSourceResponseStreamObserver(finishFuture, asyncResponseCollector));
                    requestStreamObserver.onNext(wrapData(this.sequenceNo, this.transferMeta, itemModelList));
                    requestStreamObserver.onCompleted();
                    // Blocking, waiting for the server to notify that the data has been processed, otherwise the message will be discarded
                    finishFuture.get();

                    boolean success = CollectionUtils.isNotEmpty(asyncResponseCollector.getSuccessList());
                    if (success) {
                        LOG.info("发送CK数据完成, session id：{}, 库名：{}, 表名：{}, 分片号：{}, 数据量：{}, 耗时：{}", transferMeta.getSessionId(), TransferMetaUtil.getDbName(transferMeta), TransferMetaUtil.getTableName(transferMeta), this.sequenceNo, itemModelList.size(), (System.currentTimeMillis() - startTime));
                        sequenceNo++;
                        return;
                    }
                    LOG.info("发送CK数据失败, session id：{}, 库名：{}, 表名：{}, 分片号：{}, 数据量：{}", transferMeta.getSessionId(), TransferMetaUtil.getDbName(transferMeta), TransferMetaUtil.getTableName(transferMeta), this.sequenceNo, itemModelList.size());
                } catch (Exception e) {
                    LOG.error("发送CK数据失败, session id: " + transferMeta.getSessionId() + ", dbName:" + TransferMetaUtil.getDbName(transferMeta) + ", tableName: " + TransferMetaUtil.getTableName(transferMeta) + ", 重试次数：" + i + ", exception: ", e);
                    if (i == MAX_FAIL_RETRY_COUNT) {
                        throw e;
                    }
                    ThreadUtil.sleep(100 * (i + 1));
                }
            }
        }

        @Override
        public void finish(long totalCount) {
            String dbName = TransferMetaUtil.getDbName(transferMeta);
            String tableName = TransferMetaUtil.getTableName(transferMeta);
            LOG.info("发送CK数据完成, session id：{}, 库名：{}, 表名：{}, 总数量：{}.", transferMeta.getSessionId(), dbName, tableName, totalCount);
        }
    }


    /**
     * Send data submission completion request flag
     */
    private boolean sendCompleteRequest(GatewayMetaProto.TransferMeta transferMeta, boolean success) throws Exception {
        GrpcChannelCache channelCache = GrpcChannelCache.getInstance();
        ManagedChannel originalChannel = null;
        for (int i = 0; i <= MAX_FAIL_RETRY_COUNT; i++) {
            StreamObserver<GatewayMetaProto.TransferMeta> requestStreamObserver = null;
            try {
                boolean tlsEnable = GrpcUtil.checkTlsEnable(transferMeta);
                originalChannel = channelCache.getNonNull(EndpointBuilder.endpointToUri(transferMeta.getDst().getEndpoint()), tlsEnable, TlsUtil.getAllCertificates(tlsEnable));
                transferMeta = transferMeta.toBuilder().setTransferStatus(success ? GatewayMetaProto.TransferStatus.COMPLETE : GatewayMetaProto.TransferStatus.ERROR)
                        .setSequenceIsEnd(true).build();
                // Set header
                NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceStub asyncClientStub = NetworkDataTransferProxyServiceGrpc.newStub(originalChannel).withCallCredentials(new RemoteGrpcProxyCallCredentials(null, new SignVerifyMetadataBuilder(null), new SystemTimestampMetadataBuilder(null)));

                // Synchronizer
                final SettableFuture<Void> finishFuture = SettableFuture.create();
                AsyncResponseCollector asyncResponseCollector = new AsyncResponseCollector();
                requestStreamObserver = asyncClientStub.pushDataSource(new PushDataSourceResponseStreamObserver(finishFuture, asyncResponseCollector));

                requestStreamObserver.onNext(transferMeta);
                requestStreamObserver.onCompleted();
                finishFuture.get();
                if (CollectionUtils.isNotEmpty(asyncResponseCollector.getSuccessList())) {
                    return true;
                }
            } catch (Exception e) {
                LOG.error("Data source send complete request fail, session id: " + transferMeta.getSessionId() + ", retry count:" + i);
                // If the maximum number of failed retries is exceeded, the exception can be thrown directly
                if (i == MAX_FAIL_RETRY_COUNT) {
                    throw e;
                }
                ThreadUtil.sleep(100 * (i + 1));
            }
        }
        return false;
    }

    /**
     * Packaging data
     */
    private static GatewayMetaProto.TransferMeta wrapData(int sequenceNo, GatewayMetaProto.TransferMeta transferMeta, List<DataItemModel<byte[], byte[]>> itemModelList) {
        List<BasicMetaProto.KeyValueData> keyValueDataList = new ArrayList<>();
        for (int i = 0; i < itemModelList.size(); i++) {
            DataItemModel<byte[], byte[]> dataItemModel = itemModelList.get(i);
            keyValueDataList.add(KeyValueDataBuilder.create(dataItemModel.getK(), dataItemModel.getV()));
        }

        GatewayMetaProto.TransferMeta.Builder builder = transferMeta.toBuilder();
        builder.setSequenceIsEnd(false).setSequenceNo(sequenceNo).setTransferStatus(GatewayMetaProto.TransferStatus.PROCESSING).getContentBuilder().clearKeyValueDatas().addAllKeyValueDatas(keyValueDataList);
        return builder.build();
    }
}
