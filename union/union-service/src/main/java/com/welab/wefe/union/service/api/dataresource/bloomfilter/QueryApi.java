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

package com.welab.wefe.union.service.api.dataresource.bloomfilter;

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.repo.BloomFilterMongoReop;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryInput;
import com.welab.wefe.union.service.dto.dataresource.bloomfilter.ApiBloomFilterQueryOutput;
import com.welab.wefe.union.service.mapper.BloomFilterMapper;
import com.welab.wefe.union.service.mapper.DataResourceMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 **/
@Api(path = "bloom_filter/query", name = "bloom_filter_query", rsaVerify = true, login = false)
public class QueryApi extends AbstractApi<ApiDataResourceQueryInput, PageOutput<ApiBloomFilterQueryOutput>> {
    @Autowired
    protected BloomFilterMongoReop bloomFilterMongoReop;


    protected BloomFilterMapper bloomFilterMapper = Mappers.getMapper(BloomFilterMapper.class);

    protected DataResourceMapper dataResourceMapper = Mappers.getMapper(DataResourceMapper.class);

    @Override
    protected ApiResult<PageOutput<ApiBloomFilterQueryOutput>> handle(ApiDataResourceQueryInput input) {
        PageOutput<DataResourceQueryOutput> pageOutput = bloomFilterMongoReop.findCurMemberCanSee(dataResourceMapper.transferInput(input));

        List<ApiBloomFilterQueryOutput> list = pageOutput.getList().stream()
                .map(bloomFilterMapper::transferDetail)
                .collect(Collectors.toList());

        return success(new PageOutput<>(
                pageOutput.getPageIndex(),
                pageOutput.getTotal(),
                pageOutput.getPageSize(),
                pageOutput.getTotalPage(),
                list
        ));
    }

}
