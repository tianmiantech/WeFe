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
package com.welab.wefe.serving.service.service;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.orderstatistics.QueryListApi;
import com.welab.wefe.serving.service.api.orderstatistics.SaveApi;
import com.welab.wefe.serving.service.database.serving.entity.OrderStatisticsMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.OrderStatisticsRepository;
import com.welab.wefe.serving.service.dto.OrderStatisticsInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author ivenn.zheng
 * @date 2022/4/27
 */
@Service
public class OrderStatisticsService {
    private Logger log = LoggerFactory.getLogger(OrderStatisticsService.class);


    @Autowired
    OrderStatisticsRepository orderStatisticsRepository;

    public void save(SaveApi.Input input) {

        OrderStatisticsMysqlModel model = orderStatisticsRepository.findOne("id", input.getId(), OrderStatisticsMysqlModel.class);
        if (null == model) {
            model = new OrderStatisticsMysqlModel();
            input.setId(model.getId());
        }
        model = ModelMapper.map(input, OrderStatisticsMysqlModel.class);
        model.setUpdatedBy(input.getUpdatedBy());
        model.setUpdatedTime(new Date());

        orderStatisticsRepository.save(model);
    }

    public void insertList(List<OrderStatisticsMysqlModel> list) {
        orderStatisticsRepository.saveAll(list);
    }

    public PagingOutput<QueryListApi.Output> queryList(QueryListApi.Input input) {
        List<Map<String, Object>> list = new ArrayList<>();

        switch (input.getStatisticalGranularity()) {
            case "month":

                list = orderStatisticsRepository.groupByMonth(
                        input.getServiceId(),
                        input.getServiceName(),
                        input.getRequestPartnerId(),
                        input.getRequestPartnerName(),
                        input.getResponsePartnerId(),
                        input.getResponsePartnerName(),
                        input.getStartTime(),
                        input.getEndTime());
                break;
            case "day":
                list = orderStatisticsRepository.groupByDay(
                        input.getServiceId(),
                        input.getServiceName(),
                        input.getRequestPartnerId(),
                        input.getRequestPartnerName(),
                        input.getResponsePartnerId(),
                        input.getResponsePartnerName(),
                        input.getStartTime(),
                        input.getEndTime());
                break;
            case "hour":
                list = orderStatisticsRepository.groupByHour(
                        input.getServiceId(),
                        input.getServiceName(),
                        input.getRequestPartnerId(),
                        input.getRequestPartnerName(),
                        input.getResponsePartnerId(),
                        input.getResponsePartnerName(),
                        input.getStartTime(),
                        input.getEndTime());
                break;
            case "minute":
                list = orderStatisticsRepository.groupByMinute(
                        input.getServiceId(),
                        input.getServiceName(),
                        input.getRequestPartnerId(),
                        input.getRequestPartnerName(),
                        input.getResponsePartnerId(),
                        input.getResponsePartnerName(),
                        input.getStartTime(),
                        input.getEndTime());
                break;
            default:
                log.info("order statistics not match!");
                break;
        }

        String jsonString = JSON.toJSONString(list);
        List<QueryListApi.Output> outputs = JSON.parseArray(jsonString, QueryListApi.Output.class);
        return PagingOutput.of(outputs.size(), outputs);

    }


    public void insert(OrderStatisticsMysqlModel model) {
        orderStatisticsRepository.save(model);
    }

    public List<OrderStatisticsMysqlModel> getByParams(OrderStatisticsInput input) {


        Specification<OrderStatisticsMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .equal("requestPartnerId", input.getRequestPartnerId())
                .equal("responsePartnerId", input.getResponsePartnerId())
                .equal("minute", input.getMinute())
                .betweenAndDate("createdTime", input.getStartTime() == null ? null : input.getStartTime().getTime(), input.getEndTime() == null ? null : input.getEndTime().getTime())
                .build(OrderStatisticsMysqlModel.class);


        return orderStatisticsRepository.findAll(where);
    }


}
