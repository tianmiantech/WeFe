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

package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.common.OperationLog;
import com.welab.wefe.common.data.mongodb.repo.ManagerOperationLogMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.manager.service.dto.operation.OperationLogQueryInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(transactionManager = "transactionManagerManager", rollbackFor = Exception.class)
public class OperationLogService {
    @Autowired
    private ManagerOperationLogMongoRepo operationLogMongoRepo;

    public PageOutput<OperationLog> findList(OperationLogQueryInput input) throws StatusCodeWithException {

        if (!CurrentAccount.isAdmin()) {
            throw new StatusCodeWithException("非管理员无法查看。", StatusCode.PERMISSION_DENIED);
        }

        return operationLogMongoRepo.findList(
                input.getApiName(),
                input.getCallerName(),
                input.getStartTime() == null ? null : new Date(input.getStartTime()),
                input.getEndTime() == null ? null : new Date(input.getEndTime()),
                input.getPageIndex(),
                input.getPageSize()
        );
    }


}
