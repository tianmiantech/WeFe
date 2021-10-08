/**
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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.database.entity.job.TaskProgressMysqlModel;
import com.welab.wefe.board.service.database.repository.TaskProgressRepository;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * @author lonnie
 */
@Service
public class TaskProgressService extends AbstractService {

    @Autowired
    private TaskProgressRepository taskProgressRepository;

    public TaskProgressMysqlModel findOne(String taskId, JobMemberRole role) {
        Specification<TaskProgressMysqlModel> where = Where
                .create()
                .equal("taskId", taskId)
                .equal("role", role)
                .build(TaskProgressMysqlModel.class);

        return taskProgressRepository.findOne(where).orElse(null);
    }
}
