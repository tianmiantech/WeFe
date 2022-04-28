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

package com.welab.wefe.serving.service.api.clientservice;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.ServiceClientTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceStatusEnum;
import com.welab.wefe.serving.service.service.ClientService;
import com.welab.wefe.serving.service.service.ClientServiceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import javax.persistence.Column;


@Api(path = "clientservice/save", name = "save client service model")
public class SaveApi extends AbstractNoneOutputApi<SaveApi.Input> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        clientServiceService.save(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "id")
        private String id;

        @Check(name = "服务 id", require = true, messageOnEmpty = "请选择服务")
        private String serviceId;

        @Check(name = "客户 id", require = true, messageOnEmpty = "请选择客户")
        private String clientId;

        @Check(name = "use status")
        private Integer status = ServiceStatusEnum.USED.getCode();

        @Check(name = "pay type")
        private int payType;

        @Check(name = "unit price")
        private Double unitPrice;

        @Column(name = "公钥")
        private String publicKey;

        @Column(name = "服务类型")
        private int type = ServiceClientTypeEnum.OPEN.getValue();
        
        @Check(name = "created by")
        private String createdBy;

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public int getPayType() {
            return payType;
        }

        public void setPayType(int payType) {
            this.payType = payType;
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

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }


}
