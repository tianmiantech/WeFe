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
package com.welab.wefe.serving.service.api.apirequestrecord;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ApiRequestRecordService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author ivenn.zheng
 * @date 2022/1/12
 */
@Api(path = "apirequestrecord/download", name = "download the api request records")
public class DownloadApi extends AbstractNoneOutputApi<DownloadApi.Input> {


    @Autowired
    private ApiRequestRecordService apiRequestRecordService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        apiRequestRecordService.downloadFile(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "service_id")
        private String serviceId;

        @Check(name = "client_id")
        private String clientId;

        @Check(name = "start_time")
        private long startTime;

        @Check(name = "end_time")
        private long endTime;

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
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
    }
}
