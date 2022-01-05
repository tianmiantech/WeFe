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

package com.welab.wefe.serving.service.api.requeststatistics;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.RequestStatisticsMysqlModel;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.RequestStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;

/**
 * @author ivenn.zheng
 */
@Api(path = "requeststatistics/query-list", name = "query request statistics list")
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<RequestStatisticsMysqlModel>> {

    @Autowired
    private RequestStatisticsService requestStatisticsService;

    @Override
    protected ApiResult<PagingOutput<RequestStatisticsMysqlModel>> handle(Input input) throws StatusCodeWithException, IOException {
        return success(requestStatisticsService.queryList(input));
    }

    public static class Input extends PagingInput {

        /**
         * 开始时间
         */
        private Long startTime;

        /**
         * 结束时间
         */
        private Long endTime;

        /**
         * 服务名称
         */
        private String serviceId;

        /**
         * 客户名称
         */
        private String clientId;

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public Long getEndTime() {
            return endTime;
        }

        public void setEndTime(Long endTime) {
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
