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

package com.welab.wefe.serving.service.database.serving.repository;

import com.welab.wefe.serving.service.database.serving.entity.ModelMemberBaseModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author hunter.zhao
 */
@Repository
public interface ModelMemberBaseRepository extends BaseRepository<ModelMemberBaseModel, String> {

    /**
     * find data by model id and role
     *
     * @param modelId
     * @param role
     * @return List<ModelMemberBaseModel>
     */
    @Query(value = "select mm.id,mm.model_id,mm.member_id,mm.role,m.api " +
            "from  model_member mm left join member m on mm.member_id = m.member_id " +
            "where mm.model_id = :model_id and mm.role = :role", nativeQuery = true, countProjection = "1")
    List<ModelMemberBaseModel> findAllByModelIdAndRole(@Param("model_id") String modelId, @Param("role") String role);
}
