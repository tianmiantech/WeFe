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

import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author zane.luo
 */
@Repository
public interface ProjectRepository extends BaseRepository<ProjectMySqlModel, String> {

    /**
     * 查询出我方非管理员创建的10天前的项目
     */
    @Query(value = "select * from #{#entityName}  " +
            "where 1=1 " +
            "and closed=false " +
            "and DATEDIFF(now(), created_time)>10 " +
            "and member_id=(select `value` from global_config where `group`='member_info' and `name`='member_id') " +
            "and created_by is not null " +
            "and created_by not in (select id from account where admin_role=true or super_admin_role=true)", nativeQuery = true)
    List<ProjectMySqlModel> findCreatedByThisMemberButNotAdminAccountBefore10DaysAgo();

    /**
     * 查询指定项目最后启动任务的时间
     */
    @Query(value = "select max(start_time) from `job` where project_id=?1", nativeQuery = true)
    Date getJobLastStartTime(String projectId);

    @Query(value = "select * from #{#entityName} where name=?1", nativeQuery = true)
    List<ProjectMySqlModel> findAllByName(String name);

    @Query(value = "select * from #{#entityName} where project_id=?1 limit 1;", nativeQuery = true)
    ProjectMySqlModel findOneById(String projectId);

    /**
     * 置顶
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update #{#entityName} set top=true,sort_num=((SELECT num FROM (SELECT MAX(sort_num) AS num FROM #{#entityName}) AS sub_selected) + 1) where project_id=?1", nativeQuery = true)
    void top(String projectId);

    /**
     * 取消置顶
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update #{#entityName} set top=false, sort_num=0 where project_id=?1", nativeQuery = true)
    void cancelTop(String projectId);
}
