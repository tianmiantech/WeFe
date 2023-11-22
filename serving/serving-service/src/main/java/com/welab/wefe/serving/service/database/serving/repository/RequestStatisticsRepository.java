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

import com.welab.wefe.serving.service.database.serving.entity.RequestStatisticsMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


/**
 * @author ivenn.zheng
 */
@Repository
public interface RequestStatisticsRepository extends BaseRepository<RequestStatisticsMysqlModel, String> {

    /**
     * 根据入参查询相应的接口请求统计结果
     *
     * @param serviceId
     * @param clientId
     * @param startTime
     * @param endTime
     * @return
     */
    @Query(value = "select concat(t.service_id ,t.client_id) as id, t.service_id, t.service_name, t.client_id, " +
            "t.client_name, t.success_request as total_success_times, t.total_request as total_request_times, " +
            "t.total_request - t.success_request as total_fail_times, t.service_type " +
            "from ( " +
            "SELECT count(arr.request_result < 3 or null) as success_request, count(id) as total_request, arr.client_id, " +
            "arr.service_id, max(arr.service_name) as service_name, max(arr.client_name) as client_name ,max(arr.service_type) as service_type " +
            "from api_request_record arr  " +
            "where if(:service_id != '', arr.service_id = :service_id, 1=1) and  " +
            "      if(:client_id != '', arr.client_id = :client_id, 1=1) and  " +
            "      arr.created_time  between if(:start_time is not null, :start_time, '1900-01-01 00:00:00') and  " +
            "      if(:end_time is not null ,:end_time ,NOW())  " +
            "group by arr.service_id, arr.client_id)as t ", nativeQuery = true, countProjection = "1")
    List<RequestStatisticsMysqlModel> groupByServiceIdAndClientId(@Param("service_id") String serviceId,
                                                                  @Param("client_id") String clientId,
                                                                  @Param("start_time") Date startTime,
                                                                  @Param("end_time") Date endTime);

}
