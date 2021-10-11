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

package com.welab.wefe.union.service.api.defaulttag;

import com.welab.wefe.common.data.mongodb.repo.DataSetDefaultTagMongoRepo;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataset.ApiDataSetDefaultTagOutput;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query the default tag of the dataset
 *
 * @author yuxin.zhang
 */
@Api(path = "default_tag/query", name = "default_tag_query", rsaVerify = true, login = false)
public class QueryAllApi extends AbstractApi<QueryAllApi.Input, JObject> {
    @Autowired
    protected DataSetDefaultTagMongoRepo dataSetDefaultTagMongoRepo;

    @Override
    protected ApiResult<JObject> handle(QueryAllApi.Input input) {
        List<ApiDataSetDefaultTagOutput> list = dataSetDefaultTagMongoRepo.findAll()
                .stream().map(x -> {
                    ApiDataSetDefaultTagOutput apiDataSetDefaultTagOutput = new ApiDataSetDefaultTagOutput();
                    apiDataSetDefaultTagOutput.setId(x.getTagId());
                    apiDataSetDefaultTagOutput.setTagName(x.getTagName());
                    return apiDataSetDefaultTagOutput;
                }).collect(Collectors.toList());

        return success(JObject.create("list", JObject.toJSON(list)));
    }


    public static class Input extends BaseInput {
        private String id;
        private String tagName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }
    }

}
