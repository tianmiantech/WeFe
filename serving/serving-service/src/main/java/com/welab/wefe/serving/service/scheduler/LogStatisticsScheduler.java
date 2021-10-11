/**
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

import com.welab.wefe.serving.service.service.PredictStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author lonnie
 */
@Component
public class LogStatisticsScheduler {

    private Logger logger = LoggerFactory.getLogger(LogStatisticsScheduler.class);

    @Autowired
    private PredictStatisticsService predictStatisticsService;

    @Scheduled(initialDelay = 5_000, fixedDelay = 120_000)
    public void logStatistics() {
        logger.info("logStatistics start");

        predictStatisticsService.statisticsLog(null, null, null, null);

        logger.info("logStatistics end");
    }
}
