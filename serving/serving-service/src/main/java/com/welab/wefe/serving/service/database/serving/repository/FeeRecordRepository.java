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
package com.welab.wefe.serving.service.database.serving.repository;

import com.welab.wefe.serving.service.database.serving.entity.FeeDetailOutputModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author ivenn.zheng
 * @date 2021/12/23
 */
@Repository
public interface FeeRecordRepository extends BaseRepository<FeeDetailOutputModel, String> {

    /**
     * 查询计费详情列表
     * @param clientName
     * @param serviceName
     * @param serviceType
     * @param queryType
     * @param startTime
     * @param endTime
     * @param pageOffset
     * @param pageSize
     * @return
     */
    @Query(value = "select concat(fd.service_id, fd.client_id,fd.fee_config_id,fd.created_time) as id, fd.service_name,fd.service_id as service_id, fd.client_name, " +
            "fd.client_id as client_id ,fd.service_type ,fd.unit_price,DATE_FORMAT(fd.created_time ,:query_type) as query_date , " +
            "fd.pay_type, sum(fd.total_request_times) as total_request_times, sum(fd.total_fee) as total_fee, fd.fee_config_id " +
            "from fee_detail fd  " +
            "where if(:service_name !='', fd.service_name like concat('%',:service_name,'%'), 1=1) " +
            "       and if(:client_name != '', fd.client_name like concat('%',:client_name,'%'),1=1) " +
            "       and if(:service_type != '', fd.service_type = :service_type,1=1) " +
            "       and fd.created_time  between if(:start_time is not null, :start_time, '1900-01-01 00:00:00') " +
            "       and if(:end_time is not null ,:end_time ,NOW())  " +
            "group by fd.service_id, fd.client_id ,fd.fee_config_id, DATE_FORMAT(fd.created_time ,:query_type) " +
            "order by fd.created_time desc limit :pageOffset,:pageSize ", nativeQuery = true, countProjection = "1")
    List<FeeDetailOutputModel> queryList(@Param("client_name") String clientName,
                                         @Param("service_name") String serviceName,
                                         @Param("service_type") String serviceType,
                                         @Param("query_type") String queryType,
                                         @Param("start_time") Date startTime,
                                         @Param("end_time") Date endTime,
                                         @Param("pageOffset") Integer pageOffset,
                                         @Param("pageSize") Integer pageSize);


    /**
     * count
     *
     * @param clientName
     * @param serviceName
     * @param serviceType
     * @param queryType
     * @param startTime
     * @param endTime
     * @return
     */
    @Query(value =
            "select count(t.total) " +
            "from(  " +
                "select count(*) as total " +
                "from fee_detail fd  " +
                "where if(:service_name !='', fd.service_name like concat('%',:service_name,'%'), 1=1) " +
                "       and if(:client_name != '', fd.client_name like concat('%',:client_name,'%'),1=1) " +
                "       and if(:service_type != '', fd.service_type = :service_type,1=1) " +
                "       and fd.created_time  between if(:start_time is not null, :start_time, '1900-01-01 00:00:00') " +
                "       and if(:end_time is not null ,:end_time ,NOW())  " +
                "group by fd.service_id, fd.client_id , fd.fee_config_id,DATE_FORMAT(fd.created_time ,:query_type) " +
            ")t ", nativeQuery = true, countProjection = "1")
    Integer count(@Param("client_name") String clientName,
                  @Param("service_name") String serviceName,
                  @Param("service_type") String serviceType,
                  @Param("query_type") String queryType,
                  @Param("start_time") Date startTime,
                  @Param("end_time") Date endTime);
}
