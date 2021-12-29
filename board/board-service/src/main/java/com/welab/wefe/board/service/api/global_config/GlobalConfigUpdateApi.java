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

package com.welab.wefe.board.service.api.global_config;

import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author lonnie
 */
@Api(path = "global_config/update", name = "update system global configs")
public class GlobalConfigUpdateApi extends AbstractNoneOutputApi<GlobalConfigUpdateApi.Input> {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        globalConfigService.update(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        public Map<String, Map<String, String>> groups;
    }
}
