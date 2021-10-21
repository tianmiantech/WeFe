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

package com.welab.wefe.manager.service.api.dataset;

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.dataset.ApiDataSetQueryInput;
import com.welab.wefe.manager.service.dto.dataset.ApiDataSetQueryOutput;
import com.welab.wefe.manager.service.mapper.DataSetMapper;
import com.welab.wefe.manager.service.service.DataSetContractService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 */
@Api(path = "data_set/query", name = "data_set_query",login = false)
public class QueryApi extends AbstractApi<ApiDataSetQueryInput, PageOutput<ApiDataSetQueryOutput>> {
    @Autowired
    protected DataSetMongoReop dataSetMongoReop;
    @Autowired
    protected DataSetContractService mDatasetContractService;
    protected DataSetMapper mDataSetMapper = Mappers.getMapper(DataSetMapper.class);

    @Override
    protected ApiResult<PageOutput<ApiDataSetQueryOutput>> handle(ApiDataSetQueryInput input) {
        PageOutput<DataSetQueryOutput> pageOutput = dataSetMongoReop.find(mDataSetMapper.transferInput(input));

        List<ApiDataSetQueryOutput> list = pageOutput.getList().stream()
                .map(mDataSetMapper::transferOutput)
                .collect(Collectors.toList());

        return success(new PageOutput<>(
                pageOutput.getPageIndex(),
                pageOutput.getTotal(),
                pageOutput.getPageSize(),
                pageOutput.getTotalPage(),
                list
        ));
    }

}
