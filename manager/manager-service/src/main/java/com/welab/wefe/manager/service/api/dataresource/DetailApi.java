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

package com.welab.wefe.manager.service.api.dataresource;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.repo.BloomFilterMongoReop;
import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.data.mongodb.repo.TableDataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.manager.service.dto.dataresource.ApiDataResourceDetailInput;
import com.welab.wefe.manager.service.dto.dataresource.ApiDataResourceQueryOutput;
import com.welab.wefe.manager.service.mapper.BloomFilterMapper;
import com.welab.wefe.manager.service.mapper.ImageDataSetMapper;
import com.welab.wefe.manager.service.mapper.TableDataSetMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "data_resource/detail", name = "data_resource_detail")
public class DetailApi extends AbstractApi<ApiDataResourceDetailInput, ApiDataResourceQueryOutput> {

    @Autowired
    protected DataResourceMongoReop dataResourceMongoReop;
    @Autowired
    protected BloomFilterMongoReop bloomFilterMongoReop;
    @Autowired
    protected ImageDataSetMongoReop imageDataSetMongoReop;
    @Autowired
    protected TableDataSetMongoReop tableDataSetMongoReop;

    protected TableDataSetMapper tableDataSetMapper = Mappers.getMapper(TableDataSetMapper.class);
    protected ImageDataSetMapper imageDataSetMapper = Mappers.getMapper(ImageDataSetMapper.class);
    protected BloomFilterMapper bloomFilterMapper = Mappers.getMapper(BloomFilterMapper.class);


    @Override
    protected ApiResult<ApiDataResourceQueryOutput> handle(ApiDataResourceDetailInput input) throws StatusCodeWithException {
        DataResourceQueryOutput dataResourceQueryOutput;
        switch (input.getDataResourceType()) {
            case BloomFilter:
                dataResourceQueryOutput = dataResourceMongoReop.findOneByDataResourceId(input.getDataResourceId(), MongodbTable.Union.BLOOM_FILTER);
                break;
            case TableDataSet:
                dataResourceQueryOutput = dataResourceMongoReop.findOneByDataResourceId(input.getDataResourceId(), MongodbTable.Union.TABLE_DATASET);
                break;
            case ImageDataSet:
                dataResourceQueryOutput = dataResourceMongoReop.findOneByDataResourceId(input.getDataResourceId(), MongodbTable.Union.IMAGE_DATASET);
                break;
            default:
                throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER, "dataResourceType");
        }

        return success(getOutput(dataResourceQueryOutput));
    }

    protected ApiDataResourceQueryOutput getOutput(DataResourceQueryOutput dataResourceQueryOutput) {
        if (dataResourceQueryOutput == null) {
            return null;
        }
        if (dataResourceQueryOutput.getDataResourceType().compareTo(DataResourceType.TableDataSet) == 0) {
            return tableDataSetMapper.transferDetail(dataResourceQueryOutput);
        } else if (dataResourceQueryOutput.getDataResourceType().compareTo(DataResourceType.ImageDataSet) == 0) {
            return imageDataSetMapper.transferDetail(dataResourceQueryOutput);
        } else {
            return bloomFilterMapper.transferDetail(dataResourceQueryOutput);
        }
    }

}
