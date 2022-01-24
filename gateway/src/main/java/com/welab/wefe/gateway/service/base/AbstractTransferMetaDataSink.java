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

package com.welab.wefe.gateway.service.base;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.cache.RecvTransferMetaCache;
import com.welab.wefe.gateway.cache.RecvTransferMetaCountDownLatchCache;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The metadata message is a service interface base class of type dsourceprocessor
 *
 * @author aaron.li
 **/
public abstract class AbstractTransferMetaDataSink {
    @Autowired
    private AbstractRecvTransferMetaCachePersistentService mReceivedTransferMateCachePersistent;


    /**
     * Save data
     *
     * @param transferMeta metadata message
     * @return true or false
     */
    public abstract void sink(GatewayMetaProto.TransferMeta transferMeta) throws Exception;


    /**
     * Update the cache. If it is completed, notify the recv interface that it is completed
     */
    public synchronized void updateCache(GatewayMetaProto.TransferMeta transferMeta) {
        String sessionId = transferMeta.getSessionId();
        RecvTransferMetaCache cache = RecvTransferMetaCache.getInstance();

        // Empty the corresponding data body and put it into the cache to prevent excessive memory occupation
        GatewayMetaProto.Content newContent = transferMeta.getContent().toBuilder()
                .clearConfigDatas().build();
        GatewayMetaProto.TransferMeta tempTransferMeta = transferMeta.toBuilder()
                .setContent(newContent)
                .build();

        // Persistence handle
        StatusCodeWithException statusCodeWithException = mReceivedTransferMateCachePersistent.save(tempTransferMeta);
        if (statusCodeWithException.getStatusCode().equals(StatusCode.SUCCESS)) {
            cache.put(sessionId, tempTransferMeta);
            // Open the latch and notify the client that the data has been received
            if (GatewayMetaProto.TransferStatus.COMPLETE.equals(tempTransferMeta.getTransferStatus()) || GatewayMetaProto.TransferStatus.ERROR.equals(tempTransferMeta.getTransferStatus())) {
                RecvTransferMetaCountDownLatchCache.getInstance().openCountDownLatch(sessionId);
            }
        }
    }

}
