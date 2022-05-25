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

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.ServiceClientTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceStatusEnum;
import com.welab.wefe.serving.service.service.ClientServiceService;


@Api(path = "clientservice/save", name = "save client service model")
public class SaveApi extends AbstractNoneOutputApi<SaveApi.Input> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        clientServiceService.add(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "id")
        private String id;

        @Check(name = "服务 id", require = true, messageOnEmpty = "请选择服务")
        private String serviceId;

        @Check(name = "服务名称")
        private String serviceName;
        
        @Check(name = "客户 id", require = true, messageOnEmpty = "请选择客户")
        private String clientId;

        @Check(name = "合作者名称")
        private String clientName;
        
        @Check(name = "use status")
        private Integer status = ServiceStatusEnum.USED.getCode();

        @Check(name = "pay type")
        private int payType;

        @Check(name = "unit price")
        private Double unitPrice;

        @Check(name = "公钥")
        private String publicKey;
        
        @Check(name = "私钥")
        private String privateKey;
        
        @Check(name = "调用者code")
        private String code;
        
        @Check(name = "IP白名单")
        private String ipAdd;

        @Check(name = "类型")
        private int type = ServiceClientTypeEnum.OPEN.getValue();
        
        @Check(name = "服务地址") // 激活服务使用
        private String url;
        
        @Check(name = "服务类型")
        private int serviceType;
        
        @Check(name = "created by")
        private String createdBy;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (unitPrice < 0) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("单价不能为负数：" + unitPrice);
            }
        }
        
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

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIpAdd() {
            return ipAdd;
        }

        public void setIpAdd(String ipAdd) {
            this.ipAdd = ipAdd;
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

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public int getServiceType() {
            return serviceType;
        }

        public void setServiceType(int serviceType) {
            this.serviceType = serviceType;
        }
    }


}
