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

package com.welab.wefe.bo.contract;

import java.util.List;

/**
 * contract information
 *
 * @author aaron.li
 **/
public class ContractInfo {
    /**
     * contract name
     */
    private String contractName;
    /**
     * Binary
     */
    private String binary;
    /**
     * ABI
     */
    private String abi;
    /**
     * version
     */
    private short version;
    /**
     * abi's hash value
     */
    private String abiHash;
    /**
     * Method info list
     */
    private List<MethodMetaInfo> methodMetaInfoList;
    /**
     * Event Info list
     */
    private List<EventMetaInfo> eventMetaInfoList;

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public String getAbiHash() {
        return abiHash;
    }

    public void setAbiHash(String abiHash) {
        this.abiHash = abiHash;
    }

    public List<MethodMetaInfo> getMethodMetaInfoList() {
        return methodMetaInfoList;
    }

    public void setMethodMetaInfoList(List<MethodMetaInfo> methodMetaInfoList) {
        this.methodMetaInfoList = methodMetaInfoList;
    }

    public List<EventMetaInfo> getEventMetaInfoList() {
        return eventMetaInfoList;
    }

    public void setEventMetaInfoList(List<EventMetaInfo> eventMetaInfoList) {
        this.eventMetaInfoList = eventMetaInfoList;
    }
}
