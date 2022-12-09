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

package com.welab.wefe.union.service.service;

import com.welab.wefe.common.data.mongodb.entity.union.BloomFilter;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.repo.BloomFilterMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.union.service.api.dataresource.bloomfilter.PutApi;
import com.welab.wefe.union.service.service.contract.BloomFilterContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BloomFilterService extends AbstractDataResource {
    @Autowired
    protected BloomFilterContractService bloomFilterContractService;
    @Autowired
    protected BloomFilterMongoReop bloomFilterMongoReop;

    public void add(PutApi.Input input) throws StatusCodeWithException {
        BloomFilter bloomFilter = bloomFilterMongoReop.findByDataResourceId(input.getDataResourceId());
        DataResource dataResource = dataResourceMongoReop.find(input.getDataResourceId(), input.curMemberId);
        if (dataResource == null) {
            if (bloomFilter == null) {
                bloomFilterContractService.add(new BloomFilter(input.getDataResourceId(), input.getHashFunction()));
                dataResourceContractService.add(transferPutInputToDataResource(input));
            } else {
                dataResourceContractService.add(transferPutInputToDataResource(input));
            }
        } else {
            bloomFilterContractService.updateHashFuntion(input.getDataResourceId(), input.getHashFunction());
            updateDataResource(dataResource, input);
        }
    }

    private DataResource transferPutInputToDataResource(PutApi.Input input) {
        DataResource out = ModelMapper.map(input, DataResource.class);
        out.setMemberId(input.curMemberId);
        out.setCreatedTime(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        out.setUpdatedTime(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date()));
        return out;
    }
}
