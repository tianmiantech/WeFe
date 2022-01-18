/*
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
package com.welab.wefe.serving.service.database.serving.repository;

import com.welab.wefe.serving.service.database.serving.entity.FeeDetailOutputModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author ivenn.zheng
 * @date 2021/12/23
 */
@Repository
public interface FeeRecordRepository extends BaseRepository<FeeDetailOutputModel, String> {

    /**
     * 查询计费详情列表
     *
     * @param clientName
     * @param serviceName
     * @param serviceType
     * @param queryType
     * @param startTime
     * @param endTime
     * @return
     */
    @Query(value = "select replace(uuid(),'-','') as id, s.name as service_name,s.id as service_id, c.name as client_name, " +
            "c.id as client_id ,s.service_type ,fc.unit_price,  " +
            "fc.pay_type, sum(fd.total_request_times) as total_request_times, sum(fd.total_fee) as total_fee, " +
            "DATE_FORMAT(fd.created_time ,:query_type) as query_date " +
            "from fee_detail fd  " +
            "left join service s on fd.service_id = s.id  " +
            "left join client c on fd.client_id = c.id " +
            "left join fee_config fc on fc.service_id = fd.service_id and fc.client_id = fd.client_id  " +
            "where if(:service_name !='', s.name like concat('%',:service_name,'%'), 1=1) " +
            "       and if(:client_name != '', c.name like concat('%',:client_name,'%'),1=1) " +
            "       and if(:service_type is not null, s.service_type = :service_type,1=1) " +
            "       and fd.created_time  between if(:start_time is not null, :start_time, '1900-01-01 00:00:00') " +
            "       and if(:end_time is not null ,:end_time ,NOW())  " +
            "group by DATE_FORMAT(fd.created_time ,:query_type) " +
            "order by fd.created_time desc ", nativeQuery = true, countProjection = "1")
    List<FeeDetailOutputModel> queryList(@Param("client_name") String clientName,
                                         @Param("service_name") String serviceName,
                                         @Param("service_type") Integer serviceType,
                                         @Param("query_type") String queryType,
                                         @Param("start_time") Long startTime,
                                         @Param("end_time") Long endTime);
}
