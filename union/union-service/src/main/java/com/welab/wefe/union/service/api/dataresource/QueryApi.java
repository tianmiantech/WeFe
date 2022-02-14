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

package com.welab.wefe.union.service.api.dataresource;

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.data.mongodb.repo.TableDataSetMongoReop;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryInput;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryOutput;
import com.welab.wefe.union.service.mapper.BloomFilterMapper;
import com.welab.wefe.union.service.mapper.DataResourceMapper;
import com.welab.wefe.union.service.mapper.ImageDataSetMapper;
import com.welab.wefe.union.service.mapper.TableDataSetMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 **/
@Api(path = "data_resource/query", name = "data_resource_query", rsaVerify = true, login = false)
public class QueryApi extends AbstractApi<ApiDataResourceQueryInput, PageOutput<ApiDataResourceQueryOutput>> {
    @Autowired
    protected DataResourceMongoReop dataResourceMongoReop;
    @Autowired
    protected ImageDataSetMongoReop imageDataSetMongoReop;
    @Autowired
    protected TableDataSetMongoReop tableDataSetMongoReop;

    protected TableDataSetMapper tableDataSetMapper = Mappers.getMapper(TableDataSetMapper.class);
    protected ImageDataSetMapper imageDataSetMapper = Mappers.getMapper(ImageDataSetMapper.class);
    protected BloomFilterMapper bloomFilterMapper = Mappers.getMapper(BloomFilterMapper.class);
    protected DataResourceMapper dataResourceMapper = Mappers.getMapper(DataResourceMapper.class);

    @Override
    protected ApiResult<PageOutput<ApiDataResourceQueryOutput>> handle(ApiDataResourceQueryInput input) {
        PageOutput<DataResourceQueryOutput> pageOutput = null;
        if (null == input.getDataResourceType() || DataResourceType.BloomFilter.compareTo(input.getDataResourceType()) == 0) {
            pageOutput = dataResourceMongoReop.findCurMemberCanSee(dataResourceMapper.transferInput(input));
        } else if (DataResourceType.ImageDataSet.compareTo(input.getDataResourceType()) == 0) {
            pageOutput = imageDataSetMongoReop.findCurMemberCanSee(dataResourceMapper.transferInput(input));
        } else if (DataResourceType.TableDataSet.compareTo(input.getDataResourceType()) == 0) {
            pageOutput = tableDataSetMongoReop.findCurMemberCanSee(dataResourceMapper.transferInput(input));
        }

        List<ApiDataResourceQueryOutput> list = pageOutput.getList().stream()
                .map(x -> {
                    if (x.getDataResourceType().compareTo(DataResourceType.TableDataSet) == 0) {
                        return tableDataSetMapper.transferDetail(x);
                    } else if (x.getDataResourceType().compareTo(DataResourceType.ImageDataSet) == 0) {
                        return imageDataSetMapper.transferDetail(x);
                    } else if (x.getDataResourceType().compareTo(DataResourceType.BloomFilter) == 0) {
                        return bloomFilterMapper.transferDetail(x);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
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
