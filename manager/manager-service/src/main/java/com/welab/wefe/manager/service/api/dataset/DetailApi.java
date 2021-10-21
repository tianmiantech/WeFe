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

import com.welab.wefe.common.data.mongodb.entity.union.DataSet;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.dataset.ApiDataSetQueryOutput;
import com.welab.wefe.manager.service.dto.dataset.DataSetDetailInput;
import com.welab.wefe.manager.service.mapper.DataSetMapper;
import com.welab.wefe.manager.service.service.DataSetContractService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "data_set/detail", name = "data_set_detail",login = false)
public class DetailApi extends AbstractApi<DataSetDetailInput, ApiDataSetQueryOutput> {

    @Autowired
    protected DataSetMongoReop dataSetMongoReop;
    @Autowired
    protected DataSetContractService mDataSetContractService;

    protected DataSetMapper mDataSetMapper = Mappers.getMapper(DataSetMapper.class);

    @Override
    protected ApiResult<ApiDataSetQueryOutput> handle(DataSetDetailInput input) {
        DataSet dataSet = dataSetMongoReop.findDataSetId(input.getId());
        return success(getOutput(dataSet));
    }

    protected ApiDataSetQueryOutput getOutput(DataSet dataSet) {
        if (dataSet == null) {
            return null;
        }

        ApiDataSetQueryOutput detail = mDataSetMapper.transferDetail(dataSet);
        return detail;
    }

}
