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

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryInput;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.enums.DeepLearningJobType;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryInput;
import com.welab.wefe.union.service.dto.dataresource.dataset.image.ApiImageDataSetQueryOutput;
import com.welab.wefe.union.service.mapper.ImageDataSetMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 **/
@Api(path = "image_data_set/query", name = "image_data_set_query", rsaVerify = true, login = false)
public class QueryApi extends AbstractApi<QueryApi.QueryInput, PageOutput<ApiImageDataSetQueryOutput>> {
    @Autowired
    protected ImageDataSetMongoReop imageDataSetMongoReop;

    protected ImageDataSetMapper imageDataSetMapper = Mappers.getMapper(ImageDataSetMapper.class);

    @Override
    protected ApiResult<PageOutput<ApiImageDataSetQueryOutput>> handle(QueryInput input) {
        PageOutput<DataResourceQueryOutput> pageOutput = imageDataSetMongoReop.findCurMemberCanSee(imageDataSetMapper.transferInput(input));

        List<ApiImageDataSetQueryOutput> list = pageOutput.getList().stream()
                .map(imageDataSetMapper::transferDetail)
                .collect(Collectors.toList());

        return success(new PageOutput<>(
                pageOutput.getPageIndex(),
                pageOutput.getTotal(),
                pageOutput.getPageSize(),
                pageOutput.getTotalPage(),
                list
        ));
    }

    public static class QueryInput extends ApiDataResourceQueryInput {
        private DeepLearningJobType forJobType;

        public DeepLearningJobType getForJobType() {
            return forJobType;
        }

        public void setForJobType(DeepLearningJobType forJobType) {
            this.forJobType = forJobType;
        }
    }
}
