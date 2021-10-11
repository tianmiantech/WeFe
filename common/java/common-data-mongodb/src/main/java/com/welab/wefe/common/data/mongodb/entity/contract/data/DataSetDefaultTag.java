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

package com.welab.wefe.common.data.mongodb.entity.contract.data;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.AbstractBlockChainBusinessModel;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.DATA_SET_DEFAULT_TAG)
public class DataSetDefaultTag extends AbstractBlockChainBusinessModel {
    private String tagId = UUID.randomUUID().toString().replaceAll("-", "");
    private String tagName;
    private DataSetDefaultTag.ExtJSON extJson = new DataSetDefaultTag.ExtJSON();

    public static class ExtJSON {
    }

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


    public ExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(ExtJSON extJson) {
        this.extJson = extJson;
    }
}
