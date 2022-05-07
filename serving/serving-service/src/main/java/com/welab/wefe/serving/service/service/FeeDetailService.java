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

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.feedetail.QueryListApi;
import com.welab.wefe.serving.service.database.serving.entity.FeeDetailMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.FeeDetailOutputModel;
import com.welab.wefe.serving.service.database.serving.repository.FeeDetailRepository;
import com.welab.wefe.serving.service.database.serving.repository.FeeRecordRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.PayTypeEnum;
import com.welab.wefe.serving.service.enums.QueryDateTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceResultEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FeeDetailService {

    @Autowired
    private FeeRecordRepository feeRecordRepository;

    @Autowired
    private FeeDetailRepository feeDetailRepository;

    public void save(FeeDetailMysqlModel input) {

        FeeDetailMysqlModel model = feeDetailRepository.findOne("id", input.getId(), FeeDetailMysqlModel.class);
        if (null == model) {
            model = new FeeDetailMysqlModel();
        }
        BeanUtils.copyProperties(input, model);
        model.setCreatedTime(input.getCreatedTime() != null ? input.getCreatedTime() : new Date());
        feeDetailRepository.save(model);
    }


    public PagingOutput<QueryListApi.Output> queryList(QueryListApi.Input input) {
        List<FeeDetailOutputModel> models = new ArrayList<>();
        Integer total = 0;
        if (input.getQueryDateType() == null || input.getQueryDateType() == QueryDateTypeEnum.HOUR.getValue()) {
            models = feeRecordRepository.queryList(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y-%m-%d %H:00:00",
                    input.getStartTime(), input.getEndTime(), input.getPageIndex() * input.getPageSize(), input.getPageSize());
            total = feeRecordRepository.count(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y-%m-%d %H:00:00",
                    input.getStartTime(), input.getEndTime());
        } else if (input.getQueryDateType() == QueryDateTypeEnum.YEAR.getValue()) {

            models = feeRecordRepository.queryList(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y", input.getStartTime(), input.getEndTime(), input.getPageIndex() * input.getPageSize(), input.getPageSize());
            total = feeRecordRepository.count(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y",
                    input.getStartTime(), input.getEndTime());
        } else if (input.getQueryDateType() == QueryDateTypeEnum.DAY.getValue()) {
            models = feeRecordRepository.queryList(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y-%m-%d", input.getStartTime(), input.getEndTime(), input.getPageIndex() * input.getPageSize(), input.getPageSize());
            total = feeRecordRepository.count(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y-%m-%d",
                    input.getStartTime(), input.getEndTime());
        } else if (input.getQueryDateType() == QueryDateTypeEnum.MONTH.getValue()) {
            models = feeRecordRepository.queryList(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y-%m", input.getStartTime(), input.getEndTime(), input.getPageIndex() * input.getPageSize(), input.getPageSize());
            total = feeRecordRepository.count(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y-%m",
                    input.getStartTime(), input.getEndTime());
        }

        List<QueryListApi.Output> list = new ArrayList<>();
        models.forEach(x -> {
            QueryListApi.Output output = ModelMapper.map(x, QueryListApi.Output.class);
            output.setServiceType(x.getServiceType());
            output.setPayType(x.getPayType());
            list.add(output);
        });

        return PagingOutput.of(total == null ? 0 : total, list);
    }

    public FeeDetailMysqlModel getByIdAndDateTime(String serviceId, String clientId, Date lastTime) {

        Specification<FeeDetailMysqlModel> where = Where.create()
                .equal("serviceId", serviceId)
                .equal("clientId", clientId)
                .equal("createdTime", lastTime)
                .build(FeeDetailMysqlModel.class);
        return feeDetailRepository.findOne(where).orElse(null);
    }


}
