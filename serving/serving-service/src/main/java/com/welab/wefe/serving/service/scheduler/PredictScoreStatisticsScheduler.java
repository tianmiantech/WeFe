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


import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import com.welab.wefe.serving.service.service.ModelPredictScoreStatisticsService;
import com.welab.wefe.serving.service.service.TableModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用于定时将订单按每分钟进行统计
 *
 * @author ivenn.zheng
 * @date 2022/04/28
 */
@Component
public class PredictScoreStatisticsScheduler {

    private Logger logger = LoggerFactory.getLogger(PredictScoreStatisticsScheduler.class);

    @Autowired
    private ModelPredictScoreStatisticsService statisticsService;

    @Autowired
    private TableModelRepository tableModelRepository;

    @Autowired
    private TableModelService tableModelService;

    /**
     * 整分触发一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void statistics() {
        List<String> serviceIds = tableModelRepository.getAllServiceId();

        serviceIds
                .stream()
                .filter(serviceId -> tableModelService.isHaveScoredDistribution(serviceId))
                .forEach(serviceId -> statisticsService.refresh(serviceId));
    }
}
