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

package com.welab.wefe.union.service.api.dataresource;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataResourcePublicLevel;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.service.DataResourceContractService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * data resoure  hidden
 *
 * @author yuxin.zhang
 **/
@Api(path = "data_resource/hidden", name = "data_resource_hidden", rsaVerify = true, login = false)
public class HiddenApi extends AbstractApi<HiddenApi.Input, AbstractApiOutput> {
    @Autowired
    private DataResourceContractService dataResourceContractService;

    @Autowired
    protected DataResourceMongoReop dataResourceMongoReop;

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException, IOException {
        DataResource dataResource = dataResourceMongoReop.find(input.getDataResourceId(), input.curMemberId);
        if(dataResource != null) {
            dataResource.setPublicLevel(DataResourcePublicLevel.OnlyMyself.name());
            dataResourceContractService.update(dataResource);
        } else {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND,"资源不存在");
        }

        return success();
    }


    public static class Input extends BaseInput {
        @Check(require = true)
        private String dataResourceId;

        public String getDataResourceId() {
            return dataResourceId;
        }

        public void setDataResourceId(String dataResourceId) {
            this.dataResourceId = dataResourceId;
        }
    }

}
