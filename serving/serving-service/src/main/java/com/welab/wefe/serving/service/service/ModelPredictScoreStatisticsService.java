/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.service.database.entity.ModelPredictScoreStatisticsMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.database.repository.ModelPredictScoreStatisticsRepository;
import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
@Service
public class ModelPredictScoreStatisticsService {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private TableModelRepository modelRepository;

    @Autowired
    private ModelPredictScoreStatisticsRepository statisticsRepository;

    public List<Double> findBinningSplitPoint(String serviceId) {
        TableModelMySqlModel model = modelRepository.findOne("serviceId", serviceId, TableModelMySqlModel.class);
        JObject scoresDistribution = JObject.create(model.getScoresDistribution());
        JObject binning = scoresDistribution.getJObjectByPath("data.bin_result");

        return binning
                .keySet()
                .stream()
                .map(x -> Double.valueOf(x))
                .sorted()
                .collect(Collectors.toList());
    }

    public void asyncIncrement(String serviceId, Double score) {
        CommonThreadPool.run(() -> increment(serviceId, score));
    }

    public void increment(String serviceId, Double score) {
        initCurrentDay(serviceId);

        for (double split : findBinningSplitPoint(serviceId)) {
            if (score <= split) {
                ModelPredictScoreStatisticsMySqlModel model = findByServiceIdAndDayAndBinning(serviceId, DateUtil.getCurrentDay(), split);
                model.setCount(model.getCount() + 1);
                model.setUpdatedTime(new Date());
                statisticsRepository.save(model);
                break;
            }
        }
    }

    private void initCurrentDay(String serviceId) {
        Specification<ModelPredictScoreStatisticsMySqlModel> where = Where
                .create()
                .equal("serviceId", serviceId)
                .equal("day", DateUtil.getCurrentDay())
                .build(ModelPredictScoreStatisticsMySqlModel.class);
        if (statisticsRepository.count(where) > 0) {
            return;
        }

        findBinningSplitPoint(serviceId)
                .forEach(x -> add(serviceId, x));
    }

    private void add(String serviceId, Double splitPoint) {
        ModelPredictScoreStatisticsMySqlModel model = new ModelPredictScoreStatisticsMySqlModel();
        model.setServiceId(serviceId);
        model.setSplitPoint(splitPoint);
        model.setDay(DateUtil.getCurrentDay());
        statisticsRepository.save(model);
    }

    private ModelPredictScoreStatisticsMySqlModel findByServiceIdAndDayAndBinning(String serviceId, Date day, Double splitPoints) {
        Specification<ModelPredictScoreStatisticsMySqlModel> where = Where
                .create()
                .equal("serviceId", serviceId)
                .equal("day", day)
                .equal("splitPoint", splitPoints)
                .build(ModelPredictScoreStatisticsMySqlModel.class);

        return statisticsRepository.findOne(where).get();
    }
}
