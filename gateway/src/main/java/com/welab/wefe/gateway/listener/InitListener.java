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

package com.welab.wefe.gateway.listener;

import com.welab.wefe.gateway.config.ConfigProperties;
import com.welab.wefe.gateway.init.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Initialize the data listener (such as member information loading, blacklist loading, grpc service...)
 *
 * @author aaron.li
 **/
@Component
public class InitListener implements ApplicationListener<ApplicationStartedEvent> {
    private final Logger LOG = LoggerFactory.getLogger(InitListener.class);

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private InitRpcServer initRpcServer;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // Load member information to cache
        LoadMemberToCache.load();
        // Load IP whitelist to cache
        loadSystemConfigInfo();
        // Load received data to caceh
        LoadRecvTransferMateToCache.load();
        // Load metadata message to be forwarded to cache
        LoadSendTransferMetaToCache.load();
        // Load member blacklist to cache
        LoadMemberBlacklistToCache.load();
        // Start the forward message task
        new SendTransferMetaCacheTask().start();
        // Start RPC service; Note: this method will block, so it should be put to the end
        startRpcServer();
    }

    private void startRpcServer() {
        try {
            LOG.info("start rpc server, port[{}].", configProperties.getRpcServerPort());
            initRpcServer.start();
        } catch (Exception e) {
            LOG.error("start rpc server fail, system exit: ", e);
            System.exit(-1);
        }
    }

    private void loadSystemConfigInfo() {
        if (!LoadSystemConfigToCache.load()) {
            LOG.error("Load system config to cache fail, system exit.");
            System.exit(-1);
        }
    }
}
