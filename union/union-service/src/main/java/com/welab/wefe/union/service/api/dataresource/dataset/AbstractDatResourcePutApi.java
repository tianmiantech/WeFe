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

package com.welab.wefe.union.service.api.dataresource.dataset;

import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.union.service.dto.dataresource.DataResourcePutInput;
import com.welab.wefe.union.service.service.DataResourceContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
public abstract class AbstractDatResourcePutApi<In extends AbstractApiInput, Out> extends AbstractApi<In, Out> {

    @Autowired
    protected DataResourceContractService dataResourceContractService;
    @Autowired
    protected DataResourceMongoReop dataResourceMongoReop;


    protected void updateDataResource(DataResource dataResource, DataResourcePutInput input) throws StatusCodeWithException {
        dataResource.setName(input.getName());
        dataResource.setDescription(input.getDescription());
        dataResource.setTags(input.getTags());
        dataResource.setTotalDataCount(String.valueOf(input.getTotalDataCount()));
        dataResource.setPublicLevel(input.getPublicLevel());
        dataResource.setPublicMemberList(input.getPublicMemberList());
        dataResource.setUsageCountInMember(String.valueOf(input.getUsageCountInMember()));
        dataResource.setUsageCountInProject(String.valueOf(input.getUsageCountInProject()));
        dataResource.setUsageCountInFlow(String.valueOf(input.getUsageCountInFlow()));
        dataResource.setUsageCountInJob(String.valueOf(input.getUsageCountInJob()));
        dataResourceContractService.update(dataResource);
    }

}
