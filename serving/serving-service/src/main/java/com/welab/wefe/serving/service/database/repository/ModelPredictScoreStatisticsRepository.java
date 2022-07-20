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

import com.welab.wefe.serving.service.database.entity.ModelPredictScoreStatisticsMySqlModel;
import com.welab.wefe.serving.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Map;

/**
 * @author ivenn.zheng
 */
@Repository
public interface ModelPredictScoreStatisticsRepository extends BaseRepository<ModelPredictScoreStatisticsMySqlModel, String> {
    @Query(value = "select splitPoint,sum(count) " +
            "from model_predict_score_statistics " +
            "where service_id = :service_id " +
            "and created_time between if(:begin_time is not null, :begin_time,'1900-01-01 00:00:00') " +
            "and if(:end_time is not null ,:end_time ,NOW() ) " +
            "group by splitPoint", nativeQuery = true)
    Map<Double, Integer> countBy(@Param("service_id") String serviceId, @Param("begin_time") Date beginTime, @Param("end_time") Date endTime);
}
