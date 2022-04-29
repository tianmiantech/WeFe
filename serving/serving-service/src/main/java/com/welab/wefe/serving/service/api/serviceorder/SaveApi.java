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
package com.welab.wefe.serving.service.api.serviceorder;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import com.welab.wefe.serving.service.service.ServiceOrderService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author ivenn.zheng
 * @date 2022/4/27
 */
@Api(path = "serviceorder/save", name = "save service order")
public class SaveApi extends AbstractNoneOutputApi<SaveApi.Input> {

    @Autowired
    ServiceOrderService serviceOrderService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        serviceOrderService.save(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "订单 id")
        private String id;

        @Check(name = "服务 id", require = true)
        private String serviceId;

        @Check(name = "服务名称")
        private String serviceName;

        @Check(name = "服务类型")
        private String serviceType;

        @Check(name = "订单类型", require = true)
        private Integer orderType;

        @Check(name = "订单状态")
        private String status = ServiceOrderEnum.ORDERING.getValue();

        @Check(name = "请求方 id", require = true)
        private String requestPartnerId;

        @Check(name = "请求方名称")
        private String requestPartnerName;

        @Check(name = "响应方 id", require = true)
        private String responsePartnerId;

        @Check(name = "响应方名称")
        private String responsePartnerName;

        @Check(name = "更新人")
        private String updatedBy;

        @Check(name = "创建人")
        private String createdBy;

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

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

        public Integer getOrderType() {
            return orderType;
        }

        public void setOrderType(Integer orderType) {
            this.orderType = orderType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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
    }
}
