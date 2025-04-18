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

import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Zane
 */
@Repository
public interface JobRepository extends BaseRepository<JobMySqlModel, String> {
    @Query(value = "select * from #{#entityName} where job_id=?1 and my_role=?2 ", nativeQuery = true)
    JobMySqlModel findByJobId(String jobId, String role);

    @Query(value = "select * from #{#entityName} where flow_id=?1 and my_role=?2 order by created_time desc limit 1;", nativeQuery = true)
    JobMySqlModel findLastByFlowId(String flowId, String role);

    /**
     * 查询尚未结束的 job_id 数量
     * <p>
     * 尚未结束状态：'wait_run','running','wait_stop','wait_success'
     */
    @Query(
            value = "select count(DISTINCT job_id) from #{#entityName} where `status` in ('wait_run','running','wait_stop','wait_success');",
            nativeQuery = true)
    int runningJobCount();
}
