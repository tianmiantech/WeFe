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

import com.welab.wefe.serving.service.database.serving.entity.FeeDetailMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.FeeDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class FeeDetailService {

    @Autowired
    private FeeDetailRepository feeDetailRepository;

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


}
