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

package com.welab.wefe.board.service.api.data_resource.bloom_filter;


import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.repository.data_resource.BloomFilterRepository;
import com.welab.wefe.board.service.dto.entity.data_resource.output.BloomFilterOutputModel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Jacky.jiang
 */
@Api(path = "bloom_filter/detail", name = "get BloomFilter detail")
public class BloomFilterDetailApi extends AbstractApi<BloomFilterDetailApi.Input, BloomFilterOutputModel> {

    @Autowired
    BloomFilterRepository bloomFilterRepository;

    @Override
    protected ApiResult<BloomFilterOutputModel> handle(Input input) throws StatusCodeWithException {

        BloomFilterMysqlModel model = bloomFilterRepository.findById(input.id).orElse(null);

        if (model == null) {
            return success();
        }

        BloomFilterOutputModel output = ModelMapper.map(model, BloomFilterOutputModel.class);

        return success(output);

    }

    public static class Input extends AbstractApiInput {
        private String id;

        //region getter/setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }


        //endregion
    }
}
