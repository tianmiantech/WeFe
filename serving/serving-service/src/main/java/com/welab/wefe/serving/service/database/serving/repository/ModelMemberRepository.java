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

import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.service.database.serving.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author hunter.zhao
 */
@Repository
public interface ModelMemberRepository extends BaseRepository<ModelMemberMySqlModel, String> {
    /**
     * Find data based on criteria
     *
     * @param modelId
     * @param memberId
     * @return List<ModelMemberMySqlModel>
     */
    List<ModelMemberMySqlModel> findByModelIdAndMemberId(String modelId, String memberId);

    /**
     * Find data based on criteria
     *
     * @param modelId
     * @param memberId
     * @param myRole
     * @return ModelMemberMySqlModel
     */
    ModelMemberMySqlModel findByModelIdAndMemberIdAndRole(String modelId, String memberId, JobMemberRole myRole);
}
