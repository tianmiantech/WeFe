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

package com.welab.wefe.parser;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.BlockchainDataSyncApp;
import com.welab.wefe.common.data.mongodb.entity.union.BloomFilter;
import com.welab.wefe.common.data.mongodb.entity.union.ext.BloomFilterExtJSON;
import com.welab.wefe.common.data.mongodb.repo.BloomFilterMongoReop;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * BloomFilterContract Event information interpreter
 *
 * @author yuxin.zhang
 */
public class BloomFilterContractEventParser extends AbstractParser {
    protected BloomFilterMongoReop bloomFilterMongoReop = BlockchainDataSyncApp.CONTEXT.getBean(BloomFilterMongoReop.class);
    protected BloomFilterExtJSON extJSON;


    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, BloomFilterExtJSON.class) : new BloomFilterExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.BloomFilterEvent.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.BloomFilterEvent.UPDATE_HASH_FUNCTION_EVENT:
                parseUpdateHashFuntionEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            case EventConstant.DELETE_BY_DATA_RESOURCE_ID_EVENT:
                parseDeleteByDataResourceId();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertEvent() {
        BloomFilter bloomFilter = new BloomFilter();
        bloomFilter.setDataResourceId(StringUtil.strTrim2(params.getString(0)));
        bloomFilter.setHashFunction(StringUtil.strTrim2(params.getString(1)));
        bloomFilter.setCreatedTime(StringUtil.strTrim2(params.getString(2)));
        bloomFilter.setUpdatedTime(StringUtil.strTrim2(params.getString(3)));
        bloomFilter.setExtJson(extJSON);
        bloomFilterMongoReop.upsert(bloomFilter);
    }

    private void parseUpdateHashFuntionEvent() throws BusinessException {
        String dataResourceId = eventBO.getEntity().get("data_resource_id").toString();
        String hashFunction = eventBO.getEntity().get("hash_function").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();

        BloomFilter bloomFilter = getBloomFilter(dataResourceId);

        bloomFilter.setHashFunction(hashFunction);
        bloomFilter.setUpdatedTime(updatedTime);
        bloomFilterMongoReop.upsert(bloomFilter);
    }


    private void parseUpdateExtJson() throws BusinessException {
        String dataResourceId = eventBO.getEntity().get("data_resource_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        BloomFilter bloomFilter = getBloomFilter(dataResourceId);
        bloomFilter.setExtJson(extJSON);
        bloomFilter.setUpdatedTime(updatedTime);
        bloomFilterMongoReop.upsert(bloomFilter);
    }

    private void parseDeleteByDataResourceId() {
        String dataResourceId = eventBO.getEntity().get("data_resource_id").toString();
        bloomFilterMongoReop.deleteByDataResourceId(dataResourceId);
    }

    private BloomFilter getBloomFilter(String dataResourceId) throws BusinessException {
        BloomFilter bloomFilter = bloomFilterMongoReop.findByDataResourceId(dataResourceId);
        if (bloomFilter == null) {
            throw new BusinessException("Data does not exist dataResourceId:" + dataResourceId);
        }
        return bloomFilter;
    }

}
