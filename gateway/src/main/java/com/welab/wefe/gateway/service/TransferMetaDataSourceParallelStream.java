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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * data source of dSourceProcessor type
 *
 * @author aaron.li
 **/
@Service
public class TransferMetaDataSourceParallelStream extends AbstractTransferMetaDataSource {
    private final static Logger LOG = LoggerFactory.getLogger(TransferMetaDataSourceParallelStream.class);

    /**
     * 失败重试次数
     */
    private static final int MAX_FAIL_RETRY_COUNT = 50;
    /**
     * 并行数
     */
    private static final int PARALLEL_COUNT = 5;

    private static ExecutorService EXECUTOR_SERVICE;

    static {
        int cpuCoreCount = Runtime.getRuntime().availableProcessors();
        EXECUTOR_SERVICE = cn.hutool.core.thread.ThreadUtil.newExecutor(2 * cpuCoreCount);
    }

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
            storage.getByStream(dbName, tableName, pageSize * PARALLEL_COUNT, new ClickhouseStorageStreamHandler(transferMeta, pageSize));
            return true;
        } catch (Exception e) {
            LOG.error("pushToRemote exception: ", e);
        }
        return false;
    }

    public static class ClickhouseStorageStreamHandler implements PersistentStorageStreamHandler {
        private GatewayMetaProto.TransferMeta transferMeta;
        private int sequenceNo = 0;
        private int pageSize = 0;
        private CopyOnWriteArrayList<ToRemoteTaskResult> toRemoteTaskResultCollector = new CopyOnWriteArrayList<>();

        public ClickhouseStorageStreamHandler(GatewayMetaProto.TransferMeta transferMeta, int pageSize) {
            this.transferMeta = transferMeta;
            this.pageSize = pageSize;
        }

        @Override
        public void handler(List<DataItemModel<byte[], byte[]>> itemModelList) throws Exception {
            List<List<DataItemModel<byte[], byte[]>>> splitResultList = splitData(itemModelList);
            CountDownLatch countDownLatch = new CountDownLatch(splitResultList.size());
            for (List<DataItemModel<byte[], byte[]>> dataItemModelList : splitResultList) {
                EXECUTOR_SERVICE.submit(new ToRemoteTask(transferMeta, dataItemModelList, sequenceNo++, countDownLatch, toRemoteTaskResultCollector));
            }
            countDownLatch.await();
            for (int i = 0; i < toRemoteTaskResultCollector.size(); i++) {
                ToRemoteTaskResult toRemoteTaskResult = toRemoteTaskResultCollector.get(i);
                if (!toRemoteTaskResult.success) {
                    throw toRemoteTaskResult.e;
                }
            }
            toRemoteTaskResultCollector.clear();
        }

        @Override
        public void finish(long totalCount) {
            String dbName = TransferMetaUtil.getDbName(transferMeta);
            String tableName = TransferMetaUtil.getTableName(transferMeta);
            LOG.info("发送CK数据完成, session id：{}, 库名：{}, 表名：{}, 总数量：{}.", transferMeta.getSessionId(), dbName, tableName, totalCount);
        }

        /**
         * 切割数据变成小块
         */
        private List<List<DataItemModel<byte[], byte[]>>> splitData(List<DataItemModel<byte[], byte[]>> itemModelList) {
            List<List<DataItemModel<byte[], byte[]>>> splitResultList = new ArrayList<>();
            List<DataItemModel<byte[], byte[]>> block = new ArrayList<>();
            for (int i = 0; i < itemModelList.size(); i++) {
                if ((block.size() == this.pageSize) || (i == itemModelList.size() - 1)) {
                    splitResultList.add(new ArrayList<>(block));
                    block.clear();
                } else {
                    block.add(itemModelList.get(i));
                }
            }
            return splitResultList;
        }

        /**
         * 推送数据到对端
         */
        private static class ToRemoteTask implements Runnable {
            private GatewayMetaProto.TransferMeta transferMeta;
            private List<DataItemModel<byte[], byte[]>> dataItemModelList;
            private int sequenceNo;
            private CountDownLatch countDownLatch;
            private CopyOnWriteArrayList<ToRemoteTaskResult> toRemoteTaskResultCollector;

            public ToRemoteTask(GatewayMetaProto.TransferMeta transferMeta, List<DataItemModel<byte[], byte[]>> dataItemModelList, int sequenceNo, CountDownLatch countDownLatch, CopyOnWriteArrayList<ToRemoteTaskResult> toRemoteTaskResultCollector) {
                this.transferMeta = transferMeta;
                this.dataItemModelList = dataItemModelList;
                this.sequenceNo = sequenceNo;
                this.countDownLatch = countDownLatch;
                this.toRemoteTaskResultCollector = toRemoteTaskResultCollector;
            }

            @Override
            public void run() {
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

                        boolean tlsEnable = GrpcUtil.checkTlsEnable(this.transferMeta);
                        GatewayMetaProto.Member dstMember = this.transferMeta.getDst();
                        originalChannel = channelCache.getNonNull(EndpointBuilder.endpointToUri(dstMember.getEndpoint()), tlsEnable, TlsUtil.getAllCertificates(tlsEnable));
                        // Set header
                        NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceStub asyncClientStub = NetworkDataTransferProxyServiceGrpc.newStub(originalChannel).withCallCredentials(new RemoteGrpcProxyCallCredentials(null, new SignVerifyMetadataBuilder(null), new SystemTimestampMetadataBuilder(null)));

                        // Get the request flow associated with the server
                        requestStreamObserver = asyncClientStub.pushDataSource(new PushDataSourceResponseStreamObserver(finishFuture, asyncResponseCollector));
                        requestStreamObserver.onNext(wrapData(this.sequenceNo, this.transferMeta, this.dataItemModelList));
                        requestStreamObserver.onCompleted();
                        // Blocking, waiting for the server to notify that the data has been processed, otherwise the message will be discarded
                        finishFuture.get();

                        boolean success = CollectionUtils.isNotEmpty(asyncResponseCollector.getSuccessList());
                        if (success) {
                            LOG.info("发送CK数据完成, session id：{}, 库名：{}, 表名：{}, 分片号：{}, 数据量：{}, 耗时：{}", transferMeta.getSessionId(), TransferMetaUtil.getDbName(transferMeta), TransferMetaUtil.getTableName(transferMeta), this.sequenceNo, this.dataItemModelList.size(), (System.currentTimeMillis() - startTime));
                            toRemoteTaskResultCollector.add(new ToRemoteTaskResult(true, null));
                            return;
                        }
                        LOG.info("发送CK数据失败, session id：{}, 库名：{}, 表名：{}, 分片号：{}, 数据量：{}", transferMeta.getSessionId(), TransferMetaUtil.getDbName(transferMeta), TransferMetaUtil.getTableName(transferMeta), this.sequenceNo, this.dataItemModelList.size());
                    } catch (Exception e) {
                        LOG.error("发送CK数据失败, session id: " + transferMeta.getSessionId() + ", dbName:" + TransferMetaUtil.getDbName(transferMeta) + ", tableName: " + TransferMetaUtil.getTableName(transferMeta) + ", 重试次数：" + i + ", exception: ", e);
                        if (i == MAX_FAIL_RETRY_COUNT) {
                            toRemoteTaskResultCollector.add(new ToRemoteTaskResult(false, e));
                        }
                        ThreadUtil.sleep(100 * (i + 1));
                    } finally {
                        this.countDownLatch.countDown();
                    }
                }
            }
        }

        /**
         * 推送结果
         */
        private static class ToRemoteTaskResult {
            public boolean success;
            public Exception e;

            public ToRemoteTaskResult(boolean success, Exception e) {
                this.success = success;
                this.e = e;
            }
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
                transferMeta = transferMeta.toBuilder().setTransferStatus(success ? GatewayMetaProto.TransferStatus.COMPLETE : GatewayMetaProto.TransferStatus.ERROR).setSequenceIsEnd(true).build();
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
