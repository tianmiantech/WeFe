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
import com.welab.wefe.gateway.service.base.AbstractRecvTransferMetaCachePersistentService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Meta message data cache pushed by received remote gateway
 *
 * @author aaron.li
 **/
public class RecvTransferMetaCache {
    private final Logger LOG = LoggerFactory.getLogger(RecvTransferMetaCache.class);

    private static RecvTransferMetaCache recvTransferMetaCache = new RecvTransferMetaCache();

    private static ConcurrentHashMap<String, GatewayMetaProto.TransferMeta> dataMap = new ConcurrentHashMap<>();

    private RecvTransferMetaCache() {
    }

    public static RecvTransferMetaCache getInstance() {
        return recvTransferMetaCache;
    }


    public boolean refreshCache() {
        try {
            AbstractRecvTransferMetaCachePersistentService recvTransferMetaCachePersistent = GatewayServer.CONTEXT.getBean(AbstractRecvTransferMetaCachePersistentService.class);
            List<GatewayMetaProto.TransferMeta> transferMetaList = recvTransferMetaCachePersistent.findAll();
            if (CollectionUtils.isEmpty(transferMetaList)) {
                return true;
            }

            RecvTransferMetaCountDownLatchCache downLatchCache = RecvTransferMetaCountDownLatchCache.getInstance();
            for (GatewayMetaProto.TransferMeta transferMeta : transferMetaList) {
                // Empty the content and put it into memory to avoid the content body being too large
                dataMap.put(transferMeta.getSessionId(), transferMeta.toBuilder().clearContent().setTransferStatus(GatewayMetaProto.TransferStatus.COMPLETE).build());
                // Set the blocking count cache and the setting data is ready
                downLatchCache.openCountDownLatch(transferMeta.getSessionId());
            }
            return true;
        } catch (Exception e) {
            LOG.error("load received transferMeta data from persistent fail：", e);
            return false;
        }
    }

    public void put(String key, GatewayMetaProto.TransferMeta value) {
        dataMap.put(key, value);
    }

    public GatewayMetaProto.TransferMeta get(String key) {
        return dataMap.get(key);
    }

    public GatewayMetaProto.TransferMeta remove(String key) {
        return dataMap.remove(key);
    }

}
