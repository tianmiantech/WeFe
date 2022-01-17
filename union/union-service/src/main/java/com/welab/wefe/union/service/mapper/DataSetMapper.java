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

package com.welab.wefe.union.service.mapper;

import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryInput;
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.DataSet;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.union.service.api.dataset.QueryApi;
import com.welab.wefe.union.service.dto.dataset.ApiDataSetQueryOutput;
import com.welab.wefe.union.service.dto.dataset.DataSetDetailOutput;
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
public interface DataSetMapper {

    /**
     * DataSet conversion DataSetDetailOutput
     */
    @Mappings({
            @Mapping(source = "dataSetId", target = "id"),
            @Mapping(source = "createdTime", target = "createdTime", dateFormat = DateUtil.YYYY_MM_DD_HH_MM_SS2),
            @Mapping(source = "updatedTime", target = "updatedTime", dateFormat = DateUtil.YYYY_MM_DD_HH_MM_SS2),
    })
    DataSetDetailOutput transferDetail(DataSet entity);

    /**
     * QueryApi.Input conversion DataSetQueryInput
     */
    @Mappings({
            @Mapping(source = "id", target = "dataSetId"),
            @Mapping(source = "pageSize", target = "pageSize", defaultValue = "10"),
            @Mapping(source = "pageIndex", target = "pageIndex", defaultValue = "0"),
    })
    DataSetQueryInput transferInput(QueryApi.Input entity);

    /**
     * DataSetQueryOutput conversion ApiDataSetQueryOutput
     */
    @Mappings({
            @Mapping(source = "dataSetId", target = "id"),
            @Mapping(target = "usageCountInJob",expression = "java(com.welab.wefe.common.util.StringUtil.isEmpty(entity.getUsageCountInJob()) ? 0 : Integer.parseInt(entity.getUsageCountInJob()))"),
            @Mapping(target = "usageCountInFlow",expression = "java(com.welab.wefe.common.util.StringUtil.isEmpty(entity.getUsageCountInFlow()) ? 0 : Integer.parseInt(entity.getUsageCountInFlow()))"),
            @Mapping(target = "usageCountInProject",expression = "java(com.welab.wefe.common.util.StringUtil.isEmpty(entity.getUsageCountInProject()) ? 0 : Integer.parseInt(entity.getUsageCountInProject()))"),
            @Mapping(source = "createdTime", target = "createdTime", dateFormat = DateUtil.YYYY_MM_DD_HH_MM_SS2),
            @Mapping(source = "updatedTime", target = "updatedTime", dateFormat = DateUtil.YYYY_MM_DD_HH_MM_SS2),
    })
    ApiDataSetQueryOutput transferOutput(DataSetQueryOutput entity);

}
