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
import com.welab.wefe.common.data.mongodb.entity.union.ext.BloomFilterExtJSON;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataResourceExtJSON;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = MongodbTable.Union.BLOOM_FILTER)
public class BloomFilter extends AbstractBlockChainBusinessModel {
    private String dataResourceId;
    private String hashFunction;
    public BloomFilter(String dataResourceId,String hashFunction){
        this.dataResourceId = dataResourceId;
        this.hashFunction = hashFunction;
    }

    private BloomFilterExtJSON extJson = new BloomFilterExtJSON();


    public String getDataResourceId() {
        return dataResourceId;
    }

    public void setDataResourceId(String dataResourceId) {
        this.dataResourceId = dataResourceId;
    }

    public String getHashFunction() {
        return hashFunction;
    }

    public void setHashFunction(String hashFunction) {
        this.hashFunction = hashFunction;
    }

    public BloomFilterExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(BloomFilterExtJSON extJson) {
        this.extJson = extJson;
    }
}
