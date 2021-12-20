/**
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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.database.entity.OutputModelMysqlModel;
import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.repository.JobRepository;
import com.welab.wefe.board.service.database.repository.OutputModelRepository;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


/**
 * @author jacky.jiang
 **/
@Service
public class FeatureDataOutputInfoService extends AbstractService {
    @Autowired
    JobRepository featureJobRepo;

    @Autowired
    JobMemberService jobMemberService;

    @Autowired
    TaskService taskService;

    @Autowired
    OutputModelRepository outputModelRepository;


    /**
     * Packaging results
     */
    private JObject wrapModelResult(OutputModelMysqlModel outputModelMysqlModel) {
        JObject resultObj = JObject.create();
        if (null != outputModelMysqlModel) {
            resultObj.append("model_meta", JObject.create(outputModelMysqlModel.getModelMeta()))
                    .append("model_param", JObject.create(outputModelMysqlModel.getModelParam()));
        }
        return resultObj;
    }

    /**
     * Fuzzy matching of a task record based on task ID and name
     *
     * @param myRole my job member role
     */
    public JobMySqlModel find(JobMemberRole myRole, String jobId) {

        // Find out the data first
        Specification<JobMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("myRole", myRole)
                .build(JobMySqlModel.class);

        JobMySqlModel featureJobMySqlModel = featureJobRepo.findOne(where).orElse(null);
        return featureJobMySqlModel;
    }
}
