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

package com.welab.wefe.union.service.mapper;

import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.dto.dataset.ImageDataSetQueryInput;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.union.service.api.dataresource.dataset.image.PutApi;
import com.welab.wefe.union.service.api.dataresource.dataset.image.QueryApi;
import com.welab.wefe.union.service.dto.dataresource.DataResourcePutInput;
import com.welab.wefe.union.service.dto.dataresource.dataset.image.ApiImageDataSetQueryOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Object conversion
 * Define conversion rules,The implementation class is automatically generated when the project is built
 *
 * @author yuxin.zhang
 **/
@Mapper
public interface ImageDataSetMapper {


    @Mappings({
            @Mapping(source = "createdTime", target = "createdTime", dateFormat = DateUtil.YYYY_MM_DD_HH_MM_SS2),
            @Mapping(source = "updatedTime", target = "updatedTime", dateFormat = DateUtil.YYYY_MM_DD_HH_MM_SS2),
    })
    ApiImageDataSetQueryOutput transferDetail(DataResourceQueryOutput entity);

    @Mappings({
            @Mapping(target = "enable", expression = "java(String.valueOf(0))"),
    })
    ImageDataSetQueryInput transferInput(QueryApi.QueryInput entity);


    @Mappings({
            @Mapping(target = "labelCompleted", expression = "java(String.valueOf(input.isLabelCompleted() ? 1 : 0))"),
            @Mapping(target = "createdTime", expression = "java(com.welab.wefe.common.util.DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new java.util.Date()))"),
            @Mapping(target = "updatedTime", expression = "java(com.welab.wefe.common.util.DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new java.util.Date()))"),
    })
    ImageDataSet transferPutInputToImageDataSet(PutApi.Input input);


    DataResource transferPutInputToDataResource(PutApi.Input input);


    DataResourcePutInput transferPutInputToDataResourcePutInput(PutApi.Input input);


}
