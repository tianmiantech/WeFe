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

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;

import java.io.IOException;

/**
 * @author yuxin.zhang
 */
@Api(path = "default_tag/query", name = "default_tag_query", rsaVerify = true, login = false)
public class DefaultTagQuery extends AbstractApi<DefaultTagQuery.Input, JObject> {


    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException, IOException {
        return null;
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
