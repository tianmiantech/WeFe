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

import com.google.common.util.concurrent.SettableFuture;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.data.storage.service.StorageService;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.NetworkDataTransferProxyServiceGrpc;
import com.welab.wefe.gateway.api.streammessage.PushDataResponseStreamObserver;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.ConfigDataBuilder;
import com.welab.wefe.gateway.common.EndpointBuilder;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.config.ConfigProperties;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.interceptor.SignVerifyClientInterceptor;
import com.welab.wefe.gateway.interceptor.SystemTimestampVerifyClientInterceptor;
import com.welab.wefe.gateway.service.base.AbstractTransferMetaDataSource;
import com.welab.wefe.gateway.util.GrpcUtil;
import com.welab.wefe.gateway.util.TransferMetaUtil;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
     * Number of sub blocks that can tolerate sending failure.That is, if the number of sub blocks failed to send exceeds this value,
     * the remaining blocks (or pages) will not continue to be sent (negative number means unlimited)
     */
    private static final int FAIL_TOLERATE_SUB_BLOCK_COUNT = 1;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private StorageService storageService;

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
                if (FAIL_TOLERATE_SUB_BLOCK_COUNT >= 0 && sendFailSubBlockCount > FAIL_TOLERATE_SUB_BLOCK_COUNT) {
                    LOG.error("Data source send fail, session id: " + transferMeta.getSessionId() + ", dbName:" + TransferMetaUtil.getDbName(transferMeta) + ", tableName: " + TransferMetaUtil.getTableName(transferMeta) + ", exceeded the number of tolerable failures.");
                    return ReturnStatusBuilder.sysExc("发送数据失败, 超过了可容忍失败数量", waitSendBlock.getSessionId());
                }
            }
            if (sendFailSubBlockCount > 0) {
                LOG.error("Data source send fail, session id: " + transferMeta.getSessionId() + ", dbName:" + TransferMetaUtil.getDbName(transferMeta) + ", tableName: " + TransferMetaUtil.getTableName(transferMeta) + ", fail block size: " + sendFailSubBlockCount);
            }
            // Tell the server that all data has been sent
            boolean sendCompleteResult = sendCompleteRequest(transferMeta);
            return sendCompleteResult ? ReturnStatusBuilder.ok(transferMeta.getSessionId()) : ReturnStatusBuilder.sysExc("发送完成通知消息失败", transferMeta.getSessionId());
        } catch (StatusRuntimeException e) {
            LOG.error("Data source send fail, session id: " + transferMeta.getSessionId() + ", dbName:" + TransferMetaUtil.getDbName(transferMeta) + ", tableName: " + TransferMetaUtil.getTableName(transferMeta) + ", exception: ", e);
            GatewayMetaProto.Member dstMember = transferMeta.getDst();
            String dstName = dstMember.getMemberName();
            String endpoint = dstMember.getEndpoint().getIp() + ":" + dstMember.getEndpoint().getPort();
            // Signature issue
            if (GrpcUtil.checkIsSignPermissionExp(e)) {
                return ReturnStatusBuilder.sysExc("成员方[" + dstName + "]对您的签名验证不通过，请检查您的公私钥是否匹配以及公钥是否已上报", transferMeta.getSessionId());
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
    private List<GatewayMetaProto.TransferMeta> sendBlock(GatewayMetaProto.TransferMeta block) {
        // List of metadata blocks to be sent
        List<GatewayMetaProto.TransferMeta> transferMetaDataList = blockSplitToTransferMetaList(block);
        if (CollectionUtils.isEmpty(transferMetaDataList)) {
            return null;
        }

        int failRetryCount = 4;
        for (int i = 0; i <= failRetryCount; i++) {
            try {
                transferMetaDataList = sendBlockTransferMetaDataListToRemote(block, transferMetaDataList);
                // Prove that all are sent successfully
                if (CollectionUtils.isEmpty(transferMetaDataList)) {
                    break;
                }
            } catch (StatusRuntimeException e) {
                LOG.error("Message push failed,session id: " + block.getSessionId() + ",exception：", e);
                // If the signature verification fails or the connection fails, and the number of retries exceeds the maximum, the exception can be thrown directly
                if (GrpcUtil.checkIsSignPermissionExp(e) || (i >= failRetryCount)) {
                    throw e;
                }

                MemberEntity dstMemberEntity = MemberCache.getInstance().refreshCacheById(block.getDst().getMemberId());
                if (null != dstMemberEntity) {
                    // The destination address needs to be refreshed to avoid the exception caused by the other party updating the gateway address
                    GatewayMetaProto.Member dstMember = block.getDst()
                            .toBuilder()
                            .setEndpoint(EndpointBuilder.create(dstMemberEntity.getIp(), dstMemberEntity.getPort()))
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
    private List<GatewayMetaProto.TransferMeta> blockSplitToTransferMetaList(GatewayMetaProto.TransferMeta block) {
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
        List<GatewayMetaProto.ConfigData> configDataList = wrapData(outputModel);
        // Split data
        List<List<GatewayMetaProto.ConfigData>> splitConfigDataList = splitConfigDataList(configDataList);
        List<GatewayMetaProto.TransferMeta> transferMetaDataList = new ArrayList<>();
        // Calculate start serial number
        int blockStartSequenceNo = getBlockSequenceNo(block.getSequenceNo());
        for (List<GatewayMetaProto.ConfigData> dataList : splitConfigDataList) {
            GatewayMetaProto.TransferMeta.Builder builder = block.toBuilder();
            builder.setSequenceIsEnd(false)
                    .setSequenceNo(blockStartSequenceNo++)
                    .setTransferStatus(GatewayMetaProto.TransferStatus.PROCESSING)
                    .getContentBuilder().clearConfigDatas().addAllConfigDatas(dataList);
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
    private List<GatewayMetaProto.TransferMeta> sendBlockTransferMetaDataListToRemote(GatewayMetaProto.TransferMeta block, List<GatewayMetaProto.TransferMeta> transferMetaDataList) throws ExecutionException, InterruptedException {
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
        try {
            originalChannel = GrpcUtil.getManagedChannel(block.getDst().getEndpoint());
            // Set client interceptor
            Channel channel = ClientInterceptors.intercept(originalChannel, new SystemTimestampVerifyClientInterceptor(), new SignVerifyClientInterceptor());
            NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceStub asyncClientStub = NetworkDataTransferProxyServiceGrpc.newStub(channel);
            // Get the request flow associated with the server
            requestStreamObserver = asyncClientStub.pushData(new PushDataResponseStreamObserver(finishFuture, asyncResponseCollector));

            for (GatewayMetaProto.TransferMeta transferMetaData : transferMetaDataList) {
                requestStreamObserver.onNext(transferMetaData);
                // If the server has informed that there is an error in data processing, stop sending data
                if (finishFuture.isDone()) {
                    break;
                }
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
        } finally {
            if (null != originalChannel) {
                originalChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            }
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
    private boolean sendCompleteRequest(GatewayMetaProto.TransferMeta transferMeta) throws ExecutionException, InterruptedException {
        // Failed retries count
        int failRetryCount = 4;
        for (int i = 0; i <= failRetryCount; i++) {
            ManagedChannel originalChannel = null;
            StreamObserver<GatewayMetaProto.TransferMeta> requestStreamObserver = null;
            boolean isCompleted = false;
            try {
                transferMeta = transferMeta.toBuilder().setTransferStatus(GatewayMetaProto.TransferStatus.COMPLETE).build();
                originalChannel = GrpcUtil.getManagedChannel(transferMeta.getDst().getEndpoint());
                // Set client interceptor
                Channel channel = ClientInterceptors.intercept(originalChannel, new SystemTimestampVerifyClientInterceptor(), new SignVerifyClientInterceptor());
                NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceStub asyncClientStub = NetworkDataTransferProxyServiceGrpc.newStub(channel);

                // Synchronizer
                final SettableFuture<Void> finishFuture = SettableFuture.create();
                AsyncResponseCollector asyncResponseCollector = new AsyncResponseCollector();
                requestStreamObserver = asyncClientStub.pushData(new PushDataResponseStreamObserver(finishFuture, asyncResponseCollector));

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
                if (i >= failRetryCount) {
                    throw e;
                }
            } finally {
                if (null != originalChannel) {
                    originalChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
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
    private List<GatewayMetaProto.TransferMeta> getTotalTransferMetaBlocks(GatewayMetaProto.TransferMeta transferMeta) {
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
    private int getTotalPage(GatewayMetaProto.TransferMeta transferMeta) {
        String dbName = TransferMetaUtil.getDbName(transferMeta);
        String tableName = TransferMetaUtil.getTableName(transferMeta);
        int failRetryCount = 3;
        int totalCount = 0;
        for (int i = 0; i < failRetryCount; i++) {
            totalCount = storageService.count(dbName, tableName);
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
    private PageOutputModel<byte[], byte[]> getData(GatewayMetaProto.TransferMeta transferMeta, PageInputModel inputModel) {
        // databaseName,tableName
        String dbName = TransferMetaUtil.getDbName(transferMeta);
        String tableName = TransferMetaUtil.getTableName(transferMeta);
        int failTryCount = 3;
        for (int i = 0; i < failTryCount; i++) {
            PageOutputModel<byte[], byte[]> outputModel = storageService.getPageBytes(dbName, tableName, inputModel);
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
    private List<GatewayMetaProto.ConfigData> wrapData(PageOutputModel<byte[], byte[]> outputModel) {
        List<GatewayMetaProto.ConfigData> configDataList = new ArrayList<>();
        if (null != outputModel) {
            List<DataItemModel<byte[], byte[]>> dataList = outputModel.getData();
            for (int i = 0; i < dataList.size(); i++) {
                DataItemModel dataItemModel = dataList.get(i);
                byte[] key = (byte[]) dataItemModel.getK();
                byte[] value = (byte[]) dataItemModel.getV();
                configDataList.add(ConfigDataBuilder.create(key, value));
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
    private List<List<GatewayMetaProto.ConfigData>> splitConfigDataList(List<GatewayMetaProto.ConfigData> configDataList) {
        List<List<GatewayMetaProto.ConfigData>> resultList = new ArrayList<>();
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
        // Calculate the optimal size of each page
        long byteSize = (long) (configProperties.getSendActionConfigBlockSize() * 1024 * 1024d);
        int sendBlockSize = storageService.getCountByByteSize(TransferMetaUtil.getDbName(transferMeta), TransferMetaUtil.getTableName(transferMeta), byteSize);
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
