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
package com.welab.wefe.serving.service.api.apirequestrecord;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.ApiRequestRecordService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author ivenn.zheng
 * @date 2022/1/6
 * 废弃
 */
@Api(path = "apirequestrecord/query-list", name = "query api request records")
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<QueryListApi.Output>> {


    @Autowired
    private ApiRequestRecordService apiRequestRecordService;

    @Override
    protected ApiResult<PagingOutput<Output>> handle(Input input) throws Exception {
        return success(apiRequestRecordService.getListById(input));
    }


    public static class Input extends PagingInput {

        @Check(name = "service_id")
        private String serviceId;

        @Check(name = "client_id")
        private String clientId;

        @Check(name = "start_time")
        private Long startTime;

        @Check(name = "end time")
        private Long endTime;

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

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    }

    public static class Output extends PagingOutput {

        private String serviceId;

        private String clientId;

        /**
         * 客户名称
         */
        private String clientName;

        /**
         * 服务名称
         */
        private String serviceName;

        /**
         * 服务类型
         */
        private String serviceType;

        /**
         * ip地址
         */
        private String ipAdd;

        /**
         * 耗时
         */
        private Long spend;

        /**
         * 请求结果
         */
        private String requestResult;

        private Date createdTime;

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        public String getIpAdd() {
            return ipAdd;
        }

        public void setIpAdd(String ipAdd) {
            this.ipAdd = ipAdd;
        }

        public Long getSpend() {
            return spend;
        }

        public void setSpend(Long spend) {
            this.spend = spend;
        }

        public String getRequestResult() {
            return requestResult;
        }

        public void setRequestResult(String requestResult) {
            this.requestResult = requestResult;
        }

        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
        }
    }
}
