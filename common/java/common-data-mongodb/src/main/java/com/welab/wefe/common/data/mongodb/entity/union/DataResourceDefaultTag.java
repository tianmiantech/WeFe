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
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataResourceDefaultTagExtJSON;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.DATA_RESOURCE_DEFAULT_TAG)
public class DataResourceDefaultTag extends AbstractBlockChainBusinessModel {
    private String tagId = UUID.randomUUID().toString().replaceAll("-", "");
    private String tagName;
    private DataResourceType dataResourceType;
    private DataResourceDefaultTagExtJSON extJson = new DataResourceDefaultTagExtJSON();


    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public DataResourceType getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(DataResourceType dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    public DataResourceDefaultTagExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(DataResourceDefaultTagExtJSON extJson) {
        this.extJson = extJson;
    }
}
