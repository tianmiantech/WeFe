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
package com.welab.wefe.serving.service.api.feedetail;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.FeeDetailMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.FeeDetailOutputModel;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.FeeDetailService;
import com.welab.wefe.serving.service.service.FeeRecordService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author ivenn.zheng
 * @date 2021/12/23
 */
@Api(path = "feedetail/query-list", name = "fee detail query")
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<FeeDetailOutputModel>> {

    @Autowired
    private FeeDetailService feeDetailService;

    @Override
    protected ApiResult<PagingOutput<FeeDetailOutputModel>> handle(Input input) throws StatusCodeWithException, IOException {
        return success(feeDetailService.queryList(input));
    }

    public static class Input extends PagingInput {

        /**
         * 服务名称
         */
        private String serviceName;

        /**
         * 客户名称
         */
        private String clientName;

        /**
         * 服务类型
         */
        private Integer serviceType;

        /**
         * 统计类型：1 每年、 2 每月、 3 每日, 4 每小时
         */
        private Integer queryDateType;

        /**
         * 统计开始时间
         */
        private Long startTime;

        /**
         * 统计结束时间
         */
        private Long endTime;

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

        public Integer getQueryDateType() {
            return queryDateType;
        }

        public void setQueryDateType(Integer queryDateType) {
            this.queryDateType = queryDateType;
        }

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public Long getEndTime() {
            return endTime;
        }

        public void setEndTime(Long endTime) {
            this.endTime = endTime;
        }
    }

}
