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

package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractBlockChainBusinessModel;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetMemberPermissionExtJSON;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.DATASET_MEMBER_PERMISSION)
public class DataSetMemberPermission extends AbstractBlockChainBusinessModel {

    private String dataSetMemberPermissionId;
    private String dataSetId;
    private String memberId;

    private DataSetMemberPermissionExtJSON extJson;



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

    public DataSetMemberPermissionExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(DataSetMemberPermissionExtJSON extJson) {
        this.extJson = extJson;
    }
}
