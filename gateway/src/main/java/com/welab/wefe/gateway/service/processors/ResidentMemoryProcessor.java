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

package com.welab.wefe.gateway.service.processors;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.base.Processor;
import com.welab.wefe.gateway.cache.RecvTransferMetaCache;
import com.welab.wefe.gateway.cache.RecvTransferMetaCountDownLatchCache;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.service.base.AbstractRecvTransferMetaCachePersistentService;

/**
 * Memory resident processor
 *
 * @author aaron.li
 **/
@Processor(type = GatewayProcessorType.residentMemoryProcessor, desc = "Memory resident processor")
public class ResidentMemoryProcessor extends AbstractProcessor {

    @Override
    public BasicMetaProto.ReturnStatus remoteProcess(GatewayMetaProto.TransferMeta transferMeta) {
        RecvTransferMetaCache receivedTransferMateCache = RecvTransferMetaCache.getInstance();
        try {
            // Set status to completed
            transferMeta = transferMeta.toBuilder().setTransferStatus(GatewayMetaProto.TransferStatus.COMPLETE).build();
            // Persistence
            AbstractRecvTransferMetaCachePersistentService recvTransferMetaCachePersistent = GatewayServer.CONTEXT.getBean(AbstractRecvTransferMetaCachePersistentService.class);
            StatusCodeWithException statusCodeWithException = recvTransferMetaCachePersistent.save(transferMeta);
            if (!statusCodeWithException.getStatusCode().equals(StatusCode.SUCCESS)) {
                return ReturnStatusBuilder.sysExc(statusCodeWithException.getMessage(), transferMeta.getSessionId());
            }
            // The content is not put in the cache to avoid excessive data submitted by the client
            receivedTransferMateCache.put(transferMeta.getSessionId(), transferMeta.toBuilder().clearContent().build());
        } catch (Exception e) {
            LOG.error("ResidentMemoryProcessor fail, exception：", e);
            transferMeta = transferMeta.toBuilder().setTransferStatus(GatewayMetaProto.TransferStatus.ERROR).build();
            receivedTransferMateCache.put(transferMeta.getSessionId(), transferMeta.toBuilder().clearContent().build());

            return ReturnStatusBuilder.sysExc(e.getMessage(), transferMeta.getSessionId());
        } finally {
            notifyClient(transferMeta);
        }

        return ReturnStatusBuilder.ok(transferMeta.getSessionId());
    }

    /**
     * Notify the client that the data has arrived successfully
     */
    private void notifyClient(GatewayMetaProto.TransferMeta transferMeta) {
        RecvTransferMetaCountDownLatchCache.getInstance().openCountDownLatch(transferMeta.getSessionId());
    }
}
