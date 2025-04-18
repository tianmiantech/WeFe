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

import com.welab.wefe.common.data.mongodb.entity.union.TrustCerts;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.manager.service.dto.cert.TrustCertsQueryOutput;
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
public interface TrustCertsMapper {
    /**
     * TrustCerts conversion TrustCertsQueryOutput
     */
    @Mappings({
            @Mapping(target = "caCert", expression = "java(\"1\".equals(trustCerts.getIsCaCert()) ? true : false)"),
            @Mapping(target = "rootCert", expression = "java(\"1\".equals(trustCerts.getIsRootCert()) ? true : false)"),
            @Mapping(source = "createdTime", target = "createdTime", dateFormat = DateUtil.YYYY_MM_DD_HH_MM_SS2),
            @Mapping(source = "updatedTime", target = "updatedTime", dateFormat = DateUtil.YYYY_MM_DD_HH_MM_SS2),
    })
    TrustCertsQueryOutput transfer(TrustCerts trustCerts);

}
