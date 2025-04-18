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

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.service.DefaultTagService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author yuxin.zhang
 */
@Api(path = "data_resource/default_tag/query", name = "data_resource_default_tag_query", allowAccessWithSign = true)
public class DefaultTagQueryApi extends AbstractApi<DefaultTagQueryApi.Input, JObject> {
    @Autowired
    private DefaultTagService defaultTagService;

    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException, IOException {
        return success(JObject.create("list", JObject.toJSON(defaultTagService.query(input))));
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
