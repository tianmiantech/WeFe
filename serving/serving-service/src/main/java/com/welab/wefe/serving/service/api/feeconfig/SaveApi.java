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

package com.welab.wefe.serving.service.api.feeconfig;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.FeeConfigService;
import org.springframework.beans.factory.annotation.Autowired;


@Api(path = "feeconfig/save", name = "save fee config")
public class SaveApi extends AbstractNoneOutputApi<SaveApi.Input> {

    @Autowired
    private FeeConfigService feeConfigService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        feeConfigService.save(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "计费配置 id")
        private String id;

        @Check(name = "服务 id", require = true)
        private String serviceId;

        @Check(name = "客户 id", require = true)
        private String clientId;

        @Check(name = "计费单价", require = true)
        private Double unitPrice;

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

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }
    }

}
