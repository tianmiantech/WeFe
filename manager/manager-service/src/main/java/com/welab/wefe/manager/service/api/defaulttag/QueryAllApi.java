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

package com.welab.wefe.manager.service.api.defaulttag;

import com.welab.wefe.common.data.mongodb.entity.union.DataSetDefaultTag;
import com.welab.wefe.common.data.mongodb.repo.AbstractDataSetDefaultTagMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetDefaultTagMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.TableDataSetDefaultTagMongoRepo;
import com.welab.wefe.common.enums.DataSetType;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import com.welab.wefe.manager.service.dto.tag.ApiDataSetDefaultTagQueryOutput;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query the default tag of the dataset
 *
 * @author yuxin.zhang
 */
@Api(path = "default_tag/query", name = "default_tag_query")
public class QueryAllApi extends AbstractApi<QueryAllApi.Input, JObject> {

    @Autowired
    protected TableDataSetDefaultTagMongoRepo tableDataSetDefaultTagMongoRepo;

    @Autowired
    protected ImageDataSetDefaultTagMongoRepo imageDataSetDefaultTagMongoRepo;

    @Override
    protected ApiResult<JObject> handle(QueryAllApi.Input input) {
        List<DataSetDefaultTag> dataSetDefaultTagList = getMongoRepo(input).findAll(DataSetDefaultTag.class);
        List<ApiDataSetDefaultTagQueryOutput> list = dataSetDefaultTagList
                .stream().map(x -> {
                    ApiDataSetDefaultTagQueryOutput apiDataSetDefaultTagQueryOutput = new ApiDataSetDefaultTagQueryOutput();
                    apiDataSetDefaultTagQueryOutput.setId(x.getTagId());
                    apiDataSetDefaultTagQueryOutput.setTagName(x.getTagName());
                    apiDataSetDefaultTagQueryOutput.setStatus(x.getStatus());
                    apiDataSetDefaultTagQueryOutput.setExtJson(x.getExtJson());
                    return apiDataSetDefaultTagQueryOutput;
                }).collect(Collectors.toList());

        return success(JObject.create("list", JObject.toJSON(list)));
    }

    public AbstractDataSetDefaultTagMongoRepo getMongoRepo(QueryAllApi.Input input) {
        if (DataSetType.ImageDataSet.name().equals(input.getDataSetType())) {
            return imageDataSetDefaultTagMongoRepo;
        }
        return tableDataSetDefaultTagMongoRepo;
    }

    public static class Input extends BaseInput {
        private String dataSetType;


        public String getDataSetType() {
            return dataSetType;
        }

        public void setDataSetType(String dataSetType) {
            this.dataSetType = dataSetType;
        }
    }


}
