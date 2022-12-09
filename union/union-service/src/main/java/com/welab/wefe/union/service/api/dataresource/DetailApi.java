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
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceDetailInput;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryOutput;
import com.welab.wefe.union.service.service.DataResourceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "data_resource/detail", name = "data_resource_detail", allowAccessWithSign = true)
public class DetailApi extends AbstractApi<ApiDataResourceDetailInput, ApiDataResourceQueryOutput> {
    @Autowired
    private DataResourceService dataResourceService;

    @Override
    protected ApiResult<ApiDataResourceQueryOutput> handle(ApiDataResourceDetailInput input) throws StatusCodeWithException {
        return success(dataResourceService.detail(input));
    }

}
