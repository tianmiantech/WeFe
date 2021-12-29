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
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceOutputModel;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.ClientServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;

/**
 * @author ivenn.zheng
 */
@Api(path = "clientservice/query-list", name = "query list")
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<ClientServiceOutputModel>> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<PagingOutput<ClientServiceOutputModel>> handle(Input input) throws StatusCodeWithException, IOException {
        return success(clientServiceService.queryList(input));
    }

    public static class Input extends PagingInput {
        @Check(name = "服务名称")
        private String serviceName;

        @Check(name = "客户名称")
        private String clientName;

        @Check(name = "启用状态")
        private Integer status;

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

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
