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

package com.welab.wefe.union.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author aaron.li
 **/
@Component
public class ConfigProperties {

    /**
     * toml configuration file path
     */
    @Value("${block.chain.toml.file.path}")
    private String blockChainTomlFilePath;

    @Value("${block.chain.union.member.contract.name}")
    private String blockChainUnionMemberContractName;
    @Value("${block.chain.union.data.set.contract.name}")
    private String blockChainUnionDataSetContractName;
    @Value("${block.chain.union.data.set.member.permission.contract.name}")
    private String blockChainUnionDataSetMemberPermissionContractName;
    @Value("${block.chain.union.node.contract.name}")
    private String blockChainUnionNodeContractName;


    /**
     * The group ID to which the union business belongs
     */
    @Value("${block.chain.union.group.id}")
    private String blockChainUnionGroupId;

    public String getBlockChainTomlFilePath() {
        return blockChainTomlFilePath;
    }

    public void setBlockChainTomlFilePath(String blockChainTomlFilePath) {
        this.blockChainTomlFilePath = blockChainTomlFilePath;
    }

    public String getBlockChainUnionMemberContractName() {
        return blockChainUnionMemberContractName;
    }

    public void setBlockChainUnionMemberContractName(String blockChainUnionMemberContractName) {
        this.blockChainUnionMemberContractName = blockChainUnionMemberContractName;
    }

    public String getBlockChainUnionGroupId() {
        return blockChainUnionGroupId;
    }

    public void setBlockChainUnionGroupId(String blockChainUnionGroupId) {
        this.blockChainUnionGroupId = blockChainUnionGroupId;
    }

    public String getBlockChainUnionDataSetContractName() {
        return blockChainUnionDataSetContractName;
    }

    public void setBlockChainUnionDataSetContractName(String blockChainUnionDataSetContractName) {
        this.blockChainUnionDataSetContractName = blockChainUnionDataSetContractName;
    }

    public String getBlockChainUnionDataSetMemberPermissionContractName() {
        return blockChainUnionDataSetMemberPermissionContractName;
    }

    public void setBlockChainUnionDataSetMemberPermissionContractName(String blockChainUnionDataSetMemberPermissionContractName) {
        this.blockChainUnionDataSetMemberPermissionContractName = blockChainUnionDataSetMemberPermissionContractName;
    }

    public String getBlockChainUnionNodeContractName() {
        return blockChainUnionNodeContractName;
    }

    public void setBlockChainUnionNodeContractName(String blockChainUnionNodeContractName) {
        this.blockChainUnionNodeContractName = blockChainUnionNodeContractName;
    }
}
