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

import com.welab.wefe.board.service.database.entity.job.ProjectFlowMySqlModel;
import com.welab.wefe.board.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zane.luo
 */
@Repository
public interface ProjectFlowRepository extends BaseRepository<ProjectFlowMySqlModel, String> {

    /**
     * Count the number of processes in different states in the project
     */
    @Query(value = "select flow_status,count(flow_status) from project_flow where project_id=?1 and deleted = 0 group by flow_status", nativeQuery = true)
    List<Object[]> countProjectFlowStatus(String projectId);

    /**
     * 置顶
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update #{#entityName} set top=true,sort_num=((SELECT num FROM (SELECT MAX(sort_num) AS num FROM project_flow) AS sub_selected) + 1) where flow_id=?1", nativeQuery = true)
    void top(String flowId);

    /**
     * 取消置顶
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update #{#entityName} set top=false, sort_num=0 where flow_id=?1", nativeQuery = true)
    void cancelTop(String flowId);
}
