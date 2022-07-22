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
package com.webank.cert.mgr.db.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.CertInfo;
import com.welab.wefe.common.data.mongodb.entity.manager.CertKeyInfo;
import com.welab.wefe.common.data.mongodb.entity.manager.CertRequestInfo;
import com.welab.wefe.common.data.mongodb.repo.CertInfoRepo;
import com.welab.wefe.common.data.mongodb.repo.CertKeyInfoRepo;
import com.welab.wefe.common.data.mongodb.repo.CertRequestInfoRepo;

/**
 * @author wesleywang
 */
@Service
public class CertDao {

    @Autowired
    private CertInfoRepo certInfoRepo;
    @Autowired
    private CertKeyInfoRepo certKeyInfoRepo;
    @Autowired
    private CertRequestInfoRepo certRequestInfoRepo;

    public CertKeyInfo save(CertKeyInfo certKeyInfo) {
        certKeyInfoRepo.save(certKeyInfo);
        return certKeyInfo;
    }

    public CertInfo save(CertInfo certInfo) {
        certInfoRepo.save(certInfo);
        return certInfo;
    }

    public CertRequestInfo save(CertRequestInfo certRequestInfo) {
        certRequestInfoRepo.save(certRequestInfo);
        return certRequestInfo;
    }

    public CertKeyInfo findCertKeyById(String certKeyId) {
        return certKeyInfoRepo.findByPkId(certKeyId);
    }

    public CertRequestInfo findCertRequestById(String pkId) {
        return certRequestInfoRepo.findByPkId(pkId);
    }

    public CertRequestInfo findByPCertIdAndSubjectKeyId(String PCertId, String subjectKeyId) {
        return certRequestInfoRepo.findBypCertIdAndSubjectKeyId(PCertId, subjectKeyId);
    }

    public CertInfo findCertById(String certId) {
        return certInfoRepo.findByPkId(certId);
    }

    public void updateStatus(String serialNumber, String status) {
        certInfoRepo.updateStatus(serialNumber, status);
    }

    public CertInfo findBySerialNumber(String serialNumber) {
        return certInfoRepo.findBySerialNumber(serialNumber);
    }

    public List<CertInfo> findCertList(String userId, String pCertId, Boolean isCACert, Boolean isRootCert) {
        return certInfoRepo.findCerts(userId, pCertId, isCACert, isCACert);
    }

    public PageOutput<CertInfo> findCertList(String userId, String pCertId, Boolean isCACert, Boolean isRootCert,
            int pageIndex, int pageSize) {
        return certInfoRepo.findCertList(userId, pCertId, isCACert, isCACert, pageIndex, pageSize);
    }

    public PageOutput<CertRequestInfo> findCertRequestList(String userId, String pCertId, int pageIndex, int pageSize) {
        return certRequestInfoRepo.findCertRequestList(userId, pCertId, pageIndex, pageSize);
    }

    public PageOutput<CertKeyInfo> findKeys(String userId, int pageIndex, int pageSize) {
        return certKeyInfoRepo.findKeys(userId, pageIndex, pageSize);
    }

    public List<CertKeyInfo> findKeyByUserId(String userId) {
        return certKeyInfoRepo.findByUserId(userId);
    }

    public CertKeyInfo findKeyByPkId(String pkId) {
        return certKeyInfoRepo.findByPkId(pkId);
    }
}
