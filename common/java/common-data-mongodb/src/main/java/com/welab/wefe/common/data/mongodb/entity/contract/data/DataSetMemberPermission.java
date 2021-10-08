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

package com.welab.wefe.common.data.mongodb.entity.contract.data;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.AbstractBlockChainBusinessModel;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.DATASET_MEMBER_PERMISSION)
public class DataSetMemberPermission extends AbstractBlockChainBusinessModel {

    private String dataSetMemberPermissionId;
    private String dataSetId;
    private String memberId;

    private ExtJSON extJson = new ExtJSON();

    public static class ExtJSON {
    }

    public DataSetMemberPermission() {
    }

    public DataSetMemberPermission(String dataSetId, String memberId) {
        this.dataSetId = dataSetId;
        this.memberId = memberId;
    }

    public String getDataSetMemberPermissionId() {
        return dataSetMemberPermissionId;
    }

    public void setDataSetMemberPermissionId(String dataSetMemberPermissionId) {
        this.dataSetMemberPermissionId = dataSetMemberPermissionId;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public ExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(ExtJSON extJson) {
        this.extJson = extJson;
    }
}
