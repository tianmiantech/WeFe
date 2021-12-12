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

package com.welab.wefe.union.service.api.dataresource;

import com.welab.wefe.common.data.mongodb.repo.AbstractDataSetMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.api.dataresource.dataset.AbstractDataSetTagsApi;
import com.welab.wefe.union.service.dto.dataset.ApiTagsQueryOutput;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * data resoure tags query
 *
 * @author yuxin.zhang
 **/
@Api(path = "data_resoure/tags/query", name = "resoure_tags_query", rsaVerify = true, login = false)
public class DataSetTagsApi extends AbstractApi<AbstractDataSetTagsApi.Input, ApiTagsQueryOutput> {
    @Autowired
    protected DataSetMongoReop dataSetMongoReop;


    @Override
    protected ApiResult<ApiTagsQueryOutput> handle(AbstractDataSetTagsApi.Input input) throws StatusCodeWithException, IOException {
        return null;
    }
}
