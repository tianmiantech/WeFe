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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataresource.TagsDTO;
import com.welab.wefe.union.service.service.BloomFilterContractService;
import com.welab.wefe.union.service.service.DataResourceContractService;
import com.welab.wefe.union.service.service.ImageDataSetContractService;
import com.welab.wefe.union.service.service.TableDataSetContractService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * data resoure tags query
 *
 * @author yuxin.zhang
 **/
@Api(path = "data_resoure/delete", name = "data_resoure_delete", rsaVerify = true, login = false)
public class DeleteApi extends AbstractApi<DeleteApi.Input, AbstractApiOutput> {
    @Autowired
    private DataResourceContractService dataResourceContractService;
    @Autowired
    private DataResourceMongoReop dataResourceMongoReop;
    @Autowired
    private ImageDataSetContractService imageDataSetContractService;
    @Autowired
    private TableDataSetContractService tableDataSetContractService;
    @Autowired
    private BloomFilterContractService bloomFilterContractService;


    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException, IOException {
        DataResource dataResource = dataResourceMongoReop.findByDataResourceId(input.dataResourceId);
        if(dataResource != null) {
            if(dataResource.getMemberId().equals(input.curMemberId)){
                switch (dataResource.getDataResourceType()) {
                    case BloomFilter:
                        bloomFilterContractService.delete(input.dataResourceId);
                        break;
                    case TableDataSet:
                        tableDataSetContractService.delete(input.dataResourceId);
                        break;
                    case ImageDataSet:
                        imageDataSetContractService.delete(input.dataResourceId);
                }
                dataResourceContractService.delete(input.dataResourceId);
            } else {
                throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
            }
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
