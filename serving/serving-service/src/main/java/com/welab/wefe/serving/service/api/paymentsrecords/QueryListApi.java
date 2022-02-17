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
package com.welab.wefe.serving.service.api.paymentsrecords;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.PaymentsRecordsMysqlModel;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.PaymentsRecordsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author ivenn.zheng
 * @date 2022/1/14
 */
@Api(path = "paymentsrecords/query-list", name = "query list")
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<PaymentsRecordsMysqlModel>> {

    @Autowired
    private PaymentsRecordsService paymentsRecordsService;

    @Override
    protected ApiResult<PagingOutput<PaymentsRecordsMysqlModel>> handle(Input input) throws StatusCodeWithException, IOException {
        return success(paymentsRecordsService.queryList(input));
    }

    public static class Input extends PagingInput {

        private long startTime;

        private long endTime;

        private Integer payType;

        private String serviceName;

        private String clientName;

        private Integer serviceType;

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public Integer getPayType() {
            return payType;
        }

        public void setPayType(Integer payType) {
            this.payType = payType;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public Integer getServiceType() {
            return serviceType;
        }

        public void setServiceType(Integer serviceType) {
            this.serviceType = serviceType;
        }
    }
}