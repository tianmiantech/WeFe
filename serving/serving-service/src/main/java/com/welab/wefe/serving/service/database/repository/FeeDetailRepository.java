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

import com.welab.wefe.serving.service.database.entity.FeeDetailMysqlModel;
import com.welab.wefe.serving.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author ivenn.zheng
 */
@Repository
public interface FeeDetailRepository extends BaseRepository<FeeDetailMysqlModel, String> {

    /**
     * get last record in table fee_detail
     * @return
     */
    @Query(value = "select * from fee_detail as fd order by fd.updated_time desc LIMIT 1 ", nativeQuery = true)
    FeeDetailMysqlModel getLastRecord();

}
