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

package com.welab.wefe.serving.service.database.serving.repository;

import com.welab.wefe.serving.service.api.orderstatistics.QueryListApi;
import com.welab.wefe.serving.service.database.serving.entity.OrderStatisticsMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author ivenn.zheng
 */
@Repository
public interface OrderStatisticsRepository extends BaseRepository<OrderStatisticsMysqlModel, String> {


    /**
     * 根据条件查询按"分钟粒度"统计订单
     *
     * @param serviceId
     * @param serviceName
     * @param requestPartnerId
     * @param requestPartnerName
     * @param responsePartnerId
     * @param responsePartnerName
     * @param startTime
     * @param endTime
     * @return
     */
    @Query(value = "SELECT sum(os.call_times) as call_times , sum(os.success_times) as success_times , sum(os.failed_times) as failed_times , " +
            "os.service_id ,os.service_name ,os.request_partner_id ,os.request_partner_name ,os.response_partner_id ,os.response_partner_name , " +
            "os.`minute` as date_time " +
            "FROM wefe_serving.order_statistics os " +
            "where if(:service_id !='', os.service_id = :service_id, 1=1) and " +
            "if(:service_name !='', os.service_name like concat('%',:service_name,'%'), 1=1) and " +
            "if(:request_partner_id !='', os.request_partner_id = :request_partner_id, 1=1) and " +
            "if(:request_partner_name !='', os.request_partner_name like concat('%',:request_partner_name,'%'), 1=1) and " +
            "if(:response_partner_id !='', os.response_partner_id = :response_partner_id, 1=1) and " +
            "if(:response_partner_name !='', os.response_partner_name like concat('%',:response_partner_name,'%'), 1=1) and " +
            "os.created_time  > if(:start_time is not null, :start_time, '1900-01-01 00:00:00') <=  " +
            "if(:end_time is not null ,:end_time ,NOW()) " +
            "group by os.minute ", nativeQuery = true, countProjection = "1")
    List<Map<String,Object>>  groupByMinute(@Param("service_id") String serviceId,
                                            @Param("service_name") String serviceName,
                                            @Param("request_partner_id") String requestPartnerId,
                                            @Param("request_partner_name") String requestPartnerName,
                                            @Param("response_partner_id") String responsePartnerId,
                                            @Param("response_partner_name") String responsePartnerName,
                                            @Param("start_time") Date startTime,
                                            @Param("end_time") Date endTime);

    /**
     * 根据条件查询按"小时粒度"统计订单
     *
     * @param serviceId
     * @param serviceName
     * @param requestPartnerId
     * @param requestPartnerName
     * @param responsePartnerId
     * @param responsePartnerName
     * @param startTime
     * @param endTime
     * @return
     */
    @Query(value = "SELECT sum(os.call_times) as call_times , sum(os.success_times) as success_times , sum(os.failed_times) as failed_times , " +
            "os.service_id ,os.service_name ,os.request_partner_id ,os.request_partner_name ,os.response_partner_id ,os.response_partner_name , " +
            "os.`hour` as date_time " +
            "FROM wefe_serving.order_statistics os " +
            "where if(:service_id !='', os.service_id = :service_id, 1=1) and " +
            "if(:service_name !='', os.service_name like concat('%',:service_name,'%'), 1=1) and " +
            "if(:request_partner_id !='', os.request_partner_id = :request_partner_id, 1=1) and " +
            "if(:request_partner_name !='', os.request_partner_name like concat('%',:request_partner_name,'%'), 1=1) and " +
            "if(:response_partner_id !='', os.response_partner_id = :response_partner_id, 1=1) and " +
            "if(:response_partner_name !='', os.response_partner_name like concat('%',:response_partner_name,'%'), 1=1) and " +
            "os.created_time  > if(:start_time is not null, :start_time, '1900-01-01 00:00:00') <=   " +
            "if(:end_time is not null ,:end_time ,NOW()) " +
            "group by os.hour ", nativeQuery = true, countProjection = "1")
    List<Map<String,Object>> groupByHour(@Param("service_id") String serviceId,
                                                @Param("service_name") String serviceName,
                                                @Param("request_partner_id") String requestPartnerId,
                                                @Param("request_partner_name") String requestPartnerName,
                                                @Param("response_partner_id") String responsePartnerId,
                                                @Param("response_partner_name") String responsePartnerName,
                                                @Param("start_time") Date startTime,
                                                @Param("end_time") Date endTime);

    /**
     * 根据条件查询按"按日粒度"统计订单
     *
     * @param serviceId
     * @param serviceName
     * @param requestPartnerId
     * @param requestPartnerName
     * @param responsePartnerId
     * @param responsePartnerName
     * @param startTime
     * @param endTime
     * @return
     */
    @Query(value = "SELECT sum(os.call_times) as call_times , sum(os.success_times) as success_times , sum(os.failed_times) as failed_times , " +
            "os.service_id ,os.service_name ,os.request_partner_id ,os.request_partner_name ,os.response_partner_id ,os.response_partner_name , " +
            "os.`day` as date_time " +
            "FROM wefe_serving.order_statistics os " +
            "where if(:service_id !='', os.service_id = :service_id, 1=1) and " +
            "if(:service_name !='', os.service_name like concat('%',:service_name,'%'), 1=1) and " +
            "if(:request_partner_id !='', os.request_partner_id = :request_partner_id, 1=1) and " +
            "if(:request_partner_name !='', os.request_partner_name like concat('%',:request_partner_name,'%'), 1=1) and " +
            "if(:response_partner_id !='', os.response_partner_id = :response_partner_id, 1=1) and " +
            "if(:response_partner_name !='', os.response_partner_name like concat('%',:response_partner_name,'%'), 1=1) and " +
            "os.created_time  > if(:start_time is not null, :start_time, '1900-01-01 00:00:00') <=   " +
            "if(:end_time is not null ,:end_time ,NOW()) " +
            "group by os.day ", nativeQuery = true, countProjection = "1")
    List<Map<String,Object>> groupByDay(@Param("service_id") String serviceId,
                                               @Param("service_name") String serviceName,
                                               @Param("request_partner_id") String requestPartnerId,
                                               @Param("request_partner_name") String requestPartnerName,
                                               @Param("response_partner_id") String responsePartnerId,
                                               @Param("response_partner_name") String responsePartnerName,
                                               @Param("start_time") Date startTime,
                                               @Param("end_time") Date endTime);

    /**
     * 根据条件查询按"按月粒度"统计订单
     *
     * @param serviceId
     * @param serviceName
     * @param requestPartnerId
     * @param requestPartnerName
     * @param responsePartnerId
     * @param responsePartnerName
     * @param startTime
     * @param endTime
     * @return
     */
    @Query(value = "SELECT sum(os.call_times) as call_times , sum(os.success_times) as success_times , sum(os.failed_times) as failed_times , " +
            "os.service_id ,os.service_name ,os.request_partner_id ,os.request_partner_name ,os.response_partner_id ,os.response_partner_name , " +
            "os.`month` as date_time " +
            "FROM wefe_serving.order_statistics os " +
            "where if(:service_id !='', os.service_id = :service_id, 1=1) and " +
            "if(:service_name !='', os.service_name like concat('%',:service_name,'%'), 1=1) and " +
            "if(:request_partner_id !='', os.request_partner_id = :request_partner_id, 1=1) and " +
            "if(:request_partner_name !='', os.request_partner_name like concat('%',:request_partner_name,'%'), 1=1) and " +
            "if(:response_partner_id !='', os.response_partner_id = :response_partner_id, 1=1) and " +
            "if(:response_partner_name !='', os.response_partner_name like concat('%',:response_partner_name,'%'), 1=1) and " +
            "os.created_time > if(:start_time is not null, :start_time, '1900-01-01 00:00:00') <=   " +
            "if(:end_time is not null ,:end_time ,NOW()) " +
            "group by os.month ", nativeQuery = true, countProjection = "1")
    List<Map<String,Object>> groupByMonth(@Param("service_id") String serviceId,
                                           @Param("service_name") String serviceName,
                                           @Param("request_partner_id") String requestPartnerId,
                                           @Param("request_partner_name") String requestPartnerName,
                                           @Param("response_partner_id") String responsePartnerId,
                                           @Param("response_partner_name") String responsePartnerName,
                                           @Param("start_time") Date startTime,
                                           @Param("end_time") Date endTime);
}
