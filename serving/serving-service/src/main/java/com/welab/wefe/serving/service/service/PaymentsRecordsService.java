/*
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
package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.serving.service.api.paymentsrecords.QueryListApi;
import com.welab.wefe.serving.service.database.serving.entity.PaymentsRecordsMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.PaymentsRecordsRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * @author ivenn.zheng
 * @date 2022/1/14
 */
@Service
public class PaymentsRecordsService {



    @Autowired
    private PaymentsRecordsRepository paymentsRecordsRepository;


    public PagingOutput<PaymentsRecordsMysqlModel> queryList(QueryListApi.Input input) {

        Specification<PaymentsRecordsMysqlModel> where = Where.create()
                .betweenAndDate("createdTime", input.getStartTime(), input.getEndTime())
                .contains("clientName", input.getClientName())
                .contains("serviceName", input.getServiceName())
                .equal("payType", input.getPayType())
                .equal("serviceType", input.getServiceType())
                .build(PaymentsRecordsMysqlModel.class);

        return paymentsRecordsRepository.paging(where, input);
    }

}
