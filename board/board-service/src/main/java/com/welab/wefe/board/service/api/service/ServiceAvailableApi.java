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

package com.welab.wefe.board.service.api.service;

import com.welab.wefe.board.service.service.ServiceCheckService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.checkpoint.dto.ServiceAvailableCheckOutput;
import com.welab.wefe.common.wefe.enums.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author zane
 */
@Api(path = "service/available", name = "list all checkpoint in board service to show its availability.")
public class ServiceAvailableApi extends AbstractApi<ServiceAvailableApi.Input, ServiceAvailableCheckOutput> {

    @Autowired
    private ServiceCheckService serviceCheckService;

    @Override
    protected ApiResult<ServiceAvailableCheckOutput> handle(Input input) throws StatusCodeWithException, IOException {
        ServiceAvailableCheckOutput output = serviceCheckService.getServiceAvailableInfo(input.serviceType);
        if (input.fromGateway()) {
            output.cleanValues();
        }
        return success(output);
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "服务类型", require = true)
        public ServiceType serviceType;
    }
}
