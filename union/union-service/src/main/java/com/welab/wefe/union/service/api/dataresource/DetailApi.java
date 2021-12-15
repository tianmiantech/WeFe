/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.union.service.api.dataresource;

import com.welab.wefe.common.data.mongodb.entity.union.DataSet;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataresource.dataset.table.DataSetDetailOutput;
import com.welab.wefe.union.service.mapper.DataSetMapper;
import com.welab.wefe.union.service.service.DataSetContractService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Jervis
 **/
@Api(path = "data_set/detail", name = "data_set_detail", rsaVerify = true, login = false)
public class DetailApi extends AbstractApi<DetailApi.Input, DataSetDetailOutput> {

    @Autowired
    protected DataSetMongoReop dataSetMongoReop;
    @Autowired
    protected DataSetContractService mDataSetContractService;

    protected DataSetMapper mDataSetMapper = Mappers.getMapper(DataSetMapper.class);

    @Override
    protected ApiResult<DataSetDetailOutput> handle(Input input) {
        DataSet dataSet = dataSetMongoReop.findDataSetId(input.getId());
        return success(getOutput(dataSet));
    }

    protected DataSetDetailOutput getOutput(DataSet dataSet) {
        if (dataSet == null) {
            return null;
        }

        DataSetDetailOutput detail = mDataSetMapper.transferDetail(dataSet);
        return detail;
    }

    public static class Input extends BaseInput {
        @Check(require = true)
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
