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
package com.welab.wefe.serving.service.scheduler;


import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.serving.service.api.serviceorder.QueryListApi;
import com.welab.wefe.serving.service.database.serving.entity.*;
import com.welab.wefe.serving.service.dto.OrderStatisticsInput;
import com.welab.wefe.serving.service.dto.ServiceOrderInput;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import com.welab.wefe.serving.service.service.OrderStatisticsService;
import com.welab.wefe.serving.service.service.ServiceOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用于定时将订单按每分钟进行统计
 *
 * @author ivenn.zheng
 * @date 2022/04/28
 */
@Component
public class OrderStatisticsScheduler {

    private Logger logger = LoggerFactory.getLogger(OrderStatisticsScheduler.class);

    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private OrderStatisticsService orderStatisticsService;


    /**
     * 整分触发一次
     */
    @Scheduled(cron = "0 0-59 * * * ?")
    public void orderStatistics() {

        try {
            logger.info("OrderStatisticsScheduler start in: " + DateUtil.getCurrentDate());

            Date endTime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            calendar.set(Calendar.SECOND, 0);
            calendar.setTime(endTime);
            // 统计前1分钟
            calendar.add(Calendar.MINUTE, -1);
            Date startTime = calendar.getTime();

            ServiceOrderInput input = new ServiceOrderInput();
            input.setUpdatedStartTime(startTime);
            input.setUpdatedEndTime(endTime);
            input.setStatus(ServiceOrderEnum.ORDERING.getValue());


            List<ServiceOrderMysqlModel> serviceOrderMysqlModels = serviceOrderService.getByParams(input);

            if (serviceOrderMysqlModels.size() > 0) {
                // 根据 serviceId,requestPartnerId,responsePartnerId, updatedTime 进行分组
                Map<String, Map<String, Map<String, Map<String, List<ServiceOrderMysqlModel>>>>> collect = serviceOrderMysqlModels.stream().collect(
                        Collectors.groupingBy(ServiceOrderMysqlModel::getServiceId,
                                Collectors.groupingBy(ServiceOrderMysqlModel::getRequestPartnerId,
                                        Collectors.groupingBy(ServiceOrderMysqlModel::getResponsePartnerId,
                                                Collectors.groupingBy(x -> DateUtil.toString(x.getUpdatedTime(), DateUtil.YYYY_MM_DD_HH_MM))))));


                List<OrderStatisticsMysqlModel> orderStatisticsMysqlModels = new ArrayList<>();

                collect.forEach((k, v) -> {
                    v.forEach((k1, v1) -> {
                        v1.forEach((k2, v2) -> {
                            v2.forEach((k3, list) -> {
                                ServiceOrderMysqlModel serviceOrderMysqlModel = list.get(0);
                                OrderStatisticsMysqlModel os = new OrderStatisticsMysqlModel();
                                int successTimes = (int) list.stream().filter(x ->
                                        ServiceOrderEnum.SUCCESS.getValue().equals(x.getStatus())).count();

                                int failedTimes = (int) list.stream().filter(x ->
                                        ServiceOrderEnum.FAILED.getValue().equals(x.getStatus())).count();

                                int callTimes = list.size();
                                os.setCallTimes(callTimes);
                                os.setSuccessTimes(successTimes);
                                os.setFailedTimes(failedTimes);
                                os.setMinute(DateUtil.toString(endTime, DateUtil.YYYY_MM_DD_HH_MM));
                                os.setHour(DateUtil.toString(endTime, DateUtil.YYYY_MM_DD_HH));
                                os.setDay(DateUtil.toString(endTime, DateUtil.YYYY_MM_DD));
                                os.setMonth(DateUtil.toString(endTime, DateUtil.YYYY_MM));
                                os.setRequestPartnerId(serviceOrderMysqlModel.getRequestPartnerId());
                                os.setRequestPartnerName(serviceOrderMysqlModel.getRequestPartnerName());
                                os.setResponsePartnerId(serviceOrderMysqlModel.getResponsePartnerId());
                                os.setResponsePartnerName(serviceOrderMysqlModel.getResponsePartnerName());
                                os.setServiceId(serviceOrderMysqlModel.getServiceId());
                                os.setServiceName(serviceOrderMysqlModel.getServiceName());
                                os.setCreatedTime(endTime);
                                os.setUpdatedTime(endTime);

                                orderStatisticsMysqlModels.add(os);
                            });

                        });
                    });
                });

                // 存储统计结果
                orderStatisticsService.insertList(orderStatisticsMysqlModels);
                logger.info("insert statistics success, insert size: " + orderStatisticsMysqlModels.size());
            } else {
                logger.info("there is no order statistics to save between " +
                        DateUtil.toString(startTime, DateUtil.YYYY_MM_DD_HH_MM) +
                        " -- " + DateUtil.toString(endTime, DateUtil.YYYY_MM_DD_HH_MM));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("error occur when fee details scheduler execute, happen in " + DateUtil.getCurrentDate());
        }


    }


}
