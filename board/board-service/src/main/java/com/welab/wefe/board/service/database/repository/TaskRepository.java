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

package com.welab.wefe.board.service.database.repository;

import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Zane
 */
@Repository
public interface TaskRepository extends BaseRepository<TaskMySqlModel, String> {
    @Query(value = "select * from #{#entityName} where job_id=?1 and flow_node_id=?2 and role=?3 order by created_time desc limit 1;", nativeQuery = true)
    TaskMySqlModel findOne(String jobId, String flowNodeId, String role);

    @Query(value = "select * from #{#entityName} where role!='arbiter' and job_id=?1 and task_conf -> '$.params.grid_search_param.need_grid_search'=true;", nativeQuery = true)
    List<TaskMySqlModel> findTaskWithGridSearch(String jobId);
}
