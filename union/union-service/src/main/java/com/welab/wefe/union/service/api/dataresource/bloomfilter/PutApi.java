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

package com.welab.wefe.union.service.api.dataresource.bloomfilter;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.dataresource.DataResourcePutInput;
import com.welab.wefe.union.service.service.BloomFilterService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "bloom_filter/put", name = "bloom_filter_put", allowAccessWithSign = true)
public class PutApi extends AbstractApi<PutApi.Input, AbstractApiOutput> {

    @Autowired
    private BloomFilterService bloomFilterService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        bloomFilterService.add(input);
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
