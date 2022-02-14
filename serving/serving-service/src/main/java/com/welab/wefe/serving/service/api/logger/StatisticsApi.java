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

package com.welab.wefe.serving.service.api.logger;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.service.service.PredictStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


/**
 * @author hunter.zhao
 */
@Api(path = "log/statistics", name = "Get log statistics", login = false)
public class StatisticsApi extends AbstractApi<StatisticsApi.Input, List<StatisticsApi.Output>> {
    @Autowired
    PredictStatisticsService predictStatisticsService;

    @Override
    protected ApiResult<List<Output>> handle(Input input) {
        return success(predictStatisticsService.query(input));
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "成员id")
        private String memberId;

        @Check(name = "模型ID")
        private String modelId;

        @Check(name = "日期类型", require = true)
        private String dateType;

        @Check(name = "查询间隔", require = true)
        private Integer interval;

        //region getter/setter

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public String getDateType() {
            return dateType;
        }

        public void setDateType(String dateType) {
            this.dateType = dateType;
        }

        public Integer getInterval() {
            return interval;
        }

        public void setInterval(Integer interval) {
            this.interval = interval;
        }

        //endregion
    }

    public static class Output extends AbstractApiInput {

        private String memberId;

        private String modelId;

        private Algorithm algorithm;

        private FederatedLearningType flType;

        private JobMemberRole myRole;

        private String month;

        private String day;

        private String hour;

        private String minute;

        private long total;

        private long success;

        private long fail;

        //region getter/setter

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        public FederatedLearningType getFlType() {
            return flType;
        }

        public void setFlType(FederatedLearningType flType) {
            this.flType = flType;
        }

        public JobMemberRole getMyRole() {
            return myRole;
        }

        public void setMyRole(JobMemberRole myRole) {
            this.myRole = myRole;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getHour() {
            return hour;
        }

        public void setHour(String hour) {
            this.hour = hour;
        }

        public String getMinute() {
            return minute;
        }

        public void setMinute(String minute) {
            this.minute = minute;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getSuccess() {
            return success;
        }

        public void setSuccess(long success) {
            this.success = success;
        }

        public long getFail() {
            return fail;
        }

        public void setFail(long fail) {
            this.fail = fail;
        }


        //endregion
    }
}
