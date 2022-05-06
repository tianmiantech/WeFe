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

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.DateTypeEnum;
import com.welab.wefe.serving.service.service.OrderStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Column;
import java.util.Date;

/**
 * @author ivenn.zheng
 * @date 2022/4/27
 */
@Api(path = "orderstatistics/query-list", name = "search order statistics list")
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<QueryListApi.Output>> {


    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Override
    protected ApiResult<PagingOutput<Output>> handle(Input input) throws Exception {
        return success(orderStatisticsService.queryList(input));
    }

    public static class Input extends PagingInput {

        @Check(name = "开始时间")
        private Date startTime;

        @Check(name = "结束时间")
        private Date endTime;

        @Check(name = "请求方 id")
        private String requestPartnerId;

        @Check(name = "请求方名称")
        private String requestPartnerName;

        @Check(name = "响应方 id")
        private String responsePartnerId;

        @Check(name = "响应方名称")
        private String responsePartnerName;

        @Check(name = "服务 id")
        private String serviceId;

        @Check(name = "服务名称")
        private String serviceName;

        @Check(name = "统计粒度", require = true)
        private String statisticalGranularity = DateTypeEnum.minute.name();

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
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

        public String getStatisticalGranularity() {
            return statisticalGranularity;
        }

        public void setStatisticalGranularity(String statisticalGranularity) {
            this.statisticalGranularity = statisticalGranularity;
        }
    }


    public static class Output extends PagingOutput {

        @Column(name = "总请求次数")
        private Integer callTimes;

        @Column(name = "总成功次数")
        private Integer successTimes;

        @Column(name = "总失败次数")
        private Integer failedTimes;

        @Check(name = "统计时间")
        private String dateTime;

        @Column(name = "request_partner_id")
        private String requestPartnerId;

        @Column(name = "request_partner_name")
        private String requestPartnerName;

        @Column(name = "response_partner_id")
        private String responsePartnerId;

        @Column(name = "response_partner_name")
        private String responsePartnerName;

        @Column(name = "service_id")
        private String serviceId;

        @Column(name = "service_name")
        private String serviceName;

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

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
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
