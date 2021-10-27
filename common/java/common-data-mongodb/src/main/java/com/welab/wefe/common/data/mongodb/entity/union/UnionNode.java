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
    private String unionNodeId = UUID.randomUUID().toString().replaceAll("-", "");
    private String sign;
    private String unionBaseUrl;
    private String organizationName;
    private String enable;
    private UnionNodeExtJSON extJson;


    public String getUnionNodeId() {
        return unionNodeId;
    }

    public void setUnionNodeId(String unionNodeId) {
        this.unionNodeId = unionNodeId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getUnionBaseUrl() {
        return unionBaseUrl;
    }

    public void setUnionBaseUrl(String unionBaseUrl) {
        this.unionBaseUrl = unionBaseUrl;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
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
}
