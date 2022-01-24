/*
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

package com.welab.wefe.manager.service.api.defaulttag;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.tag.DatSetDefaultTagDeleteInput;
import com.welab.wefe.manager.service.service.DatSetDefaultTagContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "default_tag/delete", name = "default_tag_delete")
public class DeleteApi extends AbstractApi<DatSetDefaultTagDeleteInput, AbstractApiOutput> {

    @Autowired
    private DatSetDefaultTagContractService datSetDefaultTagContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(DatSetDefaultTagDeleteInput input) throws StatusCodeWithException {
        LOG.info("DeleteApi handle..");
        try {

            datSetDefaultTagContractService.deleteByTagId(input.getTagId());
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}
