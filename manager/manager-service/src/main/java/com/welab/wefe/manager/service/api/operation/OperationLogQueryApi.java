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

package com.welab.wefe.manager.service.api.operation;

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.common.OperationLog;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.manager.service.dto.operation.OperationLogQueryInput;
import com.welab.wefe.manager.service.dto.operation.OperationLogQueryOutput;
import com.welab.wefe.manager.service.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/5
 */
@Api(path = "operation_log/query", name = "operation log query")
public class OperationLogQueryApi extends AbstractApi<OperationLogQueryInput, PageOutput<OperationLogQueryOutput>> {
    @Autowired
    private OperationLogService operationLogService;

    @Override
    protected ApiResult<PageOutput<OperationLogQueryOutput>> handle(OperationLogQueryInput input) throws StatusCodeWithException, IOException {
        PageOutput<OperationLog> pageOutput = operationLogService.findList(input);
        List<OperationLogQueryOutput> list = pageOutput.getList().stream()
                .map(operationLog -> ModelMapper.map(operationLog, OperationLogQueryOutput.class))
                .collect(Collectors.toList());

        return success(new PageOutput<>(
                pageOutput.getPageIndex(),
                pageOutput.getTotal(),
                pageOutput.getPageSize(),
                pageOutput.getTotalPage(),
                list
        ));
    }
}
