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
