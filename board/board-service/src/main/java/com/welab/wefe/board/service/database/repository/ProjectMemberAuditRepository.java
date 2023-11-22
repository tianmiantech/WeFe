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

import com.welab.wefe.board.service.database.entity.job.ProjectMemberAuditMySqlModel;
import com.welab.wefe.board.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zane.luo
 */
@Repository
public interface ProjectMemberAuditRepository extends BaseRepository<ProjectMemberAuditMySqlModel, String> {

    /**
     * 移除指定成员的审核记录
     * 1. 需要他审核的记录
     * 2. 对他进行审核的记录
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "delete from #{#entityName} where project_id=?1 and (auditor_id=?2 or member_id=?2)", nativeQuery = true)
    void deleteAuditingRecord(String projectId, String auditorId);
}
