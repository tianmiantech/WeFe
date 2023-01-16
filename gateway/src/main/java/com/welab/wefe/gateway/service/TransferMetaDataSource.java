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
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.data.storage.service.persistent.PersistentStorage;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.NetworkDataTransferProxyServiceGrpc;
import com.welab.wefe.gateway.api.streammessage.PushDataSourceResponseStreamObserver;
import com.welab.wefe.gateway.cache.GrpcChannelCache;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.EndpointBuilder;
import com.welab.wefe.gateway.common.KeyValueDataBuilder;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.config.ConfigProperties;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.interceptor.ClientCallCredentials;
import com.welab.wefe.gateway.interceptor.SignVerifyMetadataBuilder;
import com.welab.wefe.gateway.interceptor.SystemTimestampMetadataBuilder;
import com.welab.wefe.gateway.service.base.AbstractTransferMetaDataSource;
import com.welab.wefe.gateway.util.GrpcUtil;
import com.welab.wefe.gateway.util.TlsUtil;
import com.welab.wefe.gateway.util.TransferMetaUtil;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
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
public class TransferMetaDataSource extends AbstractTransferMetaDataSource {
    private final Logger LOG = LoggerFactory.getLogger(TransferMetaDataSource.class);
    /**
     * Number of entries per page
     */
    private static final ThreadLocal<Integer> PAGE_SIZE_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * Optimal sub block size sent to the remote end
     */
    private static final ThreadLocal<Integer> OPTIMAL_SUB_BLOCK_SIZE_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 失败重试次数
     */
    private static final int FAIL_RETRY_COUNT = 20;

    @Autowired
    private ConfigProperties configProperties;

    @Override
    public BasicMetaProto.ReturnStatus getDataAndPushToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        long startTime = System.currentTimeMillis();
        try {
            // Set paging parameters
            setPagingParams(transferMeta);
            // Get total send block
            List<GatewayMetaProto.TransferMeta> waitSendBlockList = getTotalTransferMetaBlocks(transferMeta);
            int sendFailSubBlockCount = 0;
            for (GatewayMetaProto.TransferMeta waitSendBlock : waitSendBlockList) {
                List<GatewayMetaProto.TransferMeta> sendFailSubBlockList = sendBlock(waitSendBlock);
                // Print log of sending failed sub blocks
                printSendFailBlocksErrorLog(sendFailSubBlockList);
                sendFailSubBlockCount += (CollectionUtils.isEmpty(sendFailSubBlockList) ? 0 : sendFailSubBlockList.size());
            }
            // Tell the server that all data has been sent
            boolean sendCompleteResult = sendCompleteRequest(transferMeta, sendFailSubBlockCount == 0);
            if (!sendCompleteResult) {
                return ReturnStatusBuilder.sysExc("重试" + FAIL_RETRY_COUNT + "次后发送CK转发完成标识到成员:" + transferMeta.getDst().getMemberName() + " 失败,请确认网络是否正常.", transferMeta.getSessionId());
            }
            if (sendFailSubBlockCount > 0) {
                LOG.error("Data source send fail, session id: " + transferMeta.getSessionId() + ", dbName:" + TransferMetaUtil.getDbName(transferMeta) + ", tableName: " + TransferMetaUtil.getTableName(transferMeta) + ", fail block size: " + sendFailSubBlockCount);
                return ReturnStatusBuilder.sysExc("重试" + FAIL_RETRY_COUNT + "次后发转发CK数据到成员:" + transferMeta.getDst().getMemberName() + " 失败,请确认网络是否正常.", transferMeta.getSessionId());
            }
            return ReturnStatusBuilder.ok(transferMeta.getSessionId());
        } catch (StatusRuntimeException e) {
            LOG.error("Data source send fail, session id: " + transferMeta.getSessionId() + ", dbName:" + TransferMetaUtil.getDbName(transferMeta) + ", tableName: " + TransferMetaUtil.getTableName(transferMeta) + ", exception: ", e);
            GatewayMetaProto.Member dstMember = transferMeta.getDst();
            String dstName = dstMember.getMemberName();
            String endpoint = dstMember.getEndpoint().getIp() + ":" + dstMember.getEndpoint().getPort();
            // Signature issue
            if (GrpcUtil.checkIsSignPermissionExp(e)) {
                return ReturnStatusBuilder.sysExc("成员方[" + dstName + "]对您的签名验证不通过;　存在以下可能性：1、请检查您的公私钥是否匹配以及公钥是否已上报到 union. 2、请确认双方机器系统时间差是否超过5分钟. 3、如果对方的网关使用了nginx做负载转发,请确认对方的nginx配置项[underscores_in_headers]的值是否为[on]", transferMeta.getSessionId());
            }
            if (GrpcUtil.checkIsSslConnectionDisableExp(e)) {
                return ReturnStatusBuilder.sysExc("访问成员方[" + dstName + "]的网关[" + endpoint + "]不通, 其网关启用了SSL通道,请确认CA证书的有效性.", transferMeta.getSessionId());
            }
            if (GrpcUtil.checkIsConnectionDisableExp(e)) {
                return ReturnStatusBuilder.sysExc("访问成员方[" + dstName + "]的网关[" + endpoint + "]不通，请检查网络连接是否正常以及对方网关是否已启动", transferMeta.getSessionId());
            }
            return ReturnStatusBuilder.sysExc("访问成员方[" + dstName + "]的网关[" + endpoint + "]异常：" + e.getMessage(), transferMeta.getSessionId());
        } catch (Exception e) {
            LOG.error("Data source send fail, session id: " + transferMeta.getSessionId() + ", dbName:" + TransferMetaUtil.getDbName(transferMeta) + ", tableName: " + TransferMetaUtil.getTableName(transferMeta) + ", exception: ", e);
            return ReturnStatusBuilder.sysExc("发送异常：" + e.getMessage(), transferMeta.getSessionId());
        } finally {
            LOG.info("Transfer transferMeta duration, dsource type, session id: {}, dbName: {}, tableName: {}, times: {}.", transferMeta.getSessionId(), TransferMetaUtil.getDbName(transferMeta), TransferMetaUtil.getTableName(transferMeta), (System.currentTimeMillis() - startTime));
            // Clear paging parameters
            clearPagingParams();
        }
    }


    /**
     * Send a block (or page) of data
     *
     * @param block Block (or page)
     * @return Send failed sub block list
     */
    private List<GatewayMetaProto.TransferMeta> sendBlock(GatewayMetaProto.TransferMeta block) throws Exception {
        // List of metadata blocks to be sent
        List<GatewayMetaProto.TransferMeta> transferMetaDataList = blockSplitToTransferMetaList(block);
        if (CollectionUtils.isEmpty(transferMetaDataList)) {
            return null;
        }

        for (int i = 0; i <= FAIL_RETRY_COUNT; i++) {
            try {
                transferMetaDataList = sendBlockTransferMetaDataListToRemote(block, transferMetaDataList);
                // Prove that all are sent successfully
                if (CollectionUtils.isEmpty(transferMetaDataList)) {
                    break;
                }
            } catch (StatusRuntimeException e) {
                LOG.error("Message push failed,session id: " + block.getSessionId() + ",exception：", e);
                // If the signature verification fails or the connection fails, and the number of retries exceeds the maximum, the exception can be thrown directly
                if (GrpcUtil.checkIsSignPermissionExp(e) || (i >= FAIL_RETRY_COUNT)) {
                    throw e;
                }

                MemberEntity dstMemberEntity = MemberCache.getInstance().refreshCacheById(block.getDst().getMemberId());
                if (null != dstMemberEntity) {
                    // The destination address needs to be refreshed to avoid the exception caused by the other party updating the gateway address
                    GatewayMetaProto.Member dstMember = block.getDst()
                            .toBuilder()
                            .setEndpoint(EndpointBuilder.create(dstMemberEntity.getGatewayExternalUri()))
                            .build();

                    block = block.toBuilder().setDst(dstMember).build();
                }
            } catch (Exception e) {
                LOG.error("Message push failed, attempt " + i + " frequency", block.getSessionId(), e);
            }
            ThreadUtil.sleep((i + 1) * 500);
        }

        return transferMetaDataList;
    }


    /**
     * Get the list of metadata sub blocks to be sent after block (or page) cutting
     *
     * @param block Block (or page) to be cut
     * @return List of metadata sub blocks that can be sent
     */
    private List<GatewayMetaProto.TransferMeta> blockSplitToTransferMetaList(GatewayMetaProto.TransferMeta block) throws Exception {
        PageInputModel inputModel = new PageInputModel();
        inputModel.setPageNum(block.getSequenceNo());
        inputModel.setPageSize(PAGE_SIZE_THREAD_LOCAL.get());

        //query data base
        PageOutputModel<byte[], byte[]> outputModel = getData(block, inputModel);
        if (null == outputModel) {
            LOG.error("Data source send fail, db name: {}, table name: {}, page no: {} query empty.", TransferMetaUtil.getDbName(block), TransferMetaUtil.getTableName(block), block.getSequenceNo());
            return null;
        }

        // Packaging data
        List<BasicMetaProto.KeyValueData> configDataList = wrapData(outputModel);
        // Split data
        List<List<BasicMetaProto.KeyValueData>> splitConfigDataList = splitConfigDataList(configDataList);
        List<GatewayMetaProto.TransferMeta> transferMetaDataList = new ArrayList<>();
        // Calculate start serial number
        int blockStartSequenceNo = getBlockSequenceNo(block.getSequenceNo());
        for (List<BasicMetaProto.KeyValueData> dataList : splitConfigDataList) {
            GatewayMetaProto.TransferMeta.Builder builder = block.toBuilder();
            builder.setSequenceIsEnd(false)
                    .setSequenceNo(blockStartSequenceNo++)
                    .setTransferStatus(GatewayMetaProto.TransferStatus.PROCESSING)
                    .getContentBuilder().clearKeyValueDatas().addAllKeyValueDatas(dataList);
            GatewayMetaProto.TransferMeta transferMetaNew = builder.build();

            transferMetaDataList.add(transferMetaNew);
        }

        return transferMetaDataList;
    }


    /**
     * Send data to remote
     *
     * @param transferMetaDataList List of metadata to be sent
     * @return Send failed metadata list
     */
    private List<GatewayMetaProto.TransferMeta> sendBlockTransferMetaDataListToRemote(GatewayMetaProto.TransferMeta block, List<GatewayMetaProto.TransferMeta> transferMetaDataList) throws Exception {
        if (CollectionUtils.isEmpty(transferMetaDataList)) {
            return null;
        }
        // Synchronizer
        final SettableFuture<Void> finishFuture = SettableFuture.create();
        // Response result collector
        AsyncResponseCollector asyncResponseCollector = new AsyncResponseCollector();
        ManagedChannel originalChannel = null;
        StreamObserver<GatewayMetaProto.TransferMeta> requestStreamObserver = null;
        boolean isCompleted = false;
        GrpcChannelCache channelCache = GrpcChannelCache.getInstance();
        try {
            boolean tlsEnable = GrpcUtil.checkTlsEnable(block);
            GatewayMetaProto.Member dstMember = block.getDst();
            originalChannel = channelCache.getNonNull(EndpointBuilder.endpointToUri(dstMember.getEndpoint()), tlsEnable, TlsUtil.getAllCertificates(tlsEnable));
            // Set header
            NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceStub asyncClientStub = NetworkDataTransferProxyServiceGrpc.newStub(originalChannel)
                    .withCallCredentials(new ClientCallCredentials(null,
                            new SignVerifyMetadataBuilder(null),
                            new SystemTimestampMetadataBuilder(null)));

            // Get the request flow associated with the server
            requestStreamObserver = asyncClientStub.pushDataSource(new PushDataSourceResponseStreamObserver(finishFuture, asyncResponseCollector));

            for (GatewayMetaProto.TransferMeta transferMetaData : transferMetaDataList) {
                requestStreamObserver.onNext(transferMetaData);
                // If the server has informed that there is an error in data processing, stop sending data
                if (finishFuture.isDone()) {
                    break;
                }
                ThreadUtil.sleep(200);
            }
            requestStreamObserver.onCompleted();
            // Blocking, waiting for the server to notify that the data has been processed, otherwise the message will be discarded
            finishFuture.get();
            isCompleted = true;
            // Returns the list of metadata failed to send
            return getSendFailedTransferMetaList(transferMetaDataList, asyncResponseCollector.getSuccessList());
        } catch (Exception e) {
            if (null != requestStreamObserver) {
                if (isCompleted) {
                    requestStreamObserver.onError(e);
                } else {
                    requestStreamObserver.onCompleted();
                }
            }
            throw e;
        }
    }


    /**
     * Get the list of metadata failed to send
     *
     * @param totalTransferMetaList Total metadata list
     * @param sendSuccessBlocks     Successfully sent metadata list
     * @return Send failed metadata list
     */
    private List<GatewayMetaProto.TransferMeta> getSendFailedTransferMetaList(List<GatewayMetaProto.TransferMeta> totalTransferMetaList, List<GatewayMetaProto.TransferMeta> sendSuccessBlocks) {
        List<GatewayMetaProto.TransferMeta> sendFailedBlocks = new ArrayList<>();
        if (CollectionUtils.isEmpty(totalTransferMetaList)) {
            return sendFailedBlocks;
        }

        if (CollectionUtils.isEmpty(sendSuccessBlocks)) {
            return totalTransferMetaList;
        }

        for (GatewayMetaProto.TransferMeta transferMeta : totalTransferMetaList) {
            boolean isSuccess = false;
            for (GatewayMetaProto.TransferMeta successTransferMeta : sendSuccessBlocks) {
                if (transferMeta.getSequenceNo() == successTransferMeta.getSequenceNo()) {
                    isSuccess = true;
                    break;
                }
            }
            // The metadata sending failed
            if (!isSuccess) {
                sendFailedBlocks.add(transferMeta);
            }
        }

        return sendFailedBlocks;
    }

    /**
     * Send data submission completion request flag
     */
    private boolean sendCompleteRequest(GatewayMetaProto.TransferMeta transferMeta, boolean success) throws Exception {
        GrpcChannelCache channelCache = GrpcChannelCache.getInstance();
        ManagedChannel originalChannel = null;
        for (int i = 0; i <= FAIL_RETRY_COUNT; i++) {
            StreamObserver<GatewayMetaProto.TransferMeta> requestStreamObserver = null;
            boolean isCompleted = false;
            try {
                boolean tlsEnable = GrpcUtil.checkTlsEnable(transferMeta);
                originalChannel = channelCache.getNonNull(EndpointBuilder.endpointToUri(transferMeta.getDst().getEndpoint()), tlsEnable, TlsUtil.getAllCertificates(tlsEnable));
                transferMeta = transferMeta.toBuilder().setTransferStatus(success ? GatewayMetaProto.TransferStatus.COMPLETE : GatewayMetaProto.TransferStatus.ERROR).build();
                // Set header
                NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceStub asyncClientStub = NetworkDataTransferProxyServiceGrpc.newStub(originalChannel)
                        .withCallCredentials(new ClientCallCredentials(null,
                                new SignVerifyMetadataBuilder(null),
                                new SystemTimestampMetadataBuilder(null)));

                // Synchronizer
                final SettableFuture<Void> finishFuture = SettableFuture.create();
                AsyncResponseCollector asyncResponseCollector = new AsyncResponseCollector();
                requestStreamObserver = asyncClientStub.pushDataSource(new PushDataSourceResponseStreamObserver(finishFuture, asyncResponseCollector));

                requestStreamObserver.onNext(transferMeta);
                requestStreamObserver.onCompleted();
                finishFuture.get();
                isCompleted = true;
                if (CollectionUtils.isNotEmpty(asyncResponseCollector.getSuccessList())) {
                    return true;
                }
            } catch (Exception e) {
                LOG.error("Data source send complete request fail, session id: " + transferMeta.getSessionId() + ", retry count:" + i);
                if (null != requestStreamObserver) {
                    if (isCompleted) {
                        requestStreamObserver.onError(e);
                    } else {
                        requestStreamObserver.onCompleted();
                    }
                }
                // If the maximum number of failed retries is exceeded, the exception can be thrown directly
                if (i == FAIL_RETRY_COUNT) {
                    throw e;
                }
            }
        }
        return false;
    }


    /**
     * Total number of request blocks
     *
     * @param transferMeta Data coordinate metadata
     * @return Total number of request blocks
     */
    private List<GatewayMetaProto.TransferMeta> getTotalTransferMetaBlocks(GatewayMetaProto.TransferMeta transferMeta) throws Exception {
        int totalPage = getTotalPage(transferMeta);
        List<GatewayMetaProto.TransferMeta> blocks = new ArrayList<>();
        for (int i = 0; i < totalPage; i++) {
            blocks.add(transferMeta.toBuilder()
                    // Note: the serial number here is the page number
                    .setSequenceNo(i)
                    .setSequenceIsEnd(false)
                    .setTransferStatus(GatewayMetaProto.TransferStatus.PROCESSING)
                    .build());
        }

        return blocks;
    }


    /**
     * Get total pages
     *
     * @param transferMeta Data coordinate metadata
     * @return total pages
     */
    private int getTotalPage(GatewayMetaProto.TransferMeta transferMeta) throws Exception {
        String dbName = TransferMetaUtil.getDbName(transferMeta);
        String tableName = TransferMetaUtil.getTableName(transferMeta);
        int failRetryCount = 3;
        int totalCount = 0;
        PersistentStorage storage = PersistentStorage.getInstance();
        for (int i = 0; i < failRetryCount; i++) {
            totalCount = storage.count(dbName, tableName);
            if (totalCount > 0) {
                break;
            }
            ThreadUtil.sleep((i + 1) * 200);
        }

        LOG.info("Transfer transferMeta dsource type, session id: {}, db name: {}, table name: {}, totalCount: {}.", transferMeta.getSessionId(), dbName, tableName, totalCount);
        return (totalCount + PAGE_SIZE_THREAD_LOCAL.get() - 1) / PAGE_SIZE_THREAD_LOCAL.get();
    }

    /**
     * Get entity data
     */
    private PageOutputModel<byte[], byte[]> getData(GatewayMetaProto.TransferMeta transferMeta, PageInputModel inputModel) throws Exception {
        // databaseName,tableName
        String dbName = TransferMetaUtil.getDbName(transferMeta);
        String tableName = TransferMetaUtil.getTableName(transferMeta);
        int failTryCount = 3;
        PersistentStorage storage = PersistentStorage.getInstance();
        for (int i = 0; i < failTryCount; i++) {
            PageOutputModel<byte[], byte[]> outputModel = storage.getPageBytes(dbName, tableName, inputModel);
            // success
            if (null != outputModel && CollectionUtils.isNotEmpty(outputModel.getData())) {
                return outputModel;
            }

            ThreadUtil.sleep(500 * (i + 1));
        }

        return null;
    }

    /**
     * Packaging data
     */
    private List<BasicMetaProto.KeyValueData> wrapData(PageOutputModel<byte[], byte[]> outputModel) {
        List<BasicMetaProto.KeyValueData> configDataList = new ArrayList<>();
        if (null != outputModel) {
            List<DataItemModel<byte[], byte[]>> dataList = outputModel.getData();
            for (int i = 0; i < dataList.size(); i++) {
                DataItemModel dataItemModel = dataList.get(i);
                byte[] key = (byte[]) dataItemModel.getK();
                byte[] value = (byte[]) dataItemModel.getV();
                configDataList.add(KeyValueDataBuilder.create(key, value));
            }
        }
        return configDataList;
    }

    /**
     * Print log of sending failed sub blocks
     *
     * @param sendFailBlocks Failed block list
     */
    private void printSendFailBlocksErrorLog(List<GatewayMetaProto.TransferMeta> sendFailBlocks) {
        if (CollectionUtils.isEmpty(sendFailBlocks)) {
            return;
        }
        StringBuilder sequenceNos = new StringBuilder();
        sendFailBlocks.forEach(x -> sequenceNos.append(x.getSequenceNo())
                .append(","));
        GatewayMetaProto.TransferMeta firstBlock = sendFailBlocks.get(0);
        LOG.error("Data source send fail blocks, session id: {}, db name: {}, table name: {}, sequences no: {}.", firstBlock.getSessionId(), TransferMetaUtil.getDbName(firstBlock), TransferMetaUtil.getTableName(firstBlock), sequenceNos.toString());
    }

    /**
     * Data split
     */
    private List<List<BasicMetaProto.KeyValueData>> splitConfigDataList(List<BasicMetaProto.KeyValueData> configDataList) {
        List<List<BasicMetaProto.KeyValueData>> resultList = new ArrayList<>();
        int totalSize = configDataList.size();
        // The number of split count
        int splitBlockCount = getSplitBlockCount(totalSize, OPTIMAL_SUB_BLOCK_SIZE_THREAD_LOCAL.get());
        for (int i = 1, toIndex = 0, fromIndex = 0; i <= splitBlockCount; i++) {
            toIndex = OPTIMAL_SUB_BLOCK_SIZE_THREAD_LOCAL.get() * i;
            toIndex = Math.min(toIndex, totalSize);

            resultList.add(configDataList.subList(fromIndex, toIndex));
            fromIndex = toIndex;
        }

        return resultList;
    }


    private int getBlockSequenceNo(int blockNo) {
        int splitBlockCount = getSplitBlockCount(PAGE_SIZE_THREAD_LOCAL.get(), OPTIMAL_SUB_BLOCK_SIZE_THREAD_LOCAL.get());
        return blockNo * splitBlockCount + 1;
    }


    /**
     * Get the number of split count
     *
     * @param totalCount    total count
     * @param perSplitCount per split count
     * @return The number of split count
     */
    private int getSplitBlockCount(int totalCount, int perSplitCount) {
        int model = totalCount % perSplitCount;
        int divide = totalCount / perSplitCount;

        // The number of split count
        return (model == 0 ? divide : divide + 1);
    }

    /**
     * Set paging parameters
     */
    private void setPagingParams(GatewayMetaProto.TransferMeta transferMeta) throws Exception {
        PersistentStorage storage = PersistentStorage.getInstance();
        // Calculate the optimal size of each page
        long byteSize = (long) (configProperties.getPersistentStorageBatchInsertSize() * 1024 * 1024d);
        int sendBlockSize = storage.getCountByByteSize(TransferMetaUtil.getDbName(transferMeta), TransferMetaUtil.getTableName(transferMeta), byteSize);
        OPTIMAL_SUB_BLOCK_SIZE_THREAD_LOCAL.set(sendBlockSize);
        PAGE_SIZE_THREAD_LOCAL.set(sendBlockSize * 3);
    }

    /**
     * Clear paging parameters
     */
    private void clearPagingParams() {
        OPTIMAL_SUB_BLOCK_SIZE_THREAD_LOCAL.remove();
        PAGE_SIZE_THREAD_LOCAL.remove();
    }

}
