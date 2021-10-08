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

package com.welab.wefe.board.service.database.repository;

import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Zane
 */
@Repository
public interface DataSetRepository extends BaseRepository<DataSetMysqlModel, String> {

    @Query(value = "select tags,count(tags) as count from #{#entityName} where tags<>'' group by tags;", nativeQuery = true)
    List<Object[]> listAllTags();

    @Query(value = "select count(*) from #{#entityName} where name=?1", nativeQuery = true)
    int countByName(String name);

    @Query(value = "select count(*) from #{#entityName} where name=?1 and id<>?2", nativeQuery = true)
    int countByName(String name, String id);

    @Modifying
    @Transactional
    @Query(value = "update data_set set usage_count_in_project=(select count(*) from project_data_set where data_set_id=?1 and audit_status='agree') where id=?1", nativeQuery = true)
    void updateUsageCountInProject(String dataSetId);
}
