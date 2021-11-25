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

import com.welab.wefe.common.data.mongodb.dto.dataset.ImageDataSetQueryInput;
import com.welab.wefe.common.data.mongodb.dto.dataset.ImageDataSetQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.union.service.api.dataset.image.PutApi;
import com.welab.wefe.union.service.api.dataset.image.QueryApi;
import com.welab.wefe.union.service.dto.dataset.image.ApiImageDataSetQueryOutput;
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
    ApiImageDataSetQueryOutput transferDetail(ImageDataSet entity);


    @Mappings({
            @Mapping(source = "pageSize", target = "pageSize", defaultValue = "10"),
            @Mapping(source = "pageIndex", target = "pageIndex", defaultValue = "0"),
    })
    ImageDataSetQueryInput transferInput(QueryApi.Input entity);


    @Mappings({
            @Mapping(source = "createdTime", target = "createdTime", dateFormat = DateUtil.YYYY_MM_DD_HH_MM_SS2),
            @Mapping(source = "updatedTime", target = "updatedTime", dateFormat = DateUtil.YYYY_MM_DD_HH_MM_SS2),
    })
    ApiImageDataSetQueryOutput transferOutput(ImageDataSetQueryOutput entity);


    @Mappings({
            @Mapping(target = "enable", defaultValue = "1"),
            @Mapping(target = "labelCompleted", expression = "java(String.valueOf(input.isLabelCompleted() ? 1 : 0))"),
            @Mapping(target = "createdTime", expression = "java(com.welab.wefe.common.util.DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new java.util.Date()))"),
            @Mapping(target = "updatedTime", expression = "java(com.welab.wefe.common.util.DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new java.util.Date()))"),
    })
    ImageDataSet transferPutInput(PutApi.Input input);


}
