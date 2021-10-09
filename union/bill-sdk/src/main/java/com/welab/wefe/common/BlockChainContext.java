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

package com.welab.wefe.common;

import com.welab.wefe.configuration.Config;
import com.welab.wefe.contract.BillContract;
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
 * @author aaron.li
 **/
public class BlockChainContext {
    private static final Logger LOG = LoggerFactory.getLogger(BlockChainContext.class);

    /**
     * SDK
     */
    private BcosSDK bcosSDK = null;


    private Client client = null;
    private CryptoSuite cryptoSuite = null;
    private CryptoKeyPair cryptoKeyPair = null;
    private CnsService cnsService = null;
    private int groupId;


    private BillContract billContract = null;

    public BlockChainContext(int groupId) {
        this.groupId = groupId;
    }

    public Client getClient() {
        return client;
    }

    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }



    public BlockChainContext init() throws Exception {
        try {
            bcosSDK = BcosSDK.build(Config.BLOCK_CHAIN_TOML_FILE_PATH);
            initUnionSdkCommonConstant();
            LOG.info("Init block chain context group " + groupId + " success!");
            return this;
        } catch (Exception e) {
            LOG.error("Init block chain context group " + groupId + " exception: ", e);
            throw new Exception(e);
        } finally {
            if (null != bcosSDK) {
                bcosSDK.stopAll();
                bcosSDK = null;
            }
        }

    }




    public TransactionDecoderInterface getTransactionDecoder() {
        return new TransactionDecoderService(cryptoSuite);
    }



    private void initUnionSdkCommonConstant() {
        client = bcosSDK.getClient(groupId);
        cryptoSuite = client.getCryptoSuite();
        cryptoKeyPair = cryptoSuite.getCryptoKeyPair();
        cnsService = new CnsService(client, cryptoKeyPair);
    }


    public BillContract getLatestVersionBillContract() throws Exception {
        String address = getLatestContractAddressByName(cnsService, Config.BLOCK_CHAIN_BILL_CONTRACT_NAME);
        return BillContract.load(address, client, cryptoKeyPair);
    }


    /**
     *
     * Obtain the latest version of the contract address according to the name
     *
     * @param cnsService   Contracted CNS service interface
     * @param contractName 　
     * @return Latest contract address
     */
    private String getLatestContractAddressByName(CnsService cnsService, String contractName) throws Exception {
        List<CnsInfo> cnsInfoList = null;
        try {
            cnsInfoList = cnsService.selectByName(contractName);
        } catch (Exception e) {
            LOG.error("getLatestContractAddressByName error contractName: " + contractName, e);
        }

        if (CollectionUtils.isEmpty(cnsInfoList)) {
            LOG.error("getLatestContractAddressByName cnsInfoList is empty contractName: " + contractName);
        }
        return cnsInfoList.get(cnsInfoList.size() - 1).getAddress();
    }

}
