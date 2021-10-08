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

package com.welab.wefe.board.service.api.server;

import com.welab.wefe.board.service.dto.vo.ServerCheckPointOutput;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;

import java.util.List;

/**
 * @author zane
 */
@Api(path = "server/available", name = "list all checkpoint in board service to show its availability.")
public class AvailableApi extends AbstractNoneInputApi<AvailableApi.Output> {
    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        return null;
    }

    public static class Output {
        public boolean success;
        public List<ServerCheckPointOutput> list;
    }
}
