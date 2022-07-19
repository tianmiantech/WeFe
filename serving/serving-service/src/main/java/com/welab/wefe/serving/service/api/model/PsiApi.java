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
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
@Api(path = "model/psi", name = "模型稳定性指标", desc = "模型稳定性指标")
public class PsiApi extends AbstractApi<PsiApi.Input, PsiApi.Output> {

    @Autowired
    TableModelRepository tableModelRepository;

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        //expected
        TableModelMySqlModel model = tableModelRepository.findOne("serviceId", input.serviceId, TableModelMySqlModel.class);

        JObject result = extractScoreDistributionData(JObject.create(model.getScoresDistribution()));

        List<String> dataKey = result.keySet().stream().sorted()
                .collect(Collectors.toList());

        List<List<Object>> dataList = Lists.newArrayList();

        for (int i = 0; i < dataKey.size(); i++) {
            String key = dataKey.get(i);
            dataList.add(
                    Arrays.asList(
                            extractXAxis(dataKey, i, key),
                            extractYAxis(result, key),
                            extractYAxis2(result, key)
                    )
            );
        }

        Output output = new Output();
        output.setExpected(dataList);


        return success(output);
    }

    private JObject extractScoreDistributionData(JObject obj) {
        String curveKey = "train_validate_VertLR_16196034768611638_scores_distribution";
        JObject scoresDistributionData = obj.getJObject(curveKey);
        JObject data = scoresDistributionData.getJObject("data");
        JObject result = data.getJObject("bin_result");
        return result;
    }

    private double extractYAxis2(JObject result, String key) {
        double rate = result.getJObject(key).getDoubleValue("count_rate");
        return precisionProcessByDouble(rate);
    }

    private int extractYAxis(JObject result, String key) {
        return result.getJObject(key).getIntValue("count");
    }

    private String extractXAxis(List<String> dataKey, int i, String key) {
        String beforeKey = i == 0 ? "0" : dataKey.get(i - 1);
        return precisionProcessByString(beforeKey) + "~" + precisionProcessByString(key);
    }

    private double precisionProcessByDouble(double value) {
        BigDecimal bd = new BigDecimal(value);
        return bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private double precisionProcessByString(String value) {
        BigDecimal bd = new BigDecimal(value);
        return bd.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
    }



    public static class Output extends AbstractApiOutput {
        private Object expected;

        private String actual;

        public Object getExpected() {
            return expected;
        }

        public void setExpected(Object expected) {
            this.expected = expected;
        }

        public String getActual() {
            return actual;
        }

        public void setActual(String actual) {
            this.actual = actual;
        }
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "主键id", require = true)
        private String serviceId;

        @Check(name = "开始时间")
        private Date beginTime = new Date();

        @Check(name = "结束时间")
        private Date endTime = new Date();


        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public Date getBeginTime() {
            return beginTime;
        }

        public void setBeginTime(Date beginTime) {
            this.beginTime = beginTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }
    }
}
