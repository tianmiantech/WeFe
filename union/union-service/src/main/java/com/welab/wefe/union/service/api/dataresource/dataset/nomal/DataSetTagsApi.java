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

package com.welab.wefe.union.service.api.dataresource.dataset.nomal;

import com.welab.wefe.common.data.mongodb.repo.AbstractDataSetMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.union.service.api.dataresource.dataset.AbstractDataSetTagsApi;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * data set tags query
 *
 * @author yuxin.zhang
 **/
@Api(path = "data_set/tags/query", name = "dataset_tags_query", rsaVerify = true, login = false)
public class DataSetTagsApi extends AbstractDataSetTagsApi {
    @Autowired
    protected DataSetMongoReop dataSetMongoReop;

    @Override
    protected AbstractDataSetMongoRepo getDataSetMongoRepo() {
        return dataSetMongoReop;
    }
}
