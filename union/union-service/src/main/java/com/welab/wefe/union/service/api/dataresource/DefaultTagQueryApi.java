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

package com.welab.wefe.union.service.api.dataresource;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceDefaultTag;
import com.welab.wefe.common.data.mongodb.repo.DataResourceDefaultTagMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataresource.dataset.table.ApiDataSetDefaultTagOutput;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 */
@Api(path = "data_resource/default_tag/query", name = "data_resource_default_tag_query", rsaVerify = true, login = false)
public class DefaultTagQueryApi extends AbstractApi<DefaultTagQueryApi.Input, JObject> {
    @Autowired
    protected DataResourceDefaultTagMongoRepo dataResourceDefaultTagMongoRepo;

    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException, IOException {
        List<DataResourceDefaultTag> dataResourceDefaultTagList = dataResourceDefaultTagMongoRepo.findByDataResourceType(convertDataResourceType(input.dataResourceType));
        List<ApiDataSetDefaultTagOutput> list = dataResourceDefaultTagList
                .stream().map(x -> {
                    ApiDataSetDefaultTagOutput apiDataSetDefaultTagOutput = new ApiDataSetDefaultTagOutput();
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
                throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER,"dataResourceType");
        }
    }

    public static class Input extends BaseInput {
        @Check(require = true)
        private DataResourceType dataResourceType;

        public DataResourceType getDataResourceType() {
            return dataResourceType;
        }

        public void setDataResourceType(DataResourceType dataResourceType) {
            this.dataResourceType = dataResourceType;
        }
    }
}
