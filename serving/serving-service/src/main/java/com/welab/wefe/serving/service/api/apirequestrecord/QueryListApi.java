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
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.ApiRequestRecordMysqlModel;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.ApiRequestRecordService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author ivenn.zheng
 * @date 2022/1/6
 */
@Api(path = "apirequestrecord/query-list", name = "query api request records")
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<ApiRequestRecordMysqlModel>> {


    @Autowired
    private ApiRequestRecordService apiRequestRecordService;

    @Override
    protected ApiResult<PagingOutput<ApiRequestRecordMysqlModel>> handle(Input input) throws StatusCodeWithException, IOException {
        return success(apiRequestRecordService.getListById(input));
    }

    public static class Input extends PagingInput {

        @Check(name = "service_id")
        private String serviceId;

        @Check(name = "client_id")
        private String clientId;

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
