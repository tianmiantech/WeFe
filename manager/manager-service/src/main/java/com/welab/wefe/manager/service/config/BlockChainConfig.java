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

package com.welab.wefe.manager.service.config;


import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.manager.service.contract.*;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.Message;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
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

@Configuration
@ConfigurationProperties(prefix = "block.chain")
public class BlockChainConfig {
    private static final Logger log =
            LoggerFactory.getLogger(BlockChainConfig.class);

    public String certPath = "conf";
    private int groupId;
    /* use String in java sdk*/
    private String corePoolSize;
    private String maxPoolSize;
    private String queueCapacity;
    /* use String in java sdk*/
    private String ip = "127.0.0.1";
    private String channelPort = "20200";


    private String memberContractName;
    private String dataSetContractName;
    private String dataSetMemberPermissionContractName;
    private String dataSetDefaultTagContractName;
    private String memberAuthTypeContractName;
    private String unionNodeContractName;
    private String realnameAuthAgreementTemplateContractName;
    private String dataResourceContractName;


    // add channel disconnect
    public static boolean PEER_CONNECTED = true;

    static class BcosSDKChannelMsg implements MsgHandler {
        @Override
        public void onConnect(ChannelHandlerContext ctx) {
            PEER_CONNECTED = true;
            log.info("BcosSDKChannelMsg onConnect:{}, status:{}", ctx.channel().remoteAddress(), PEER_CONNECTED);
        }

        @Override
        public void onMessage(ChannelHandlerContext ctx, Message msg) {
            // not added in message handler, ignore this override
            log.info("BcosSDKChannelMsg onMessage:{}, status:{}", ctx.channel().remoteAddress(), PEER_CONNECTED);
        }

        @Override
        public void onDisconnect(ChannelHandlerContext ctx) {
            PEER_CONNECTED = false;
            log.error("BcosSDKChannelMsg onDisconnect:{}, status:{}", ctx.channel().remoteAddress(), PEER_CONNECTED);
        }
    }

    @Bean
    public BcosSDK getBcosSDK() throws Exception {
        log.info("start init ConfigProperty");
        // cert config, encrypt type
        Map<String, Object> cryptoMaterial = new HashMap<>();
        // cert use conf
        cryptoMaterial.put("certPath", certPath);
        // user no need set this:cryptoMaterial.put("sslCryptoType", encryptType);
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
        log.info("init configOption from configProperty");
        ConfigOption configOption = new ConfigOption(configProperty);
        // init bcosSDK
        log.info("init bcos sdk instance, please check sdk.log");
        BcosSDK bcosSDK = new BcosSDK(configOption);

        log.info("init client version");
        ClientVersion version = bcosSDK.getGroupManagerService().getNodeVersion(ip + ":" + channelPort)
                .getNodeVersion();

        BcosSDKChannelMsg disconnectMsg = new BcosSDKChannelMsg();
        bcosSDK.getChannel().addConnectHandler(disconnectMsg);
        bcosSDK.getChannel().addDisconnectHandler(disconnectMsg);

        return bcosSDK;
    }

    @Bean
    public Client getClient(BcosSDK bcosSDK) {
        Client client = bcosSDK.getClient(groupId);
        return client;
    }

    @Bean
    public CryptoSuite getCryptoSuite(Client client) {
        CryptoSuite cryptoSuite = client.getCryptoSuite();
        return cryptoSuite;
    }

    @Bean
    public CryptoKeyPair getCryptoKeyPair(CryptoSuite cryptoSuite) {
        CryptoKeyPair cryptoKeyPair = cryptoSuite.getCryptoKeyPair();
        return cryptoKeyPair;
    }


    @Bean
    public CnsService getCnsService(Client client, CryptoKeyPair cryptoKeyPair) {
        CnsService cnsService = new CnsService(client, cryptoKeyPair);
        return cnsService;
    }

    @Bean
    public String getCurrentBlockchainNodeId(Client client) {
        String nodeId = client.getNodeIDList().getResult().get(0);
        return nodeId;
    }


    @Bean
    public MemberContract getLatestVersionMemberContract(CnsService cnsService, Client client, CryptoKeyPair cryptoKeyPair) throws StatusCodeWithException {
        String address = getLatestContractAddressByName(cnsService, memberContractName);
        return MemberContract.load(address, client, cryptoKeyPair);
    }


    @Bean
    public DataSetContract getLatestVersionDataSetContract(CnsService cnsService, Client client, CryptoKeyPair cryptoKeyPair) throws StatusCodeWithException {
        String address = getLatestContractAddressByName(cnsService, dataSetContractName);
        return DataSetContract.load(address, client, cryptoKeyPair);
    }


    @Bean
    public DataSetMemberPermissionContract getLatestVersionDataSetMemberPermissionContract(CnsService cnsService, Client client, CryptoKeyPair cryptoKeyPair) throws StatusCodeWithException {
        String address = getLatestContractAddressByName(cnsService, dataSetMemberPermissionContractName);
        return DataSetMemberPermissionContract.load(address, client, cryptoKeyPair);
    }


    @Bean
    public DataSetDefaultTagContract getLatestVersionDataSetDefaultTagContract(CnsService cnsService, Client client, CryptoKeyPair cryptoKeyPair) throws StatusCodeWithException {
        String address = getLatestContractAddressByName(cnsService, dataSetDefaultTagContractName);
        return DataSetDefaultTagContract.load(address, client, cryptoKeyPair);
    }



    @Bean
    public MemberAuthTypeContract getLatestVersionMemberAuthTypeContract(CnsService cnsService, Client client, CryptoKeyPair cryptoKeyPair) throws StatusCodeWithException {
        String address = getLatestContractAddressByName(cnsService, memberAuthTypeContractName);
        return MemberAuthTypeContract.load(address, client, cryptoKeyPair);
    }


    @Bean
    public UnionNodeContract getLatestVersionUnionNodeContract(CnsService cnsService, Client client, CryptoKeyPair cryptoKeyPair) throws StatusCodeWithException {
        String address = getLatestContractAddressByName(cnsService, unionNodeContractName);
        return UnionNodeContract.load(address, client, cryptoKeyPair);
    }



    @Bean
    public RealnameAuthAgreementTemplateContract getLatestVersionRealnameAuthAgreementTemplateContract(CnsService cnsService, Client client, CryptoKeyPair cryptoKeyPair) throws StatusCodeWithException {
        String address = getLatestContractAddressByName(cnsService, realnameAuthAgreementTemplateContractName);
        return RealnameAuthAgreementTemplateContract.load(address, client, cryptoKeyPair);
    }


    @Bean
    public DataResourceContract getLatestVersionDataResourceContract(CnsService cnsService, Client client, CryptoKeyPair cryptoKeyPair) throws StatusCodeWithException {
        String address = getLatestContractAddressByName(cnsService, dataResourceContractName);
        return DataResourceContract.load(address, client, cryptoKeyPair);
    }


    /**
     * Obtain the latest version of the contract address according to the name
     *
     * @param cnsService
     * @param contractName 　
     * @return Latest contract address
     * @throws StatusCodeWithException
     */
    public String getLatestContractAddressByName(CnsService cnsService, String contractName) throws StatusCodeWithException {
        List<CnsInfo> cnsInfoList = null;
        try {
            cnsInfoList = cnsService.selectByName(contractName);
        } catch (Exception e) {
            String msg = contractName + ":Exception in obtaining contract CNS list";
            log.error(msg, e);
            throw new StatusCodeWithException(msg, StatusCode.SYSTEM_ERROR);
        }

        if (CollectionUtils.isEmpty(cnsInfoList)) {
            String msg = contractName + ":Get contract CNS list is empty";
            log.error(msg);
            throw new StatusCodeWithException(msg, StatusCode.DATA_NOT_FOUND);
        }
        return cnsInfoList.get(cnsInfoList.size() - 1).getAddress();
    }

    public String getMemberContractName() {
        return memberContractName;
    }

    public void setMemberContractName(String memberContractName) {
        this.memberContractName = memberContractName;
    }

    public String getDataSetContractName() {
        return dataSetContractName;
    }

    public void setDataSetContractName(String dataSetContractName) {
        this.dataSetContractName = dataSetContractName;
    }

    public String getDataSetMemberPermissionContractName() {
        return dataSetMemberPermissionContractName;
    }

    public void setDataSetMemberPermissionContractName(String dataSetMemberPermissionContractName) {
        this.dataSetMemberPermissionContractName = dataSetMemberPermissionContractName;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
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

    public String getDataSetDefaultTagContractName() {
        return dataSetDefaultTagContractName;
    }

    public void setDataSetDefaultTagContractName(String dataSetDefaultTagContractName) {
        this.dataSetDefaultTagContractName = dataSetDefaultTagContractName;
    }


    public String getMemberAuthTypeContractName() {
        return memberAuthTypeContractName;
    }

    public void setMemberAuthTypeContractName(String memberAuthTypeContractName) {
        this.memberAuthTypeContractName = memberAuthTypeContractName;
    }

    public String getUnionNodeContractName() {
        return unionNodeContractName;
    }

    public void setUnionNodeContractName(String unionNodeContractName) {
        this.unionNodeContractName = unionNodeContractName;
    }


    public String getRealnameAuthAgreementTemplateContractName() {
        return realnameAuthAgreementTemplateContractName;
    }

    public void setRealnameAuthAgreementTemplateContractName(String realnameAuthAgreementTemplateContractName) {
        this.realnameAuthAgreementTemplateContractName = realnameAuthAgreementTemplateContractName;
    }

    public String getDataResourceContractName() {
        return dataResourceContractName;
    }

    public void setDataResourceContractName(String dataResourceContractName) {
        this.dataResourceContractName = dataResourceContractName;
    }
}
