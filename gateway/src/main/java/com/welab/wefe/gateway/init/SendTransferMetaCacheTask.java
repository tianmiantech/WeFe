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

package com.welab.wefe.gateway.init;

import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.cache.SendTransferMetaCache;
import com.welab.wefe.gateway.service.MessageService;
import com.welab.wefe.gateway.service.base.AbstractSendTransferMetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forward message task
 *
 * @author aaron.li
 **/
public class SendTransferMetaCacheTask extends Thread {
    private final Logger LOG = LoggerFactory.getLogger(SendTransferMetaCacheTask.class);

    @Override
    public void run() {
        // First sleep for 5 seconds to prevent the GRPC service from triggering the grpc call before it is initialized,
        // because this method is invoked before starting the GRPC service.
        ThreadUtil.sleep(5000);
        boolean flag = true;
        while (flag && !InitRpcServer.SERVER_IS_SHUTDOWN) {
            SendTransferMetaCache sendTransferMetaCache = SendTransferMetaCache.getInstance();
            GatewayMetaProto.TransferMeta transferMeta = sendTransferMetaCache.poll();
            if (null == transferMeta) {
                ThreadUtil.sleep(50);
                continue;
            }

            new Thread(() -> {
                try {
                    LOG.info("Start forwarding cached messages, session id：{}", transferMeta.getSessionId());
                    GatewayServer.CONTEXT.getBean(AbstractSendTransferMetaService.class).doHandleCache(transferMeta);
                    LOG.info("Forwarding cache message complete, session id：{}", transferMeta.getSessionId());
                } catch (Exception e) {
                    LOG.error("Forwarding cache message exception：", e);
                    GatewayServer.CONTEXT.getBean(MessageService.class).saveError("转发消息失败", e.getMessage(), transferMeta);
                }
            }).start();

        }
    }
}
