package com.webank.cert.mgr.db.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<CertInfo> findCertList(String userId, String issuerKeyId, String pCertId, String issuerOrg,
            String issuerCN, Boolean isCACert) {
        return certInfoRepo.findCerts(userId, issuerKeyId, pCertId, issuerOrg, issuerCN, isCACert);
    }

    public List<CertRequestInfo> findCertRequestList(String userId, String subjectKeyId, String pCertId,
            String subjectOrg, String subjectCN, String pCertUserId) {
        return certRequestInfoRepo.findCertRequestList(userId, subjectKeyId, pCertId, subjectOrg, subjectCN,
                pCertUserId);
    }

    public List<CertKeyInfo> findKeyByUserId(String userId) {
        return certKeyInfoRepo.findByUserId(userId);
    }

    public CertKeyInfo findByPkId(String pkId) {
        return certKeyInfoRepo.findByPkId(pkId);
    }
}
