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

package com.welab.wefe.board.service.api.project.fusion;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.HashOptions;

import java.util.EnumSet;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/hash_options_enum", name = "任务状态", desc = "任务状态")
public class HashOptionsEnumApi extends AbstractApi<HashOptionsEnumApi.Input, EnumSet<HashOptions>> {

    @Override
    protected ApiResult<EnumSet<HashOptions>> handle(Input input) throws StatusCodeWithException {
        EnumSet<HashOptions> hashOptions = EnumSet.allOf(HashOptions.class);
        return success(hashOptions);
    }

    public static class Input extends AbstractApiInput {

    }
}
