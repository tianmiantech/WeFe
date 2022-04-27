/*
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.database.serving.entity.OrderStatisticsMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.OrderStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author ivenn.zheng
 * @date 2022/4/27
 */
@Service
public class OrderStatisticsService {

    @Autowired
    OrderStatisticsRepository orderStatisticsRepository;

    public void save(OrderStatisticsMysqlModel input) {

        OrderStatisticsMysqlModel model = orderStatisticsRepository.findOne("id", input.getId(), OrderStatisticsMysqlModel.class);
        if (null == model) {
            model = new OrderStatisticsMysqlModel();
        }
        model = ModelMapper.map(input, OrderStatisticsMysqlModel.class);
        model.setUpdatedBy(input.getUpdatedBy());
        model.setUpdatedTime(new Date());

        orderStatisticsRepository.save(model);
    }
}
