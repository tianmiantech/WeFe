/**
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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.database.entity.data_set.DataSetColumnMysqlModel;
import com.welab.wefe.board.service.database.repository.DataSetColumnRepository;
import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnInputModel;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnOutputModel;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.OrderBy;
import com.welab.wefe.common.web.CurrentAccount;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zane.luo
 */
@Service
public class DataSetColumnService extends AbstractService {
    @Autowired
    DataSetColumnRepository dataSetColumnRepository;

    public PagingOutput<DataSetColumnOutputModel> list(String dataSetId) {
        Specification<DataSetColumnMysqlModel> where = Where
                .create()
                .equal("dataSetId", dataSetId)
                .orderBy("index", OrderBy.asc)
                .build(DataSetColumnMysqlModel.class);

        // The front end does not do paging,
        // but considering that there may be a data set with a particularly large number of fields,
        // the paging method is used to query here.
        return dataSetColumnRepository.paging(
                where,
                new PagingInput(0, 10000),
                DataSetColumnOutputModel.class
        );
    }

    public void update(String dataSetId, List<DataSetColumnInputModel> list, CurrentAccount.Info userInfo) {
        // clear data set columns
        dataSetColumnRepository.deleteByDataSetId(dataSetId);

        // save data set columns
        for (int i = 0; i < list.size(); i++) {
            DataSetColumnInputModel item = list.get(i);

            DataSetColumnMysqlModel column = new ModelMapper().map(item, DataSetColumnMysqlModel.class);
            column.setCreatedBy(userInfo.id);
            column.setDataSetId(dataSetId);
            column.setIndex(i);

            dataSetColumnRepository.save(column);
        }
    }
}
