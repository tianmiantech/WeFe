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
package com.welab.wefe.serving.service.api.model;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.entity.StatisticsSumModel;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.database.repository.PredictScoreStatisticsRepository;
import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
@Api(path = "model/psi", name = "模型稳定性指标", desc = "模型稳定性指标")
public class PsiApi extends AbstractApi<PsiApi.Input, PsiApi.Output> {

    @Autowired
    TableModelRepository tableModelRepository;

    @Autowired
    PredictScoreStatisticsRepository statisticsRepository;

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        Output output = Output.create(
                extractExpectedData(input.getServiceId()),
                extractActualData(input.getServiceId(), input.getStartTime(), input.getEndTime()),
                extractActualDataByGroup(input)
        );
        return success(output);
    }

    private List<List<Object>> extractActualData(String serviceId, Date startTime, Date endTime) {

        List<StatisticsSumModel> binningList = getBinningInfo(serviceId, startTime, endTime);

        List<StatisticsSumModel> temp = sort(binningList);

        int total = sum(temp);

        List<List<Object>> dataList = Lists.newArrayList();

        for (int i = 0; i < temp.size(); i++) {
            StatisticsSumModel model = temp.get(i);
            dataList.add(
                    Arrays.asList(
                            extractXAxis2(temp, i, model.getSplitPoint()),
                            model.getCount(),
                            rate(total, model.getCount().doubleValue())));
        }

        return dataList;
    }

    private List<StatisticsSumModel> sort(List<StatisticsSumModel> count) {
        return count
                .stream()
                .sorted(Comparator.comparing(StatisticsSumModel::getSplitPoint))
                .collect(Collectors.toList());
    }

    private List<StatisticsSumModel> getBinningInfo(String serviceId, Date startTime, Date endTime) {
        return statisticsRepository.countBy(serviceId, startTime, endTime);
    }

    private List<Object> extractActualDataByGroup(Input input) {
        List<DayModel> days = getDayList(input);

        return days
                .stream()
                .map(x -> {
                    List<List<Object>> psiList = psiList(input.getServiceId(), x.getStartTime(), x.getEndTime());
                    Map<String, Object> map = new HashMap();
                    map.put(formatDate(x.getStartTime()), psiList);
                    return map;
                })
                .collect(Collectors.toList());
    }

    private double rate(int total, double count) {
        return total == 0 ? 0 : count / total;
    }

    public List<List<Object>> psiList(String serviceId, Date startTime, Date endTime) {

        List<StatisticsSumModel> temp = getBinningInfo(serviceId, startTime, endTime);

        List<StatisticsSumModel> temp1 = sort(temp);

        int total = sum(temp);

        TableModelMySqlModel model = tableModelRepository.findOne("serviceId", serviceId, TableModelMySqlModel.class);
        if (model == null) {
            return Lists.newArrayList();
        }
        JObject result = JObject.create(model.getScoresDistribution()).getJObjectByPath("data.bin_result");

        List<List<Object>> dataList = Lists.newArrayList();

        for (int i = 0; i < temp1.size(); i++) {
            StatisticsSumModel split = temp1.get(i);
            double actualRate = rate(total, split.getCount().doubleValue());
            double expectedRate = extractYAxis2(result, split.getSplitPoint().toString());
            dataList.add(
                    Arrays.asList(
                            extractXAxis2(temp, i, split.getSplitPoint()),
                            split.getCount(),
                            actualRate,
                            psi(actualRate, expectedRate)
                    ));
        }


        return dataList;
    }

    private double psi(double actual, double expected) {
        return subtract(actual, expected) * ln(actual, expected);
    }

    private double subtract(double actual, double expected) {
//        double temp = actual == 0.0 ? 0.1 : actual;
        return actual - expected;
    }

    private double ln(double actual, double expected) {
//        double tempActual = actual == 0.0 ? 0.1 : actual;
//        double tempExpected = expected == 0.0 ? 0.1 : expected;
        return Math.log(actual / expected);
    }

    public static void main(String[] args) {
        double actual = 0;
        double expected = 0.4;

        actual = actual == 0.0 ? 0.1 : actual;

        double temp = actual - expected;
        double ln = Math.log(actual / expected);
        System.out.println(temp * ln);
    }

    private int sum(List<StatisticsSumModel> count) {
        return count
                .stream()
                .mapToInt(StatisticsSumModel::getCount)
                .sum();
    }

    private String formatDate(Date date) {
        return DateUtil.timeInMillisToDate(date.getTime(), DateUtil.YYYY_MM_DD);
    }

    private List<DayModel> getDayList(Input input) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(input.getStartTime());
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        List<DayModel> list = Lists.newArrayList();

        Date start;
        Date end;
        do {
            start = calendar.getTime();
            calendar.add(Calendar.DATE, input.getStep());
            calendar.add(Calendar.SECOND, -1);
            end = calendar.getTime();
            list.add(DayModel.of(start, end));
        } while (end.getTime() <= input.getEndTime().getTime());

        return list;
    }

    private List<List<Object>> extractExpectedData(String serviceId) {

        TableModelMySqlModel model = tableModelRepository.findOne("serviceId", serviceId, TableModelMySqlModel.class);
        if (model == null || JObject.create(model.getScoresDistribution()).isEmpty()) {
            return Lists.newArrayList();
        }

        JObject result = JObject.create(model.getScoresDistribution()).getJObjectByPath("data.bin_result");

        List<Double> dataKey = result
                .keySet()
                .stream()
                .map(x -> Double.valueOf(x))
                .sorted()
                .collect(Collectors.toList());

        List<List<Object>> dataList = Lists.newArrayList();
        for (int i = 0; i < dataKey.size(); i++) {
            Double key = dataKey.get(i);
            dataList.add(
                    Arrays.asList(
                            extractXAxis(dataKey, i, key),
                            extractYAxis(result, key.toString()),
                            extractYAxis2(result, key.toString()))
            );
        }

        return dataList;
    }

    private double extractYAxis2(JObject result, String key) {
        double rate = result.getJObject(key).getDoubleValue("count_rate");
        return newScale(rate, 2);
    }

    private int extractYAxis(JObject result, String key) {
        return result.getJObject(key).getIntValue("count");
    }

    private String extractXAxis(List<Double> dataKey, int i, Double key) {
        Double beforeKey = i == 0 ? 0.0 : dataKey.get(i - 1);
        return newScale(beforeKey, 3) + "~" + newScale(key, 3);
    }

    private String extractXAxis2(List<StatisticsSumModel> dataKey, int i, Double key) {
        Double beforeKey = i == 0 ? 0.0 : dataKey.get(i - 1).getSplitPoint();
        return newScale(beforeKey, 3) + "~" + newScale(key, 3);
    }

    private double newScale(double value, int scale) {
        BigDecimal bd = new BigDecimal(value);
        return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    public static class Output extends AbstractApiOutput {
        private Object expected;

        private Object actual;

        private Object dataGrid;

        public static Output create(Object expected, Object actual, Object dataGrid) {
            Output output = new Output();
            output.expected = expected;
            output.actual = actual;
            output.dataGrid = dataGrid;
            return output;
        }

        public Object getExpected() {
            return expected;
        }

        public void setExpected(Object expected) {
            this.expected = expected;
        }

        public Object getActual() {
            return actual;
        }

        public void setActual(Object actual) {
            this.actual = actual;
        }

        public Object getDataGrid() {
            return dataGrid;
        }

        public void setDataGrid(Object dataGrid) {
            this.dataGrid = dataGrid;
        }
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "主键id", require = true)
        private String serviceId;

        @Check(name = "开始时间", require = true)
        private Date startTime;

        @Check(name = "结束时间", require = true)
        private Date endTime = new Date();

        @Check(name = "周期")
        private int step = 1;

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }
    }


    public static class DayModel {

        private Date startTime;

        private Date endTime;

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        private static DayModel of(Date beginTime, Date endTime) {
            DayModel dayModel = new DayModel();
            dayModel.startTime = beginTime;
            dayModel.endTime = endTime;
            return dayModel;
        }
    }
}
