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

package com.welab.wefe.serving.service.api.apirequestrecord;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ApiRequestRecordService;
import org.springframework.beans.factory.annotation.Autowired;


@Api(path = "apirequestrecord/save", name = "save api request record", login = false)
public class SaveApi extends AbstractNoneOutputApi<SaveApi.Input> {

    @Autowired
    private ApiRequestRecordService apiRequestRecordService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {

        apiRequestRecordService.save(input.getServiceId(), input.getClientId(), input.getSpend(), input.getIpAdd(), input.getRequestResult());
        return success();
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "service_id")
        private String serviceId;

        @Check(name = "client_id")
        private String clientId;

        /**
         * 请求地址
         */
        @Check(name = "ip_add")
        private String ipAdd;

        /**
         * 耗时
         */
        private Long spend;

        /**
         * 请求结果：1 成功、0 失败
         */
        @Check(name = "request_result")
        private Integer requestResult;

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

        public String getIpAdd() {
            return ipAdd;
        }

        public void setIpAdd(String ipAdd) {
            this.ipAdd = ipAdd;
        }

        public Long getSpend() {
            return spend;
        }

        public void setSpend(Long spend) {
            this.spend = spend;
        }

        public Integer getRequestResult() {
            return requestResult;
        }

        public void setRequestResult(Integer requestResult) {
            this.requestResult = requestResult;
        }
    }
}
