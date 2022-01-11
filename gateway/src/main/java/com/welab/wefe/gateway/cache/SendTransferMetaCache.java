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

package com.welab.wefe.gateway.cache;

import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.service.base.AbstractSendTransferMetaCachePersistentService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Client metadata cache to forward
 *
 * @author aaron.li
 **/
public class SendTransferMetaCache {
    private final Logger LOG = LoggerFactory.getLogger(SendTransferMetaCache.class);

    private static SendTransferMetaCache sendTransferMetaCache = new SendTransferMetaCache();

    private ConcurrentLinkedQueue<GatewayMetaProto.TransferMeta> dataQueue = new ConcurrentLinkedQueue<>();

    private SendTransferMetaCache() {
    }

    public static SendTransferMetaCache getInstance() {
        return sendTransferMetaCache;
    }


    public boolean refreshCache() {
        try {
            AbstractSendTransferMetaCachePersistentService sendTransferMetaCachePersistent = GatewayServer.CONTEXT.getBean(AbstractSendTransferMetaCachePersistentService.class);
            List<GatewayMetaProto.TransferMeta> transferMetaList = sendTransferMetaCachePersistent.findAll();
            if (CollectionUtils.isEmpty(transferMetaList)) {
                return true;
            }
            for (GatewayMetaProto.TransferMeta transferMeta : transferMetaList) {
                dataQueue.add(transferMeta);
            }

            return true;

        } catch (Exception e) {
            LOG.error("SendTransferMetaCache load error: ", e);
            return false;
        }
    }

    public void add(GatewayMetaProto.TransferMeta e) {
        dataQueue.add(e);
    }

    public GatewayMetaProto.TransferMeta poll() {
        return dataQueue.poll();
    }

    public boolean isEmpty() {
        return dataQueue.isEmpty();
    }
}
