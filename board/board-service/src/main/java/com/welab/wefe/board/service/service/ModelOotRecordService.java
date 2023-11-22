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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.database.entity.job.ModelOotRecordMysqlModel;
import com.welab.wefe.board.service.database.repository.ModelOotRecordRepository;
import com.welab.wefe.common.data.mysql.Where;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * @author aaron.li
 **/
@Service
public class ModelOotRecordService {

    @Autowired
    private ModelOotRecordRepository modelOotRecordRepository;

    public void save(ModelOotRecordMysqlModel modelOotRecordMysqlModel) {
        modelOotRecordRepository.save(modelOotRecordMysqlModel);
    }

    /**
     * Query model scoring and verification records according to process ID
     *
     * @param flowId flow id
     * @return Model scoring verification record
     */
    public ModelOotRecordMysqlModel findByFlowId(String flowId) {
        Specification<ModelOotRecordMysqlModel> where = Where
                .create()
                .equal("flowId", flowId)
                .build(ModelOotRecordMysqlModel.class);
        return modelOotRecordRepository.findOne(where).orElse(null);
    }

    /**
     * Query model scoring and verification records according to job ID and node ID
     */
    public ModelOotRecordMysqlModel findByJobIdAndModelFlowNodeId(String jobId, String nodeId) {
        Specification<ModelOotRecordMysqlModel> where = Where
                .create()
                .equal("ootJobId", jobId)
                .equal("ootModelFlowNodeId", nodeId)
                .build(ModelOotRecordMysqlModel.class);
        return modelOotRecordRepository.findOne(where).orElse(null);
    }
}
