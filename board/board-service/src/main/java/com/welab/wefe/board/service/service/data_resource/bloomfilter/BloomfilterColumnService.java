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

package com.welab.wefe.board.service.service.data_resource.bloomfilter;

import com.welab.wefe.board.service.database.entity.fusion.bloomfilter.BloomFilterColumnMysqlModel;
import com.welab.wefe.board.service.database.repository.fusion.BloomFilterColumnRepository;
import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.fusion.BloomFilterColumnInputModel;
import com.welab.wefe.board.service.dto.fusion.BloomFilterColumnOutputModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.OrderBy;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jacky.jiang
 */
@Service
public class BloomfilterColumnService extends AbstractService {
    @Autowired
    BloomFilterColumnRepository bloomfilterColumnRepository;

    public PagingOutput<BloomFilterColumnOutputModel> list(String dataSetId) {
        Specification<BloomFilterColumnMysqlModel> where = Where
                .create()
                .equal("dataSetId", dataSetId)
                .orderBy("index", OrderBy.asc)
                .build(BloomFilterColumnMysqlModel.class);

        // The front end does not do paging,
        // but considering that there may be a data set with a particularly large number of fields,
        // the paging method is used to query here.
        return bloomfilterColumnRepository.paging(
                where,
                new PagingInput(0, 10000), BloomFilterColumnOutputModel.class
        );
    }

    public void update(String dataSetId, List<BloomFilterColumnInputModel> list) {
        // clear data set columns
        bloomfilterColumnRepository.deleteByBloomfilterId(dataSetId);

        // save data set columns
        for (int i = 0; i < list.size(); i++) {
            BloomFilterColumnInputModel item = list.get(i);

            BloomFilterColumnMysqlModel column = new ModelMapper().map(item, BloomFilterColumnMysqlModel.class);
            column.setCreatedBy("test");
            column.setBloomfilterId(dataSetId);
            column.setIndex(i);

            bloomfilterColumnRepository.save(column);
        }
    }
}
