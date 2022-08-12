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

package com.welab.wefe.gateway.service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.gateway.dto.ServerCertInfoModel;
import com.welab.wefe.gateway.entity.CertInfoEntity;
import com.welab.wefe.gateway.repository.CertInfoRepository;
import com.welab.wefe.gateway.repository.CertKeyInfoRepository;
import com.welab.wefe.gateway.repository.CertRequestInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerCertService {

    @Autowired
    private CertInfoRepository certInfoRepository;
    @Autowired
    private CertKeyInfoRepository certKeyInfoRepository;
    @Autowired
    private CertRequestInfoRepository certRequestInfoRepository;

    public ServerCertInfoModel getCertInfo() {
        Specification<CertInfoEntity> where = Where
                .create()
                .equal("status", "VALID")
                .orderBy("createTime", OrderBy.desc)
                .build(CertInfoEntity.class);
        List<CertInfoEntity> certInfoEntityList = certInfoRepository.findAll(where);
        return null;
    }
}
