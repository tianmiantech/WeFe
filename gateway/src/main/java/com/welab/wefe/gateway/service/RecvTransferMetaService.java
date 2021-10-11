/**
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

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.cache.MemberBlacklistCache;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.cache.RecvTransferMetaCache;
import com.welab.wefe.gateway.cache.RecvTransferMetaCountDownLatchCache;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.service.base.AbstractRecvTransferMetaCachePersistentService;
import com.welab.wefe.gateway.service.base.AbstractRecvTransferMetaService;
import com.welab.wefe.gateway.service.processors.ProcessorContext;
import com.welab.wefe.gateway.util.ActionProcessorMappingUtil;
import com.welab.wefe.gateway.util.ReturnStatusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Handle the data submitted by the remote end, and provide a pull data interface to the client (flow or board)
 *
 * @author aaron.li
 **/
@Service
public class RecvTransferMetaService extends AbstractRecvTransferMetaService {
    private final Logger LOG = LoggerFactory.getLogger(RecvTransferMetaService.class);

    @Autowired
    private AbstractRecvTransferMetaCachePersistentService mReceivedTransferMateCachePersistent;

    @Override
    public GatewayMetaProto.TransferMeta recv(GatewayMetaProto.TransferMeta transferMeta) {
        RecvTransferMetaCache receivedTransferMateCache = RecvTransferMetaCache.getInstance();
        GatewayMetaProto.TransferMeta cacheData = receivedTransferMateCache.get(transferMeta.getSessionId());
        // If completed, return directly
        if (null != cacheData) {
            GatewayMetaProto.TransferStatus status = cacheData.getTransferStatus();
            if (GatewayMetaProto.TransferStatus.COMPLETE.equals(status) || GatewayMetaProto.TransferStatus.ERROR.equals(status)) {
                // Since the memory does not store the content body, it is necessary to take it again from the persistence layer
                cacheData = mReceivedTransferMateCachePersistent.get(cacheData).toBuilder().build();
                deleteCache(cacheData);
                LOG.info("Client recv data complete ==================> sessionId:" + cacheData.getSessionId());
                return cacheData;
            }
        }

        // Set blocking latch
        CountDownLatch countDownLatch = RecvTransferMetaCountDownLatchCache.getInstance().closeCountDownLatch(transferMeta.getSessionId());
        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Recv count down latch await exception:", e);
        }
        cacheData = receivedTransferMateCache.get(transferMeta.getSessionId());
        if (null == cacheData) {
            cacheData = transferMeta.toBuilder().setTransferStatus(GatewayMetaProto.TransferStatus.PROCESSING).build();
        } else if (GatewayMetaProto.TransferStatus.COMPLETE.equals(cacheData.getTransferStatus())
                || GatewayMetaProto.TransferStatus.ERROR.equals(cacheData.getTransferStatus())) {
            cacheData = mReceivedTransferMateCachePersistent.get(cacheData).toBuilder().build();
            LOG.info("Client recv data complete ==================> sessionId :" + cacheData.getSessionId());
            deleteCache(cacheData);
        }
        return cacheData;
    }

    @Override
    public GatewayMetaProto.TransferMeta checkStatusNow(GatewayMetaProto.TransferMeta transferMeta) {
        LOG.info("RecvTransferMetaService checkStatusNow called：" + transferMeta.toString());
        RecvTransferMetaCache receivedTransferMateCache = RecvTransferMetaCache.getInstance();
        GatewayMetaProto.TransferMeta cacheData = receivedTransferMateCache.get(transferMeta.getSessionId());
        if (null == cacheData) {
            cacheData = transferMeta.toBuilder().setTransferStatus(GatewayMetaProto.TransferStatus.PROCESSING).build();
        }
        return cacheData;
    }

    @Override
    public BasicMetaProto.ReturnStatus doHandle(GatewayMetaProto.TransferMeta transferMeta) {
        // Check the validity of the received message
        BasicMetaProto.ReturnStatus returnStatus = check(transferMeta);
        if (!ReturnStatusUtil.ok(returnStatus)) {
            return returnStatus;
        }
        // Old version of action field
        String action = transferMeta.getAction();
        // New version of processor field
        String processor = transferMeta.getProcessor();
        // If the action is not empty and the processor is empty, it means that it is a request submitted by an old version of the client,
        // and the action shall prevail
        if (StringUtil.isNotEmpty(action) && StringUtil.isEmpty(processor)) {
            processor = ActionProcessorMappingUtil.getProcessorByAction(action);
            transferMeta = transferMeta.toBuilder().setProcessor(processor).build();
        }

        // Execute business processing
        try {
            returnStatus = ProcessorContext.remoteExecute(transferMeta);
        } catch (Exception e) {
            LOG.error("RecvTransferMetaService doHandle method exception: ", e);
            returnStatus = ReturnStatusBuilder.sysExc("对端业务处理异常：" + e.getMessage(), transferMeta.getSessionId());
        }
        return returnStatus;
    }


    /**
     * Delete cache
     */
    private void deleteCache(GatewayMetaProto.TransferMeta transferMeta) {
        String sessionId = transferMeta.getSessionId();
        RecvTransferMetaCountDownLatchCache countDownLatchCache = RecvTransferMetaCountDownLatchCache.getInstance();
        RecvTransferMetaCache receivedTransferMateCache = RecvTransferMetaCache.getInstance();
        countDownLatchCache.removeCountDownLatch(sessionId);
        receivedTransferMateCache.remove(sessionId);
        mReceivedTransferMateCachePersistent.delete(transferMeta);
    }

    /**
     * Check the validity of the received message
     *
     * @param transferMeta Received message
     */
    private BasicMetaProto.ReturnStatus check(GatewayMetaProto.TransferMeta transferMeta) {
        GatewayMetaProto.Member srcMember = transferMeta.getSrc();
        GatewayMetaProto.Member dstMember = transferMeta.getDst();
        String dstEndpointStr = (dstMember.getEndpoint().getIp() + ":" + dstMember.getEndpoint().getPort());
        MemberEntity selfMember = MemberCache.getInstance().getSelfMember();
        // The destination memberid received and sent is not equal to its own ID, which indicates that there is an error in forwarding the message (such as an error in IP address configuration)
        if (!selfMember.getId().equals(dstMember.getMemberId())) {
            return ReturnStatusBuilder.paramError("成员[" + selfMember.getName() + "]接收到了来自成员[" + srcMember.getMemberName()
                    + "]的消息，但发现该消息的接收方理应是成员[" + dstMember.getMemberName() + "]，因此拒收该消息. 请确认网关地址[" + dstEndpointStr + "]是否属于成员[" + dstMember.getMemberName() + "].");
        }

        // The message sender exists in the blacklist and refuses to receive the message
        if (MemberBlacklistCache.getInstance().isExistBlacklist(srcMember.getMemberId())) {
            return ReturnStatusBuilder.paramError("您已被成员[" + dstMember.getMemberName() + "]拉黑，对方拒收该消息.");
        }


        return ReturnStatusBuilder.ok();
    }

}
