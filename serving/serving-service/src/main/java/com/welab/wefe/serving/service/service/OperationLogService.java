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

package com.welab.wefe.serving.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.serving.service.api.operation.LogQueryApi;
import com.welab.wefe.serving.service.database.serving.entity.OperationLogMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.OperationLogRepository;
import com.welab.wefe.serving.service.dto.OperationLogOutputModel;
import com.welab.wefe.serving.service.dto.PagingOutput;

/**
 * @author eval
 **/
@Service
public class OperationLogService {

    @Autowired
    OperationLogRepository mOperationLogRepository;

    public PagingOutput<OperationLogOutputModel> query(LogQueryApi.Input input) throws StatusCodeWithException {
        if (!CurrentAccount.isAdmin()) {
            StatusCode.PERMISSION_DENIED.throwException("普通用户无法进行此操作。");
        }

        Specification<OperationLogMysqlModel> where = Where.create().equal("logInterface", input.logInterface)
                .equal("operatorId", input.operatorId)
                .betweenAndDate("createdTime", input.getStartTime(), input.getEndTime())
                .orderBy("createdTime", OrderBy.desc).build(OperationLogMysqlModel.class);

        return mOperationLogRepository.paging(where, input, OperationLogOutputModel.class);
    }
}
