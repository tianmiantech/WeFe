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

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.dto.global_config.ServerCertInfoModel;
import com.welab.wefe.gateway.entity.CertInfoEntity;
import com.welab.wefe.gateway.entity.CertKeyInfoEntity;
import com.welab.wefe.gateway.entity.CertRequestInfoEntity;
import com.welab.wefe.gateway.repository.CertInfoRepository;
import com.welab.wefe.gateway.repository.CertKeyInfoRepository;
import com.welab.wefe.gateway.repository.CertRequestInfoRepository;
import com.welab.wefe.gateway.util.DatabaseEncryptUtil;

@Service
public class ServerCertService {

    @Autowired
    private CertInfoRepository certInfoRepository;
    @Autowired
    private CertKeyInfoRepository certKeyInfoRepository;
    @Autowired
    private CertRequestInfoRepository certRequestInfoRepository;

    public ServerCertInfoModel getCertInfo() throws StatusCodeWithException {
        Specification<CertInfoEntity> where = Where
                .create()
                .equal("status", "VALID")
                .orderBy("createdTime", OrderBy.desc)
                .build(CertInfoEntity.class);
        List<CertInfoEntity> certInfoEntityList = certInfoRepository.findAll(where);
        if (CollectionUtils.isEmpty(certInfoEntityList)) {
            return null;
        }

        CertRequestInfoEntity certRequestInfoEntity = certRequestInfoRepository.findById(certInfoEntityList.get(0).getCsrId()).orElse(null);
        if (null == certRequestInfoEntity) {
            return null;
        }
        CertKeyInfoEntity certKeyInfoEntity = certKeyInfoRepository.findById(certRequestInfoEntity.getSubjectKeyId()).orElse(null);
        if (null == certKeyInfoEntity) {
            return null;
        }

        ServerCertInfoModel model = new ServerCertInfoModel();
        model.setKey(DatabaseEncryptUtil.decrypt(certKeyInfoEntity.getKeyPem()));
        model.setContent(certInfoEntityList.get(0).getContent());
        return model;
    }
}
