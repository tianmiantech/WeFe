package com.welab.wefe.manager.service.mapper;

import com.welab.wefe.common.data.mongodb.entity.union.RealnameAuthAgreementTemplate;
import com.welab.wefe.manager.service.dto.agreement.RealnameAuthAgreementTemplateOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/27
 */
@Mapper
public interface RealnameAuthAgreementTemplateMapper {


    @Mappings({
            @Mapping(target = "enable", expression = "java(Integer.parseInt(realnameAuthAgreementTemplate.getEnable()))")
    })
    RealnameAuthAgreementTemplateOutput transfer(RealnameAuthAgreementTemplate realnameAuthAgreementTemplate);
}
