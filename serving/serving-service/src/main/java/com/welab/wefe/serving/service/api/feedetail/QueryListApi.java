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
package com.welab.wefe.serving.service.api.feedetail;

import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.FeeDetailService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ivenn.zheng
 * @date 2021/12/23
 */
@Api(path = "feedetail/query-list", name = "fee detail query")
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<QueryListApi.Output>> {


    @Autowired
    private FeeDetailService feeDetailService;

    @Override
    protected ApiResult<PagingOutput<Output>> handle(Input input) throws Exception {
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
        private String serviceType;

        /**
         * 统计类型：1 每年、 2 每月、 3 每日, 4 每小时
         */
        private Integer queryDateType;

        /**
         * 统计开始时间
         */
        private Date startTime;

        /**
         * 统计结束时间
         */
        private Date endTime;

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

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        public Integer getQueryDateType() {
            return queryDateType;
        }

        public void setQueryDateType(Integer queryDateType) {
            this.queryDateType = queryDateType;
        }

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
    }

    public static class Output extends PagingOutput {
        private String id;

        private String serviceId;

        private String clientId;

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
        private String serviceType;

        /**
         * 单价
         */
        private BigDecimal unitPrice;

        /**
         * 付费类型
         */
        private String payType;

        /**
         * 总调用次数
         */
        private Long totalRequestTimes;

        /**
         * 总计
         */
        private BigDecimal totalFee;

        /**
         * 统计日期
         */
        private String queryDate;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getPayType() {
            return payType;
        }

        public void setPayType(String payType) {
            this.payType = payType;
        }

        public Long getTotalRequestTimes() {
            return totalRequestTimes;
        }

        public void setTotalRequestTimes(Long totalRequestTimes) {
            this.totalRequestTimes = totalRequestTimes;
        }

        public BigDecimal getTotalFee() {
            return totalFee;
        }

        public void setTotalFee(BigDecimal totalFee) {
            this.totalFee = totalFee;
        }

        public String getQueryDate() {
            return queryDate;
        }

        public void setQueryDate(String queryDate) {
            this.queryDate = queryDate;
        }
    }
}
