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

package com.welab.wefe.union.service.api.dataresource.bloomfilter;

import com.welab.wefe.common.data.mongodb.repo.BloomFilterMongoReop;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataresource.dataset.image.ApiImageDataSetQueryOutput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "bloom_filter/detail", name = "bloom_filter_detail", rsaVerify = true, login = false)
public class DetailApi extends AbstractApi<DetailApi.Input, ApiImageDataSetQueryOutput> {

    @Autowired
    protected BloomFilterMongoReop bloomFilterMongoReop;


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
