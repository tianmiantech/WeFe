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
import com.welab.wefe.serving.service.api.feedetail.QueryListApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.FeeDetailMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.FeeDetailOutputModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceRepository;
import com.welab.wefe.serving.service.database.serving.repository.FeeDetailRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.QueryDateTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeeDetailService {

    @Autowired
    private FeeDetailRepository feeDetailRepository;

    @Autowired
    private ClientServiceRepository clientServiceRepository;

    public void save(FeeDetailMysqlModel input) {

        FeeDetailMysqlModel model = feeDetailRepository.findOne("id", input.getId(), FeeDetailMysqlModel.class);
        if (null == model) {
            model = new FeeDetailMysqlModel();
        }

        model.setServiceId(input.getServiceId());
        model.setClientId(input.getClientId());
        model.setTotalFee(input.getTotalFee());
        model.setTotalRequestTimes(input.getTotalRequestTimes());
        model.setCreatedTime(new Date());

        feeDetailRepository.save(model);
    }


    /**
     * 该接口用于统计
     *
     * @param input
     * @return
     */
    public PagingOutput<FeeDetailOutputModel> queryStatistics(QueryListApi.Input input) {

        // return result
        List<FeeDetailOutputModel> list = new ArrayList<>();

        Specification<FeeDetailMysqlModel> feeDetailMysqlModelSpecification = Where.create()
                .contains("serviceName", input.getServiceName())
                .contains("clientName", input.getClientName())
                .equal("serviceType", input.getServiceType())
                .betweenAndDate("createdTime", input.getStartTime(), input.getEndTime())
                .build(FeeDetailMysqlModel.class);

        List<FeeDetailMysqlModel> feeDetailMysqlModels = feeDetailRepository.findAll(feeDetailMysqlModelSpecification);

        List<FeeDetailMysqlModel> feeDetailMysqlModelsNew = new ArrayList<>();
        if (input.getQueryDateType() == null || input.getQueryDateType() == QueryDateTypeEnum.HOUR.getValue()) {

            feeDetailMysqlModels.stream()
                    .collect(Collectors.groupingBy(feeDetailMysqlModel -> DateUtil.toString(feeDetailMysqlModel.getCreatedTime(), "%Y-%m-%d %H:00:00")))
                    .forEach((k, v) -> {
                        v.stream().reduce((v1,v2) -> {
                            v1.setTotalFee(v1.getTotalFee().add(v2.getTotalFee()));
                            v2.setTotalRequestTimes(v1.getTotalRequestTimes() + v2.getTotalRequestTimes());
                            return v1;
                        });
                    });


        }



        // 2、根据  fee_config_id 获取费用

        // 3、根据时间范围、统计类型、统计 费用、调用次数

        // 4、根据分页参数，返回数值
        return null;

    }

}
