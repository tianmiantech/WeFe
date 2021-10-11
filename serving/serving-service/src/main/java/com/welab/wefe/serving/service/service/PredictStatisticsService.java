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

package com.welab.wefe.serving.service.service;

import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.collect.Lists;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.service.api.logger.StatisticsApi;
import com.welab.wefe.serving.service.database.serving.entity.PredictStatisticsMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.PredictStatisticsRepository;
import com.welab.wefe.serving.service.manager.ModelManager;
import com.welab.wefe.serving.service.utils.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
@Service
public class PredictStatisticsService {

    private Logger log = LoggerFactory.getLogger(PredictStatisticsService.class);

    @Autowired
    private PredictStatisticsRepository predictStatisticsRepository;


    /**
     * Query by time interval. Time format: yyyy MM DD HH: mm
     */
    public void statisticsLog(String modelId, String memberId, String startDate, String endDate) {

        Map<String, String> minuteIntervalMap;

        if (StringUtil.isNotEmpty(startDate) && StringUtil.isNotEmpty(endDate)) {
            minuteIntervalMap = initMinuteIntervalMap(false, DateUtil.fromString(startDate, DateUtil.YYYY_MM_DD_HH_MM), DateUtil.fromString(endDate, DateUtil.YYYY_MM_DD_HH_MM));
        } else {
            minuteIntervalMap = initMinuteIntervalMap(true, null, null);
        }

        if (StringUtil.isNotEmpty(memberId) && StringUtil.isEmpty(modelId)) {

            List<String> modelIds = predictStatisticsRepository.getModelId();
            for (String modelIdStr : modelIds) {
                insertPredictStatistics(modelIdStr, memberId, minuteIntervalMap);
            }
        } else if (StringUtil.isEmpty(memberId) && StringUtil.isNotEmpty(modelId)) {

            List<String> memberIds = predictStatisticsRepository.getMemberId();
            for (String memberIdStr : memberIds) {
                insertPredictStatistics(modelId, memberIdStr, minuteIntervalMap);
            }
        } else if (StringUtil.isEmpty(memberId) && StringUtil.isEmpty(modelId)) {

            List<String> memberIds = predictStatisticsRepository.getMemberId();
            List<String> modelIds = predictStatisticsRepository.getModelId();
            for (String memberIdStr : memberIds) {
                for (String modelIdStr : modelIds) {
                    insertPredictStatistics(modelIdStr, memberIdStr, minuteIntervalMap);
                }
            }
        } else {

            insertPredictStatistics(modelId, memberId, minuteIntervalMap);
        }
    }

    /**
     * Insert statistical logging
     */
    @Transactional(rollbackFor = Exception.class)
    private void insertPredictStatistics(String modelId, String memberId, Map<String, String> minuteIntervalMap) {

        for (Map.Entry<String, String> entry : minuteIntervalMap.entrySet()) {

            int successNum = countLog(entry.getKey(), entry.getValue(), memberId, modelId, true);
            int failNum = countLog(entry.getKey(), entry.getValue(), memberId, modelId, false);

            PredictStatisticsMySqlModel predictStatisticsMySqlModel = getOneByCondition(modelId, memberId, entry.getKey());
            if (predictStatisticsMySqlModel != null) {
                continue;
            }

            String minute = entry.getKey();
            PredictStatisticsMySqlModel model = new PredictStatisticsMySqlModel();
            model.setMemberId(memberId);
            model.setModelId(modelId);
            model.setDateFields(minute);
            model.setMinute(minute);
            model.setSuccess(successNum);
            model.setFail(failNum);
            model.setTotal(successNum + failNum);

            predictStatisticsRepository.save(model);
        }
    }

    /**
     * Count the number of success / failure logs in minutes
     */
    public int countLog(String startTime, String endTime, String memberId, String modelId, boolean result) {
        String sql = "select count(*) from predict_log where created_time >= '" + startTime + "' and created_time <'" + endTime +
                "' and member_id='" + memberId + "' and model_id='" + modelId + "' and result=" + result;

        List list = predictStatisticsRepository.query(sql);
        return list == null ? 0 : TypeUtils.castToInt(((Object[]) list.get(0))[0]);
    }

    /**
     * Get a record by query criteria
     */
    public PredictStatisticsMySqlModel getOneByCondition(String modelId, String memberId, String minuteStr) {

        Specification<PredictStatisticsMySqlModel> where = Where
                .create()
                .equal("modelId", modelId)
                .equal("memberId", memberId)
                .equal("minute", minuteStr)
                .build(PredictStatisticsMySqlModel.class);

        return predictStatisticsRepository.findOne(where).orElse(null);
    }

    /**
     * Paging query
     */
    public List<StatisticsApi.Output> query(StatisticsApi.Input input) {

        Date endDate = new Date();
        List<PredictStatisticsMySqlModel> data = null;
        switch (input.getDateType()) {
            case "month":
                data = predictStatisticsRepository.findByMonth(
                        input.getModelId(),
                        input.getMemberId(),
                        DateUtil.toString(DateUtil.addMonths(endDate, 1 - input.getInterval()), DateUtil.YYYY_MM),
                        DateUtil.toString(endDate, DateUtil.YYYY_MM));
                break;
            case "day":
                data = predictStatisticsRepository.findByDay(
                        input.getModelId(),
                        input.getMemberId(),
                        DateUtil.toString(DateUtil.addDays(endDate, 1 - input.getInterval()), DateUtil.YYYY_MM_DD),
                        DateUtil.toString(endDate, DateUtil.YYYY_MM_DD)
                );
                break;
            case "hour":
                data = predictStatisticsRepository.findByHour(
                        input.getModelId(),
                        input.getMemberId(),
                        DateUtil.toString(DateUtil.addHours(endDate, 1 - input.getInterval()), DateUtil.YYYY_MM_DD_HH),
                        DateUtil.toString(endDate, DateUtil.YYYY_MM_DD_HH));
                break;
            case "minute":
                data = predictStatisticsRepository.findByMinute(
                        input.getModelId(),
                        input.getMemberId(),
                        DateUtil.toString(DateUtil.addMinutes(endDate, 1 - input.getInterval()), DateUtil.YYYY_MM_DD_HH_MM),
                        DateUtil.toString(endDate, DateUtil.YYYY_MM_DD_HH_MM));
                break;
            default:
                log.info("predict_statistics not match!");
                break;
        }

        if (data == null) {
            return Lists.newArrayList();
        }

        List<StatisticsApi.Output> list = data
                .stream()
                .map(x -> ModelMapper.map(x, StatisticsApi.Output.class))
                .collect(Collectors.toList());

        /**
         * Redundant model information
         */
        for (StatisticsApi.Output vo : list) {
            try {
                BaseModel model = ModelManager.getModelParam(vo.getModelId());
                if (model == null) {
                    continue;
                }
                vo.setAlgorithm(model.getAlgorithm());
                vo.setFlType(model.getFlType());
                vo.setMyRole(model.getMyRole());
            } catch (StatusCodeWithException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    /**
     * useDefault=true The default time interval is nearly 10 minutes map：eg(key:2020-11-09 12:00,value:2020-11-09 12:01)
     * useDefault=false If StartDate and endDate are not empty, calculate the two time intervals
     */
    public static Map<String, String> initMinuteIntervalMap(boolean useDefault, Date startDate, Date endDate) {

        Map<String, String> dateIntervalMap = new LinkedHashMap<>();

        //Nearly 10 minutes by default
        long number = 10;
        if (!useDefault && startDate != null && endDate != null) {
            number = DateUtil.intervalSeconds(startDate.getTime(), endDate.getTime()) / 60;
        }

        Date date = new Date();
        for (int i = 1; i <= number; i++) {
            String startTime = DateUtil.toString(DateUtil.addMinutes(date, -i), DateUtil.YYYY_MM_DD_HH_MM);
            String endTime = DateUtil.toString(DateUtil.addMinutes(date, -i + 1), DateUtil.YYYY_MM_DD_HH_MM);
            dateIntervalMap.put(startTime, endTime);
        }

        return dateIntervalMap;
    }
}
