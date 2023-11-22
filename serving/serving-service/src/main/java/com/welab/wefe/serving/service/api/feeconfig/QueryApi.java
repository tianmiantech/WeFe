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

import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.service.FeeConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ivenn.zheng
 * @date 2022/1/22
 */
@Api(path = "feeconfig/query-one", name = "query fee config")
public class QueryApi extends AbstractApi<QueryApi.Input, FeeConfigMysqlModel> {


    @Autowired
    private FeeConfigService feeConfigService;

    @Override
    protected ApiResult<FeeConfigMysqlModel> handle(Input input) throws Exception {
        return success(feeConfigService.queryOne(input.getServiceId(), input.clientId));
    }

    public static class Input extends AbstractApiInput {
        private String serviceId;

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
