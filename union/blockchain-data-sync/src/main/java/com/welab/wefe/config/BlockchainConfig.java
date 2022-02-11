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

/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.welab.wefe.config;


import io.netty.channel.ChannelHandlerContext;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.model.Message;
import org.fisco.bcos.sdk.network.MsgHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * init blockchain bcosSDK
 *
 * @author yuxin.zhang
 **/
@Configuration
@ConfigurationProperties(prefix = "sdk")
public class BlockchainConfig {
    private static final Logger log =
            LoggerFactory.getLogger(BlockchainConfig.class);

    public String certPath = "conf";
    private List<Integer> groupIdList;
    private String corePoolSize;
    private String maxPoolSize;
    private String queueCapacity;

    private String ip = "127.0.0.1";
    private String channelPort = "20200";

    /**
     * add channel disconnect
     */
    public static boolean PEER_CONNECTED = true;

    static class BlockchainChannelMsg implements MsgHandler {
        @Override
        public void onConnect(ChannelHandlerContext chc) {
            PEER_CONNECTED = true;
            log.info("BlockchainChannelMsg onConnect:{}, status:{}", chc.channel().remoteAddress(), PEER_CONNECTED);
        }

        @Override
        public void onMessage(ChannelHandlerContext chc, Message msg) {
            // not added in message handler, ignore this override
            log.info("BlockchainChannelMsg onMessage:{}, status:{}", chc.channel().remoteAddress(), PEER_CONNECTED);
        }

        @Override
        public void onDisconnect(ChannelHandlerContext chc) {
            PEER_CONNECTED = false;
            log.error("BlockchainChannelMsg onDisconnect:{}, status:{}", chc.channel().remoteAddress(), PEER_CONNECTED);
        }
    }

    @Bean
    public BcosSDK getBcosSDK() throws ConfigException {
        log.info("start init ConfigProperty");
        // cert config, encrypt type
        Map<String, Object> cryptoMaterial = new HashMap<>();
        // cert use conf
        cryptoMaterial.put("certPath", certPath);
        log.info("init cert cryptoMaterial:{}, (using conf as cert path)", cryptoMaterial);

        // peers, default one node in front
        Map<String, Object> network = new HashMap<>();
        List<String> peers = new ArrayList<>();
        peers.add(ip + ":" + channelPort);
        network.put("peers", peers);
        log.info("init node network property :{}", peers);

        // thread pool config
        log.info("init thread pool property");
        Map<String, Object> threadPool = new HashMap<>();
        threadPool.put("channelProcessorThreadSize", corePoolSize);
        threadPool.put("receiptProcessorThreadSize", corePoolSize);
        threadPool.put("maxBlockingQueueSize", queueCapacity);
        log.info("init thread pool property:{}", threadPool);

        // init property
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setCryptoMaterial(cryptoMaterial);
        configProperty.setNetwork(network);
        configProperty.setThreadPool(threadPool);
        // init config option
        ConfigOption configOption = new ConfigOption(configProperty);
        // init bcosSDK
        BcosSDK bcosSDK = new BcosSDK(configOption);

        BlockchainChannelMsg disconnectMsg = new BlockchainChannelMsg();
        bcosSDK.getChannel().addConnectHandler(disconnectMsg);
        bcosSDK.getChannel().addDisconnectHandler(disconnectMsg);

        return bcosSDK;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public List<Integer> getGroupIdList() {
        return groupIdList;
    }

    public void setGroupIdList(List<Integer> groupIdList) {
        this.groupIdList = groupIdList;
    }

    public String getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(String corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public String getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(String maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public String getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(String queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getChannelPort() {
        return channelPort;
    }

    public void setChannelPort(String channelPort) {
        this.channelPort = channelPort;
    }
}
