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
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ClientServiceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ivenn.zheng
 * @date 2022/1/19
 */
@Api(path = "clientservice/update_status", name = "update client service status")
public class UpdateStatusApi extends AbstractNoneOutputApi<UpdateStatusApi.Input> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<?> handler(UpdateStatusApi.Input input) throws StatusCodeWithException {
        clientServiceService.updateStatus(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "服务 id", require = true, messageOnEmpty = "请选择服务")
        private String serviceId;

        @Check(name = "客户 id", require = true, messageOnEmpty = "请选择客户")
        private String clientId;

        @Check(name = "use status")
        private int status;

        @Check(name = "updated by")
        private String updatedBy;

        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
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

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
