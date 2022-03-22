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
package com.welab.wefe.board.service.database.repository;

import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zane
 * @date 2021/11/10
 */
@Repository
public interface ImageDataSetSampleRepository extends BaseRepository<ImageDataSetSampleMysqlModel, String> {
    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByDataSetId(String dataSetId);

    @Query(value = "select label_list from #{#entityName} where data_set_id=?1 and labeled=true;", nativeQuery = true)
    List<String> getAllLabelList(String dataSetId);

    @Query(value = "select label_list from #{#entityName} where data_set_id=?1 and labeled=true group by label_list;", nativeQuery = true)
    List<String> getAllDistinctLabelList(String dataSetId);


    @Query(value = "select count(*) from #{#entityName} where data_set_id=?1 and labeled=true", nativeQuery = true)
    long getLabeledCount(String dataSetId);

    @Query(value = "select count(*) from #{#entityName} where data_set_id=?1", nativeQuery = true)
    long getSampleCount(String dataSetId);

}
