/*
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

package com.welab.wefe.union.service.api.dataresource.bloomfilter;

import com.welab.wefe.common.data.mongodb.entity.union.BloomFilter;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.repo.BloomFilterMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.api.dataresource.dataset.AbstractDatResourcePutApi;
import com.welab.wefe.union.service.dto.dataresource.DataResourcePutInput;
import com.welab.wefe.union.service.mapper.BloomFilterMapper;
import com.welab.wefe.union.service.service.BloomFilterContractService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "bloom_filter/put", name = "bloom_filter_put", rsaVerify = true, login = false)
public class PutApi extends AbstractDatResourcePutApi<PutApi.Input, AbstractApiOutput> {
    @Autowired
    protected BloomFilterContractService bloomFilterContractService;
    protected BloomFilterMongoReop bloomFilterMongoReop;

    protected BloomFilterMapper bloomFilterMapper = Mappers.getMapper(BloomFilterMapper.class);

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        BloomFilter bloomFilter = bloomFilterMongoReop.findByDataResourceId(input.getDataResourceId());
        DataResource dataResource = dataResourceMongoReop.find(input.getDataResourceId(), input.getCurMemberId());
        if (dataResource == null) {
            if (bloomFilter == null) {
                bloomFilterContractService.add(new BloomFilter(input.getDataResourceId(), input.getHashFunction()));
            } else {
                dataResourceContractService.add(bloomFilterMapper.transferPutInputToDataResource(input));
            }
        } else {
            bloomFilterContractService.updateHashFuntion(input.getDataResourceId(), input.getHashFunction());
            updateDataResource(dataResource, input);
        }

        return success();
    }

    public static class Input extends DataResourcePutInput {
        private String hashFunction;

        public String getHashFunction() {
            return hashFunction;
        }

        public void setHashFunction(String hashFunction) {
            this.hashFunction = hashFunction;
        }
    }
}
