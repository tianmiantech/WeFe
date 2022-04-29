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
package com.welab.wefe.serving.service.api.orderstatistics;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.OrderStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author ivenn.zheng
 * @date 2022/4/27
 */
@Api(path = "orderstatistics/save", name = "新增订单统计")
public class SaveApi extends AbstractNoneOutputApi<SaveApi.Input> {


    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        orderStatisticsService.save(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "id")
        private String id;

        @Check(name = "总请求次数")
        private Integer callTimes;

        @Check(name = "总成功次数")
        private Integer successTimes;

        @Check(name = "总失败次数")
        private Integer failedTimes;

        @Check(name = "minute")
        private String minute;

        @Check(name = "hour")
        private String hour;

        @Check(name = "day")
        private String day;

        @Check(name = "month")
        private String month;

        @Check(name = "请求方id")
        private String requestPartnerId;

        @Check(name = "请求方名称")
        private String requestPartnerName;

        @Check(name = "响应方id")
        private String responsePartnerId;

        @Check(name = "响应方名称")
        private String responsePartnerName;

        @Check(name = "服务id")
        private String serviceId;

        @Check(name = "服务名称")
        private String serviceName;

        @Check(name = "更新人")
        private String updatedBy;

        @Check(name = "创建人")
        private String createdBy;

        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getCallTimes() {
            return callTimes;
        }

        public void setCallTimes(Integer callTimes) {
            this.callTimes = callTimes;
        }

        public Integer getSuccessTimes() {
            return successTimes;
        }

        public void setSuccessTimes(Integer successTimes) {
            this.successTimes = successTimes;
        }

        public Integer getFailedTimes() {
            return failedTimes;
        }

        public void setFailedTimes(Integer failedTimes) {
            this.failedTimes = failedTimes;
        }

        public String getMinute() {
            return minute;
        }

        public void setMinute(String minute) {
            this.minute = minute;
        }

        public String getHour() {
            return hour;
        }

        public void setHour(String hour) {
            this.hour = hour;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getRequestPartnerId() {
            return requestPartnerId;
        }

        public void setRequestPartnerId(String requestPartnerId) {
            this.requestPartnerId = requestPartnerId;
        }

        public String getRequestPartnerName() {
            return requestPartnerName;
        }

        public void setRequestPartnerName(String requestPartnerName) {
            this.requestPartnerName = requestPartnerName;
        }

        public String getResponsePartnerId() {
            return responsePartnerId;
        }

        public void setResponsePartnerId(String responsePartnerId) {
            this.responsePartnerId = responsePartnerId;
        }

        public String getResponsePartnerName() {
            return responsePartnerName;
        }

        public void setResponsePartnerName(String responsePartnerName) {
            this.responsePartnerName = responsePartnerName;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
    }
}
