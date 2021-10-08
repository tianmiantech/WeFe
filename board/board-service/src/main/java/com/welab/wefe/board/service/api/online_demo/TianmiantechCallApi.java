/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.api.online_demo;

import com.welab.wefe.board.service.base.OnlineDemoApi;
import com.welab.wefe.board.service.onlinedemo.TianmiantechService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

/**
 * @author zane
 */
@OnlineDemoApi
@Api(path = "tianmiantech/call_api", name = "call tianmiantech service api")
public class TianmiantechCallApi extends AbstractApi<TianmiantechCallApi.Input, JObject> {
    @Autowired
    private TianmiantechService tianmiantechService;

    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException, IOException {
        JObject result = tianmiantechService.through(input.api, input.params);
        return success(result);
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String api;
        public Map<String, Object> params;
    }

}
