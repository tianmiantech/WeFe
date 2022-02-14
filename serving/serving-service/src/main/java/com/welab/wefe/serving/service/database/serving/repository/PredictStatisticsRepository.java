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

import com.welab.wefe.serving.service.database.serving.entity.PredictStatisticsMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author hunter.zhao
 */
@Repository
public interface PredictStatisticsRepository extends BaseRepository<PredictStatisticsMySqlModel, String> {

    /**
     * Find data based on criteria
     *
     * @param modelId
     * @param memberId
     * @return PredictStatisticsMySqlModel
     */
    PredictStatisticsMySqlModel findByModelIdAndMemberId(String modelId, String memberId);

    /**
     * Gets all member ids
     *
     * @return List<String>
     */
    @Query(value = "select member_id from predict_log group by member_id", nativeQuery = true)
    List<String> getMemberId();

    /**
     * Gets all model ids
     *
     * @return List<String>
     */
    @Query(value = "select model_id from predict_log group by model_id", nativeQuery = true)
    List<String> getModelId();


    /**
     * Find by day
     *
     * @param modelId
     * @param memberId
     * @param startDate
     * @param endDate
     * @return List<PredictStatisticsMySqlModel>
     */
    @Query(value = "select id,member_id,model_id,month,day,hour,minute,sum(total) total,sum(success) success,sum(fail) fail,created_time,updated_time " +
            "from predict_statistics where 1=1 " +
            "and if(:modelId != '', model_id = :modelId, 1 = 1) " +
            "and if(:memberId != '', member_id = :memberId, 1 = 1) " +
            "and if(:startDate != '', day >= :startDate, 1 = 1) " +
            "and if(:endDate != '', day <= :endDate, 1 = 1) " +
            "group by day "
            , nativeQuery = true, countProjection = "1")
    List<PredictStatisticsMySqlModel> findByDay(@Param("modelId") String modelId, @Param("memberId") String memberId
            , @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * Find by hour
     *
     * @param modelId
     * @param memberId
     * @param startDate
     * @param endDate
     * @return List<PredictStatisticsMySqlModel>
     */
    @Query(value = "select id,member_id,model_id,month,day,hour,minute,sum(total) total,sum(success) success,sum(fail) fail,created_time,updated_time " +
            "from predict_statistics where 1=1 " +
            "and if(:modelId != '', model_id = :modelId, 1 = 1) " +
            "and if(:memberId != '', member_id = :memberId, 1 = 1) " +
            "and if(:startDate != '', hour >= :startDate, 1 = 1) " +
            "and if(:endDate != '', hour <= :endDate, 1 = 1) " +
            "group by hour "
            , nativeQuery = true, countProjection = "1")
    List<PredictStatisticsMySqlModel> findByHour(@Param("modelId") String modelId, @Param("memberId") String memberId
            , @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * Find by month
     *
     * @param modelId
     * @param memberId
     * @param startDate
     * @param endDate
     * @return List<PredictStatisticsMySqlModel>
     */
    @Query(value = "select id,member_id,model_id,month,day,hour,minute,sum(total) total,sum(success) success,sum(fail) fail,created_time,updated_time " +
            "from predict_statistics where 1=1 " +
            "and if(:modelId != '', model_id = :modelId, 1 = 1) " +
            "and if(:memberId != '', member_id = :memberId, 1 = 1) " +
            "and if(:startDate != '', month >= :startDate, 1 = 1) " +
            "and if(:endDate != '', month <= :endDate, 1 = 1) " +
            "group by month "
            , nativeQuery = true, countProjection = "1")
    List<PredictStatisticsMySqlModel> findByMonth(@Param("modelId") String modelId, @Param("memberId") String memberId
            , @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * Find by minute
     *
     * @param modelId
     * @param memberId
     * @param startDate
     * @param endDate
     * @return List<PredictStatisticsMySqlModel>
     */
    @Query(value = "select id,member_id,model_id,month,day,hour,minute,sum(total) total,sum(success) success,sum(fail) fail,created_time,updated_time " +
            "from predict_statistics where 1=1 " +
            "and if(:modelId != '', model_id = :modelId, 1 = 1) " +
            "and if(:memberId != '', member_id = :memberId, 1 = 1) " +
            "and if(:startDate != '', minute >= :startDate, 1 = 1) " +
            "and if(:endDate != '', minute <= :endDate, 1 = 1) " +
            "group by minute "
            , nativeQuery = true, countProjection = "1")
    List<PredictStatisticsMySqlModel> findByMinute(@Param("modelId") String modelId, @Param("memberId") String memberId
            , @Param("startDate") String startDate, @Param("endDate") String endDate);
}
