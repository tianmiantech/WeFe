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
package com.welab.wefe.serving.service.api.servicecalllog;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.ServiceCallLogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author ivenn.zheng
 * @date 2022/5/6
 */
@Api(path = "servicecalllog/query-list", name = "query service call logs")
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<QueryListApi.Output>> {

    @Autowired
    private ServiceCallLogService serviceCallLogService;

    @Override
    protected ApiResult<PagingOutput<Output>> handle(Input input) throws Exception {
        return success(serviceCallLogService.queryList(input));
    }


    public static class Output extends PagingOutput {

        private Integer callByMe;

        private String requestId;

        private String responseId;

        private String requestData;

        private String responseData;

        private String requestIp;

        private Date createdTime;


        public Integer getCallByMe() {
            return callByMe;
        }

        public void setCallByMe(Integer callByMe) {
            this.callByMe = callByMe;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getResponseId() {
            return responseId;
        }

        public void setResponseId(String responseId) {
            this.responseId = responseId;
        }

        public String getRequestData() {
            return requestData;
        }

        public void setRequestData(String requestData) {
            this.requestData = requestData;
        }

        public String getResponseData() {
            return responseData;
        }

        public void setResponseData(String responseData) {
            this.responseData = responseData;
        }

        public String getRequestIp() {
            return requestIp;
        }

        public void setRequestIp(String requestIp) {
            this.requestIp = requestIp;
        }

        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
        }
    }


    public static class Input extends PagingInput {


        @Check(name = "服务 id")
        private String serviceId;

        @Check(name = "订单 id")
        private String orderId;


        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }

}
