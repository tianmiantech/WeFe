/*
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

package com.welab.wefe.data.fusion.service.api.system;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.enums.Options;

import java.util.EnumSet;

/**
 * @author hunter.zhao
 */
@Api(path = "system/options", name = "操作类型", desc = "操作类型")
public class OptionsApi extends AbstractApi<OptionsApi.Input, EnumSet<Options>> {

    @Override
    protected ApiResult<EnumSet<Options>> handle(Input input) throws StatusCodeWithException {
        EnumSet<Options> options = EnumSet.allOf(Options.class);
        return success(options);
    }

    public static class Input extends AbstractApiInput {

    }
}
