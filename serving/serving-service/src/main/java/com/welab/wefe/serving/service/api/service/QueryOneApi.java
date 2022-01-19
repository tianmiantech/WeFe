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
package com.welab.wefe.serving.service.api.service;

import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.ServiceMySqlModel;
import com.welab.wefe.serving.service.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ivenn.zheng
 * @date 2022/1/19
 */
@Api(path = "service/query-one", name = "query service by id")
public class QueryOneApi extends AbstractApi<QueryOneApi.Input, ServiceMySqlModel> {


    @Autowired
    private ServiceService serviceService;


    @Override
    protected ApiResult<ServiceMySqlModel> handle(Input input) throws Exception {
        return success(serviceService.queryById(input));
    }

    public static class Input extends AbstractApiInput {

        private String id;

        private Integer status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}



