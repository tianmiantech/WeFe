/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.database.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;

import java.util.List;

@Repository("tableModelRepository")
public interface TableModelRepository extends BaseServiceRepository<TableModelMySqlModel> {
    /**
     * Gets all member ids
     *
     * @return List<String>
     */
    @Query(value = "select service_id from base_service where service_type = 7 group by service_id", nativeQuery = true)
    List<String> getAllServiceId();
}
