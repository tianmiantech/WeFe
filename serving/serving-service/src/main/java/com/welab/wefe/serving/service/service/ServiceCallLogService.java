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
import com.welab.wefe.serving.service.database.serving.entity.ServiceCallLogMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ServiceCallLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author ivenn.zheng
 * @date 2022/4/27
 */
@Service
public class ServiceCallLogService {

    @Autowired
    ServiceCallLogRepository serviceCallLogRepository;

    public void save(ServiceCallLogMysqlModel input) {

        ServiceCallLogMysqlModel model = serviceCallLogRepository.findOne("id", input.getId(), ServiceCallLogMysqlModel.class);
        if (null == model) {
            model = new ServiceCallLogMysqlModel();
        }
        model = ModelMapper.map(input, ServiceCallLogMysqlModel.class);
        model.setUpdatedBy(input.getUpdatedBy());
        model.setUpdatedTime(new Date());

        serviceCallLogRepository.save(model);
    }


}
