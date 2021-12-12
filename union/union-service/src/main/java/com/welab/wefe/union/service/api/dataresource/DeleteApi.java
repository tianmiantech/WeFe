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

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataset.DataSetOutput;
import com.welab.wefe.union.service.service.DataSetContractService;
import com.welab.wefe.union.service.service.DataSetMemberPermissionContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "data_set/delete", name = "data_set_delete", rsaVerify = true, login = false)
public class DeleteApi extends AbstractApi<DeleteApi.Input, DataSetOutput> {
    @Autowired
    protected DataSetContractService datasetContractService;

    @Autowired
    private DataSetMemberPermissionContractService mDataSetMemberPermissionContractService;

    @Override
    protected ApiResult<DataSetOutput> handle(Input input) throws StatusCodeWithException {
        datasetContractService.deleteById(input.getId());
        mDataSetMemberPermissionContractService.deleteByDataSetId(input.getId());
        return success();
    }

    public static class Input extends BaseInput {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
