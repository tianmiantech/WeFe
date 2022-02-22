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

package com.welab.wefe.manager.service.api.defaulttag;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceDefaultTag;
import com.welab.wefe.common.data.mongodb.repo.DataResourceDefaultTagMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import com.welab.wefe.manager.service.dto.tag.ApiDataResourceDefaultTagQueryOutput;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Query the default tag of the dataset
 *
 * @author yuxin.zhang
 */
@Api(path = "data_resource/default_tag/query", name = "default_tag_query",login = false)
public class QueryApi extends AbstractApi<QueryApi.Input, JObject> {

    @Autowired
    protected DataResourceDefaultTagMongoRepo dataResourceDefaultTagMongoRepo;

    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException, IOException {
        List<DataResourceDefaultTag> dataResourceDefaultTagList = dataResourceDefaultTagMongoRepo.findByDataResourceType(
                null == input.dataResourceType ? null : convertDataResourceType(input.dataResourceType)
        );
        List<ApiDataResourceDefaultTagQueryOutput> list = dataResourceDefaultTagList
                .stream().map(x -> {
                    ApiDataResourceDefaultTagQueryOutput apiDataSetDefaultTagOutput = new ApiDataResourceDefaultTagQueryOutput();
                    apiDataSetDefaultTagOutput.setId(x.getTagId());
                    apiDataSetDefaultTagOutput.setTagName(x.getTagName());
                    return apiDataSetDefaultTagOutput;
                }).collect(Collectors.toList());

        return success(JObject.create("list", JObject.toJSON(list)));
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


    public static class Input extends BaseInput {
        private DataResourceType dataResourceType;

        public DataResourceType getDataResourceType() {
            return dataResourceType;
        }

        public void setDataResourceType(DataResourceType dataResourceType) {
            this.dataResourceType = dataResourceType;
        }
    }

}
