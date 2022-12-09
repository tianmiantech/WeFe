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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceDefaultTag;
import com.welab.wefe.common.data.mongodb.entity.union.DataSetDefaultTag;
import com.welab.wefe.common.data.mongodb.repo.DataResourceDefaultTagMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.DataSetDefaultTagMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.union.service.api.dataresource.DefaultTagQueryApi;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataresource.dataset.table.ApiDataSetDefaultTagOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DefaultTagService {
    @Autowired
    protected DataResourceDefaultTagMongoRepo dataResourceDefaultTagMongoRepo;
    @Autowired
    protected DataSetDefaultTagMongoRepo dataSetDefaultTagMongoRepo;

    public List<ApiDataSetDefaultTagOutput> query(DefaultTagQueryApi.Input input) throws StatusCodeWithException {
        List<DataResourceDefaultTag> dataResourceDefaultTagList = dataResourceDefaultTagMongoRepo.findByDataResourceType(convertDataResourceType(input.getDataResourceType()));
        return dataResourceDefaultTagList
                .stream().map(x -> {
                    ApiDataSetDefaultTagOutput apiDataSetDefaultTagOutput = new ApiDataSetDefaultTagOutput();
                    apiDataSetDefaultTagOutput.setId(x.getTagId());
                    apiDataSetDefaultTagOutput.setTagName(x.getTagName());
                    return apiDataSetDefaultTagOutput;
                }).collect(Collectors.toList());
    }

    public List<ApiDataSetDefaultTagOutput> queryAll(BaseInput input) {
        List<DataSetDefaultTag> dataSetDefaultTagList = dataSetDefaultTagMongoRepo.findAll();
        return dataSetDefaultTagList
                .stream().map(x -> {
                    ApiDataSetDefaultTagOutput apiDataSetDefaultTagOutput = new ApiDataSetDefaultTagOutput();
                    apiDataSetDefaultTagOutput.setId(x.getTagId());
                    apiDataSetDefaultTagOutput.setTagName(x.getTagName());
                    return apiDataSetDefaultTagOutput;
                }).collect(Collectors.toList());
    }


    private String convertDataResourceType(DataResourceType dataResourceType) throws StatusCodeWithException {
        switch (dataResourceType) {
            case BloomFilter:
            case TableDataSet:
                return DataResourceType.TableDataSet.name();
            case ImageDataSet:
                return DataResourceType.ImageDataSet.name();
            default:
                throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER, "dataResourceType");
        }
    }
}
