/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.manager.service.api.defaulttag;

import com.welab.wefe.common.data.mongodb.repo.DataSetDefaultTagMongoRepo;
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
@Api(path = "default_tag/query", name = "default_tag_query",login = false)
public class QueryAllApi extends AbstractApi<BaseInput, JObject> {

    @Autowired
    protected DataSetDefaultTagMongoRepo dataSetDefaultTagMongoRepo;

    @Override
    protected ApiResult<JObject> handle(BaseInput input) {
        List<ApiDataSetDefaultTagQueryOutput> list = dataSetDefaultTagMongoRepo.findAll()
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


}
