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

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.dataset.DataSetExtJsonUpdateInput;
import com.welab.wefe.manager.service.dto.dataset.DataSetExtJsonUpdateOutput;
import com.welab.wefe.manager.service.service.DataSetContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "data_set/update_ext_json", name = "update_ext_json",login = false)
public class UpdateExtJsonApi extends AbstractApi<DataSetExtJsonUpdateInput, DataSetExtJsonUpdateOutput> {
    @Autowired
    protected DataSetContractService mDataSetContractService;

    @Override
    protected ApiResult<DataSetExtJsonUpdateOutput> handle(DataSetExtJsonUpdateInput input) throws StatusCodeWithException {
        mDataSetContractService.updateExtJson(input);
        return success();
    }

}
