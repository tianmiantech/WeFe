/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.api.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.ServiceResultEnum;
import com.welab.wefe.serving.service.service.ServiceService;

@Api(path = "api", name = "api service", forward = true, allowAccessWithSign = true, domain = Caller.Customer)
public class RouteApi extends AbstractApi<RouteApi.Input, JObject> {

    @Autowired
    private ServiceService serviceService;

    @Override
    protected ApiResult<JObject> handle(Input input) {
        try {
            JObject result = serviceService.executeService(input);
            return success(result);
        } catch (Exception e) {
            LOG.error("executeService error, ", e);
            JObject res = new JObject();
            res.put("code", ServiceResultEnum.SERVICE_FAIL.getCode());
            res.put("message", e.getMessage());
            return success(res);
        }
    }

    /**
     * Maximum parallelism allowed by an interface
     */
    @Override
    protected int parallelism() {
        return 20;
    }
    
    /**
     * Specifies whether the interface allows concurrency. The default value is yes.
     * <p>
     * Override this method in a subclass if changes are needed.
     */
    @Override
    public boolean canParallel() {
        return true;
    }
    
    public static class Input extends AbstractApiInput {

        @Check(name = "合作者ID")
        private String partnerCode;

        @Check(name = "请求数据")
        private String data;

        @Check(name = "服务ID")
        private String serviceId;

        @Check(name = "请求ID", require = true)
        private String requestId;

        @Check(name = "请求ID", require = true)
        private boolean isModelService;

        public String getPartnerCode() {
            return partnerCode;
        }

        public void setPartnerCode(String partnerCode) {
            this.partnerCode = partnerCode;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public boolean isModelService() {
            return isModelService;
        }

        public void setModelService(boolean modelService) {
            isModelService = modelService;
        }
    }

}
