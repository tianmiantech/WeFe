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

package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.api.requeststatistics.QueryListApi;
import com.welab.wefe.serving.service.database.serving.entity.RequestStatisticsMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.RequestStatisticsRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author ivenn.zheng
 * desc: 用于统计接口调用信息
 */
@Service
public class RequestStatisticsService {

    @Autowired
    private RequestStatisticsRepository requestStatisticsRepository;

    /**
     * query request statistics list
     *
     * @param input
     * @return
     */
    public PagingOutput<RequestStatisticsMysqlModel> queryList(QueryListApi.Input input) {
        List<RequestStatisticsMysqlModel> list = requestStatisticsRepository.groupByServiceIdAndClientId(input.getServiceId(),
                input.getClientId(), input.getStartTime(), input.getEndTime());
        return PagingOutput.of(list.size(), list);
    }


}
