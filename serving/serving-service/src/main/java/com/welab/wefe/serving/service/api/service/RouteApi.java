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

import com.alibaba.fastjson.JSON;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.entity.ServiceMySqlModel;
import com.welab.wefe.serving.service.dto.ServiceResultOutput;
import com.welab.wefe.serving.service.enums.ServiceResultEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.service.ModelService;
import com.welab.wefe.serving.service.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;

@Api(path = "api", name = "api service", forward = true, login = false, rsaVerify = true, domain = Caller.Customer)
public class RouteApi extends AbstractApi<RouteApi.Input, JObject> {

    @Autowired
    private ServiceService service;

    @Autowired
    private ModelService modelService;

    @Override
    protected ApiResult<JObject> handle(Input input) {
        LOG.info("request =" + JObject.toJSONString(input));
        try {
            ServiceMySqlModel serviceModel = service.findById(input.getServiceId());
            if (ServiceTypeEnum.MachineLearning.getCode() == serviceModel.getServiceType()) {
                ServiceResultOutput output = modelService.predict(serviceModel, input);
                return success(JObject.create(JSON.toJSONString(output)));
            }

            JObject result = service.executeService(input);
            LOG.info("response =" + JObject.toJSONString(result));
            return success(result);
        } catch (Exception e) {
            e.printStackTrace();
            JObject res = new JObject();
            res.put("code", ServiceResultEnum.SERVICE_FAIL.getCode());
            res.put("message", e.getMessage());
            return success(res);
        }
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "合作者ID")
        private String customerId;

        @Check(name = "请求数据")
        private String data;

        @Check(name = "服务ID")
        private String serviceId;

        @Check(name = "请求ID")
        private String requestId;

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
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

    }

}
