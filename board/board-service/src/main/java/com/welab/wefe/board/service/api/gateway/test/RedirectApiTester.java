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

package com.welab.wefe.board.service.api.gateway.test;

import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "gateway/test/redirect", name = "自己给自己发消息，用来测试", login = false)
public class RedirectApiTester extends AbstractApi<RedirectApiTester.Input, RedirectApiTester.Output> {

    @Autowired
    private GatewayService gatewayService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        Class<?> api = null;
        try {
            api = Class.forName(input.api);
        } catch (ClassNotFoundException e) {
            throw new StatusCodeWithException("api class error:" + input.api, StatusCode.PARAMETER_VALUE_INVALID);
        }

        ApiResult<?> result = gatewayService.sendToBoardRedirectApi(
                CacheObjects.getMemberId(),
                input.memberRole,
                input.data,
                api
        );
        return success(new Output(result.success()));
    }

    public static class Output {
        public boolean success;

        public Output(boolean success) {
            this.success = success;
        }
    }

    public static class Input extends AbstractApiInput {
        public Object data;
        public JobMemberRole memberRole;
        public String api;
    }
}
