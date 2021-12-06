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
