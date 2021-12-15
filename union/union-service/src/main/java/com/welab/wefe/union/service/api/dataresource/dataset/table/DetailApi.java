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

package com.welab.wefe.union.service.api.dataresource.dataset.table;

import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.repo.TableDataSetMongoReop;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataset.image.ApiImageDataSetQueryOutput;
import com.welab.wefe.union.service.mapper.TableDataSetMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "table_data_set/detail", name = "table_data_set", rsaVerify = true, login = false)
public class DetailApi extends AbstractApi<DetailApi.Input, ApiImageDataSetQueryOutput> {

    @Autowired
    protected TableDataSetMongoReop tableDataSetMongoReop;

    protected TableDataSetMapper tableDataSetMapper = Mappers.getMapper(TableDataSetMapper.class);

    @Override
    protected ApiResult<ApiImageDataSetQueryOutput> handle(Input input) {
        return success();
    }



    public static class Input extends BaseInput {
        @Check(require = true)
        private String dataResouceId;

        public String getDataResouceId() {
            return dataResouceId;
        }

        public void setDataResouceId(String dataResouceId) {
            this.dataResouceId = dataResouceId;
        }
    }
}
