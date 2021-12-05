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

package com.welab.wefe.union.service.common;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.union.service.UnionService;
import com.welab.wefe.union.service.config.ConfigProperties;
import com.welab.wefe.union.service.contract.*;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderInterface;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * block chain context
 *
 * @author aaron.li
 **/
public class BlockChainContext {
    private static final Logger LOG = LoggerFactory.getLogger(BlockChainContext.class);

    private static BlockChainContext blockChainContext = new BlockChainContext();


    private ConfigProperties configProperties;

    /**
     * SDK
     */
    private BcosSDK bcosSDK = null;

    /**
     * client
     */
    private Client unionClient = null;
    private CryptoSuite unionCryptoSuite = null;
    private CryptoKeyPair unionCryptoKeyPair = null;
    private CnsService unionCnsService = null;


    private BlockChainContext() {
        configProperties = UnionService.CONTEXT.getBean(ConfigProperties.class);
    }


    public static BlockChainContext getInstance() {
        return blockChainContext;
    }

    public Client getClient() {
        return unionClient;
    }

    public CryptoSuite getCryptoSuite() {
        return unionCryptoSuite;
    }


    public boolean init() {
        boolean ret = true;
        try {
            // Initialize SDK common constants
            initSdkCommonConstant();
            LOG.info("Init block chain context success!");
            return true;
        } catch (Exception e) {
            ret = false;
            LOG.error("Init block chain context exception: ", e);
        } finally {
            if (!ret && null != bcosSDK) {
                bcosSDK.stopAll();
                bcosSDK = null;
            }
        }

        return false;
    }


    /**
     * get UnionDataSetContract
     */
    public DataSetContract getUnionDataSetContract() throws StatusCodeWithException {
        return getLatestVersionDataSetContract();
    }

    /**
     * get UnionDataSetMemberPermissionContract
     */
    public DataSetMemberPermissionContract getUnionDataSetMemberPermissionContract() throws StatusCodeWithException {
        return getLatestVersionDataSetMemberPermissionContract();
    }

    /**
     * get UnionTransactionDecoder
     */
    public TransactionDecoderInterface getUnionTransactionDecoder() {
        return new TransactionDecoderService(unionCryptoSuite);
    }


    /**
     * Initialization constant
     */
    private void initSdkCommonConstant() {
        bcosSDK = BcosSDK.build(configProperties.getBlockChainTomlFilePath());
        initUnionSdkCommonConstant();
    }


    /**
     * General constants for initializing the union business
     */
    private void initUnionSdkCommonConstant() {
        unionClient = bcosSDK.getClient(Integer.parseInt(configProperties.getBlockChainUnionGroupId()));
        unionCryptoSuite = unionClient.getCryptoSuite();
        unionCryptoKeyPair = unionCryptoSuite.getCryptoKeyPair();
        unionCnsService = new CnsService(unionClient, unionCryptoKeyPair);
    }


    /**
     * Get the latest version of the Member contract
     */
    public MemberContract getLatestVersionMemberContract() throws StatusCodeWithException {
        String address = getUnionLatestContractAddressByName(unionCnsService, configProperties.getBlockChainUnionMemberContractName());
        return MemberContract.load(address, unionClient, unionCryptoKeyPair);
    }


    /**
     * Get the latest version of the DataSet contract
     */
    public DataSetContract getLatestVersionDataSetContract() throws StatusCodeWithException {
        String address = getUnionLatestContractAddressByName(unionCnsService, configProperties.getBlockChainUnionDataSetContractName());
        return DataSetContract.load(address, unionClient, unionCryptoKeyPair);
    }

    /**
     * Get the latest version of the DataSetMemberPermission contract
     */
    public DataSetMemberPermissionContract getLatestVersionDataSetMemberPermissionContract() throws StatusCodeWithException {
        String address = getUnionLatestContractAddressByName(unionCnsService, configProperties.getBlockChainUnionDataSetMemberPermissionContractName());
        return DataSetMemberPermissionContract.load(address, unionClient, unionCryptoKeyPair);
    }

    /**
     * Get the latest version of the DataSetMemberPermission contract
     */
    public UnionNodeContract getLatestVersionUnionNodeContract() throws StatusCodeWithException {
        String address = getUnionLatestContractAddressByName(unionCnsService, configProperties.getBlockChainUnionNodeContractName());
        return UnionNodeContract.load(address, unionClient, unionCryptoKeyPair);
    }


    /**
     * Get the latest version of the DataSet contract
     */
    public MemberFileInfoContract getLatestVersionMemberFileInfoContract() throws StatusCodeWithException {
        String address = getUnionLatestContractAddressByName(unionCnsService, configProperties.getBlockChainUnionMemberFileInfoContractName());
        return MemberFileInfoContract.load(address, unionClient, unionCryptoKeyPair);
    }

    /**
     * Get the contract address of the latest version of the union business based on the name
     *
     * @param cnsService   CNS service interface of the contract
     * @param contractName
     * @return The latest version of the contract address
     */
    private String getUnionLatestContractAddressByName(CnsService cnsService, String contractName) throws StatusCodeWithException {
        List<CnsInfo> cnsInfoList = null;
        try {
            cnsInfoList = cnsService.selectByName(contractName);
        } catch (Exception e) {
            LOG.error("contractName: " + contractName + "　select cnsInfo list error：", e);
            throw new StatusCodeWithException("contractName: " + contractName + "　select cnsInfo list error:" + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        if (CollectionUtils.isEmpty(cnsInfoList)) {
            LOG.error("contractName: " + contractName + "　cnsInfo List is empty");
            throw new StatusCodeWithException("contractName: " + contractName + "cnsInfo List is empty，Please use the CNS method to deploy the corresponding contract to the chain", StatusCode.DATA_NOT_FOUND);
        }
        return cnsInfoList.get(cnsInfoList.size() - 1).getAddress();
    }
}
