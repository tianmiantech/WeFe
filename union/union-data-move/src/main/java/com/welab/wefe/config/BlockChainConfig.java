package com.welab.wefe.config;


import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.contract.DataResourceContract;
import com.welab.wefe.contract.TableDataSetContract;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Configuration
//@ConfigurationProperties(prefix = "block.chain")
public class BlockChainConfig {
    private static final Logger log =
            LoggerFactory.getLogger(BlockChainConfig.class);

    public String certPath = "conf";
    private int groupId;
    private String corePoolSize;
    private String maxPoolSize;
    private String queueCapacity;
    private String ip = "127.0.0.1";
    private String channelPort = "20200";
    private String dataResourceContractName;
    private String tableDataSetContractName;


    @Bean
    public BcosSDK getBcosSDK() throws Exception {
        log.info("start init ConfigProperty");
        Map<String, Object> cryptoMaterial = new HashMap<>();
        cryptoMaterial.put("certPath", certPath);
        log.info("init cert cryptoMaterial:{}, (using conf as cert path)", cryptoMaterial);

        Map<String, Object> network = new HashMap<>();
        List<String> peers = new ArrayList<>();
        peers.add(ip + ":" + channelPort);
        network.put("peers", peers);
        log.info("init node network property :{}", peers);

        log.info("init thread pool property");
        Map<String, Object> threadPool = new HashMap<>();
        threadPool.put("channelProcessorThreadSize", corePoolSize);
        threadPool.put("receiptProcessorThreadSize", corePoolSize);
        threadPool.put("maxBlockingQueueSize", queueCapacity);
        log.info("init thread pool property:{}", threadPool);

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
    public DataResourceContract getLatestVersionDataResourceContract(CnsService cnsService, Client client, CryptoKeyPair cryptoKeyPair) throws StatusCodeWithException {
        String address = getLatestContractAddressByName(cnsService, dataResourceContractName);
        return DataResourceContract.load(address, client, cryptoKeyPair);
    }

    @Bean
    public TableDataSetContract getLatestVersionTableDataSetContract(CnsService cnsService, Client client, CryptoKeyPair cryptoKeyPair) throws StatusCodeWithException {
        String address = getLatestContractAddressByName(cnsService, tableDataSetContractName);
        return TableDataSetContract.load(address, client, cryptoKeyPair);
    }

    /**
     * 根据名称获取最新版的union业务的合约地址
     *
     * @param cnsService   合约的CNS服务接口
     * @param contractName 　合约名称
     * @return 最新版的合约地址
     * @throws StatusCodeWithException
     */
    public String getLatestContractAddressByName(CnsService cnsService, String contractName) throws StatusCodeWithException {
        List<CnsInfo> cnsInfoList = null;
        try {
            cnsInfoList = cnsService.selectByName(contractName);
        } catch (Exception e) {
            log.error("根据名称: " + contractName + "　获取合约CNS列表异常：", e);
            throw new StatusCodeWithException("根据名称: " + contractName + "　获取合约CNS列表异常:" + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        if (CollectionUtils.isEmpty(cnsInfoList)) {
            log.error("根据名称: " + contractName + "　获取合约CNS列表为空");
            throw new StatusCodeWithException("根据名称: " + contractName + "获取合约为空，请用CNS方式部署相应合约到链上", StatusCode.DATA_NOT_FOUND);
        }
        return cnsInfoList.get(cnsInfoList.size() - 1).getAddress();
    }
}
