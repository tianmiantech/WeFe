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

package com.welab.wefe.board.service.database.repository;

import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
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
public interface ProjectDataSetRepository extends BaseRepository<ProjectDataSetMySqlModel, String> {

    /**
     * When a member leaves, its data set is disabled.
     *
     * @param projectId project id
     * @param memberId  Id of the leaving member
     */
    @Modifying
    @Transactional
    @Query(value = "update #{#entityName} set audit_status='disagree',audit_comment='成员已退出，数据集不可用。' where project_id=?1 and member_id=?2 and member_role=?3", nativeQuery = true)
    void disableDataSetWhenMemberExist(String projectId, String memberId, String memberRole);

    @Query(value = "select * from #{#entityName} where data_set_id=?1 and audit_status='agree'", nativeQuery = true)
    List<ProjectDataSetMySqlModel> queryUsageInProject(String dataSetId);

    /**
     * Query the number of data sets to be reviewed
     */
    @Query(value = "select count(*) from #{#entityName} where project_id=?1 and member_id=?2 and audit_status='auditing' and source_type is null", nativeQuery = true)
    int queryNeedAuditDataSetCount(String projectId, String memberId);
}
