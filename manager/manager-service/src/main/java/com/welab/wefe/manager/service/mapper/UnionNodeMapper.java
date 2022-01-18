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

package com.welab.wefe.manager.service.mapper;

import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.manager.service.dto.union.UnionNodeAddInput;
import com.welab.wefe.manager.service.dto.union.UnionNodeQueryOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/27
 */
@Mapper
public interface UnionNodeMapper {
    /**
     * UnionNodeAddInput conversion UnionNode
     */
    @Mappings({
            @Mapping(target = "enable",expression = "java(String.valueOf(1))"),
            @Mapping(target = "lostContact",expression = "java(String.valueOf(0))"),
            @Mapping(target = "createdTime", expression = "java(com.welab.wefe.common.util.DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new java.util.Date()))"),
            @Mapping(target = "updatedTime", expression = "java(com.welab.wefe.common.util.DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new java.util.Date()))"),
    })
    UnionNode transferAddInput(UnionNodeAddInput input);


    /**
     * UnionNode conversion UnionNodeQueryOutput
     */
    @Mappings({
            @Mapping(target = "enable", expression = "java(Integer.parseInt(unionNode.getEnable()))"),
            @Mapping(target = "lostContact", expression = "java(Integer.parseInt(unionNode.getLostContact()))")
    })
    UnionNodeQueryOutput transfer(UnionNode unionNode);
}
