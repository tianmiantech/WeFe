/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.tool;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderInterface;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;

/**
 * Data sync context
 *
 * @author aaron.li
 **/
public class DataSyncContext {

    /**
     * Blockchain client
     */
    private Client client;

    private CnsService cnsService;
    /**
     * Transaction decoder
     */
    private TransactionDecoderInterface decoder;
    /**
     * The group ID corresponding to the blockchain client
     */
    private Integer groupId;

    public static DataSyncContext create(Client client) {
        DataSyncContext dataSyncContext = new DataSyncContext();
        dataSyncContext.client = client;
        dataSyncContext.decoder = new TransactionDecoderService(client.getCryptoSuite());
        dataSyncContext.groupId = client.getGroupId();

        dataSyncContext.cnsService = new CnsService(dataSyncContext.client, dataSyncContext.client.getCryptoSuite().getCryptoKeyPair());

        return dataSyncContext;
    }


    public Client getClient() {
        return client;
    }

    public CnsService getCnsService() {
        return cnsService;
    }

    public void setCnsService(CnsService cnsService) {
        this.cnsService = cnsService;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public TransactionDecoderInterface getDecoder() {
        return decoder;
    }

    public void setDecoder(TransactionDecoderInterface decoder) {
        this.decoder = decoder;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
}
