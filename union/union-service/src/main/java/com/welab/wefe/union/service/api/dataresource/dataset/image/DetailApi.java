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

package com.welab.wefe.union.service.api.dataresource.dataset.image;

import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceDetailInput;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryInput;
import com.welab.wefe.union.service.dto.dataresource.dataset.image.ApiImageDataSetQueryOutput;
import com.welab.wefe.union.service.mapper.ImageDataSetMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "image_data_set/detail", name = "data_set_detail", rsaVerify = true, login = false)
public class DetailApi extends AbstractApi<ApiDataResourceDetailInput, ApiImageDataSetQueryOutput> {

    @Autowired
    protected ImageDataSetMongoReop imageDataSetMongoReop;

    protected ImageDataSetMapper mDataSetMapper = Mappers.getMapper(ImageDataSetMapper.class);

    @Override
    protected ApiResult<ApiImageDataSetQueryOutput> handle(ApiDataResourceDetailInput input) {
        DataResourceQueryOutput dataResourceQueryOutput = imageDataSetMongoReop.findCurMemberCanSee(input.getDataResourceId(), input.getCurMemberId());
        return success(getOutput(dataResourceQueryOutput));
    }

    protected ApiImageDataSetQueryOutput getOutput(DataResourceQueryOutput dataResourceQueryOutput) {
        if (dataResourceQueryOutput == null) {
            return null;
        }

        ApiImageDataSetQueryOutput detail = mDataSetMapper.transferDetail(dataResourceQueryOutput);
        return detail;
    }


}
