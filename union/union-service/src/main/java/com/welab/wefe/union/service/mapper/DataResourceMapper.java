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

package com.welab.wefe.union.service.mapper;

import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryInput;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Object conversion
 * Define conversion rules,The implementation class is automatically generated when the project is built
 *
 * @author yuxin.zhang
 **/
@Mapper
public interface DataResourceMapper {

    @Mappings({
            @Mapping(target = "enable", expression = "java(String.valueOf(1))"),
            @Mapping(source = "dataResourceType", target = "dataResourceType", defaultExpression = "java(java.util.Arrays.stream(DataResourceType.values()).collect(java.util.stream.Collectors.toList()))"),
    })
    DataResourceQueryInput transferInput(ApiDataResourceQueryInput entity);


}
