/**
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

import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


/**
 * @author ivenn.zheng
 */
@Repository
public interface ClientServiceRepository extends BaseRepository<ClientServiceMysqlModel, String> {


    /**
     * 更新
     *
     * @param serviceId
     * @param clientId
     * @param status
     * @param updatedBy
     * @param updatedTime
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update client_service cs set " +
            "cs.updated_by = :updatedBy , cs.updated_time = :updatedTime , cs.status = :status " +
            "where cs.service_id = :serviceId and cs.client_id = :clientId ", nativeQuery = true)
    void updateByParam(@Param("serviceId") String serviceId,
                       @Param("clientId") String clientId,
                       @Param("status") Integer status,
                       @Param("updatedBy") String updatedBy,
                       @Param("updatedTime") Date updatedTime);

}
