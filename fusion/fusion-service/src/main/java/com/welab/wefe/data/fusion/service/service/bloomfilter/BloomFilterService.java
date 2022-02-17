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

package com.welab.wefe.data.fusion.service.service.bloomfilter;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.data.fusion.service.api.bloomfilter.DeleteApi;
import com.welab.wefe.data.fusion.service.api.bloomfilter.DetailApi;
import com.welab.wefe.data.fusion.service.api.bloomfilter.QueryApi;
import com.welab.wefe.data.fusion.service.database.entity.BloomFilterMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.BloomFilterRepository;
import com.welab.wefe.data.fusion.service.dto.base.PagingOutput;
import com.welab.wefe.data.fusion.service.dto.entity.bloomfilter.BloomfilterOutputModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author hunter.zhao
 */
@Service
public class BloomFilterService {
    LongAdder longAdder = new LongAdder();

    @Autowired
    private BloomFilterRepository bloomFilterRepository;

    /**
     * @param bloomFilterId
     */
    public void increment(String bloomFilterId) {
        BloomFilterMySqlModel model = bloomFilterRepository.findOne("id", bloomFilterId, BloomFilterMySqlModel.class);
        model.setUsedCount(model.getUsedCount() + 1);
        model.setUpdatedTime(new Date());
        bloomFilterRepository.save(model);
    }


    public BloomFilterMySqlModel findById(String bloomFilterId) {
        return bloomFilterRepository.findOne("id", bloomFilterId, BloomFilterMySqlModel.class);
    }


    /**
     * Filter list
     */
    public List<BloomFilterMySqlModel> list() {
        return bloomFilterRepository.findAll();
    }

    /**
     * Paging query data sets
     */
    public PagingOutput<BloomfilterOutputModel> query(QueryApi.Input input) {
        Specification<BloomFilterMySqlModel> where = Where
                .create()
                .equal("id", input.getId())
                .contains("name", input.getName())
                .build(BloomFilterMySqlModel.class);

        return bloomFilterRepository.paging(where, input, BloomfilterOutputModel.class);
    }

    /**
     * Delete filter
     *
     * @param input
     */
    public void delete(DeleteApi.Input input) {
        BloomFilterMySqlModel model = bloomFilterRepository.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        bloomFilterRepository.deleteById(input.getId());
        String src = model.getSrc();
        if(StringUtil.isEmpty(src)){
            return;
        }

        File file = new File(src);
        if(file.exists()) {
            file.delete();
            System.out.println("删除成功");
        }
    }


    /**
     * Delete filter
     *
     * @param input
     */
    public BloomfilterOutputModel detail(DetailApi.Input input) throws StatusCodeWithException {
        BloomFilterMySqlModel model = bloomFilterRepository.findById(input.getId()).orElse(null);
        if (model == null) {
            throw new StatusCodeWithException("数据不存在！", StatusCode.DATA_NOT_FOUND);
        }

        return ModelMapper.map(model,BloomfilterOutputModel.class);
    }
}
