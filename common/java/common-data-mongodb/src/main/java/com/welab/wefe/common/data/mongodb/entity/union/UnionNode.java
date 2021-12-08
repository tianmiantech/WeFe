/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractBlockChainBusinessModel;
import com.welab.wefe.common.data.mongodb.entity.union.ext.UnionNodeExtJSON;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.UNION_NODE)
public class UnionNode extends AbstractBlockChainBusinessModel {
    private String nodeId;
    private String blockchainNodeId;
    private String baseUrl;
    private String organizationName;
    private String lostContact;
    private String contactEmail;
    private String priorityLevel;
    private String version;
    private String publicKey;
    private String enable;
    private UnionNodeExtJSON extJson = new UnionNodeExtJSON();

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getBlockchainNodeId() {
        return blockchainNodeId;
    }

    public void setBlockchainNodeId(String blockchainNodeId) {
        this.blockchainNodeId = blockchainNodeId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getLostContact() {
        return lostContact;
    }

    public void setLostContact(String lostContact) {
        this.lostContact = lostContact;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public UnionNodeExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(UnionNodeExtJSON extJson) {
        this.extJson = extJson;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
