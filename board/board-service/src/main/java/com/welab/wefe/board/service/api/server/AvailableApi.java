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

package com.welab.wefe.board.service.api.server;

import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.checkpoint.CheckpointManager;
import com.welab.wefe.common.wefe.checkpoint.dto.ServerAvailableCheckOutput;
import com.welab.wefe.common.wefe.enums.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author zane
 */
@Api(path = "server/available", name = "list all checkpoint in board service to show its availability.")
public class AvailableApi extends AbstractApi<AvailableApi.Input, ServerAvailableCheckOutput> {

    @Autowired
    private CheckpointManager checkpointManager;
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<ServerAvailableCheckOutput> handle(Input input) throws StatusCodeWithException, IOException {
        ServerAvailableCheckOutput output = null;
        try {
            switch (input.serviceType) {
                case BoardService:
                    output = checkpointManager.checkAll();
                    break;
                case GatewayService:
                    output = gatewayService.getLocalGatewayAvailable();
                    break;
                case UnionService:
                    output = unionService.getAvailable();
                    break;
                default:
                    StatusCode.UNEXPECTED_ENUM_CASE.throwException();
            }
        } catch (Exception e) {
            output = new ServerAvailableCheckOutput(e.getMessage());
        }

        return success(output);
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "服务类型", require = true)
        public ServiceType serviceType;
    }
}
