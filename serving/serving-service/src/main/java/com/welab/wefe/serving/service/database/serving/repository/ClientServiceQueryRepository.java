/**
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

package com.welab.wefe.serving.service.database.serving.repository;

import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceOutputModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author ivenn.zheng
 */
@Repository
public interface ClientServiceQueryRepository extends BaseRepository<ClientServiceOutputModel, String> {


    /**
     * 查询列表
     * @param serviceName
     * @param clientName
     * @param status
     * @return
     */
    @Query(value = "select * from " +
            "(SELECT cs.id , s.name as serviceName, c.name as clientName, cs.status as userStatus," +
            " cs.created_time as createdTime, s.service_type as serviceType, c.ip_add as ipAdd ,s.url " +
            "FROM wefe_serving.client_service cs " +
            "left join service s on cs.service_id = s.id " +
            "left join client c on cs.client_id = c.id ) as t " +
            "where if(:service_name is not null ,t.serviceName = :service_name, 1=1) " +
            "and if(:client_name is not null ,t.clientName = :client_name, 1=1) " +
            "and if(:status is not null ,t.userStatus = :status, 1=1) ", nativeQuery = true ,countProjection = "1")
    List<ClientServiceOutputModel> queryClientServiceList(@Param("service_name") String serviceName,
                                                          @Param("client_name") String clientName,
                                                          @Param("status") Integer status);


    /**
     * 根据 id 查询相关内容
     * @param id
     * @return
     */
    @Query(value = "SELECT cs.id , s.name as serviceName, c.name as clientName, cs.status , " +
            "s.service_type as serviceType, c.ip_add as ipAdd ,s.url " +
            "FROM client_service cs " +
            "left join service s on cs.service_id = s.id " +
            "left join client c on cs.client_id = c.id " +
            "where cs.id = :id ", nativeQuery = true)
    ClientServiceOutputModel queryOne(@Param("id") String id);


}
