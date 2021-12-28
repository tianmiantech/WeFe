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

import com.welab.wefe.serving.service.api.feedetail.QueryListApi;
import com.welab.wefe.serving.service.database.serving.entity.FeeDetailOutputModel;
import com.welab.wefe.serving.service.database.serving.repository.FeeRecordRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.QueryDateTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * @author ivenn.zheng
 * @date 2021/12/23
 */
@Service
public class FeeRecordService {

    @Autowired
    private FeeRecordRepository feeRecordRepository;


    public PagingOutput<FeeDetailOutputModel> queryList(QueryListApi.Input input) {
        List<FeeDetailOutputModel> models = null;
        if (input.getQueryDateType() == null || input.getQueryDateType() == QueryDateTypeEnum.MONTH.getValue()) {

            models = feeRecordRepository.queryList(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y-%m", input.getStartTime(), input.getEndTime());
        } else if (input.getQueryDateType() == QueryDateTypeEnum.YEAR.getValue()) {
            models = feeRecordRepository.queryList(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y", input.getStartTime(), input.getEndTime());
        } else if (input.getQueryDateType() == QueryDateTypeEnum.DAY.getValue()) {
            models = feeRecordRepository.queryList(input.getClientName(), input.getServiceName(),
                    input.getServiceType(), "%Y-%m-%d", input.getStartTime(), input.getEndTime());
        }
        return PagingOutput.of(models.size(), models);
    }

}
