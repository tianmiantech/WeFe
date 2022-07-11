package com.webank.cert.mgr.handler;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.cert.mgr.db.dao.CertDao;
import com.webank.cert.mgr.enums.CertDigestAlgEnums;
import com.webank.cert.mgr.enums.KeyAlgorithmEnums;
import com.webank.cert.mgr.enums.MgrExceptionCodeEnums;
import com.webank.cert.mgr.exception.CertMgrException;
import com.webank.cert.toolkit.handler.X509CertHandler;
import com.webank.cert.toolkit.model.X500NameInfo;
import com.webank.cert.toolkit.service.CertService;
import com.webank.cert.toolkit.utils.CertUtils;
import com.webank.cert.toolkit.utils.KeyUtils;
import com.welab.wefe.common.data.mongodb.entity.manager.CertInfo;
import com.welab.wefe.common.data.mongodb.entity.manager.CertKeyInfo;
import com.welab.wefe.common.data.mongodb.entity.manager.CertRequestInfo;

/**
 * @author wesleywang
 */
@Service
public class CertHandler {

    protected static final Logger LOG = LoggerFactory.getLogger(CertHandler.class);

    @Autowired
    private CertService certService;
    @Autowired
    private CertDao certDao;

    public String importPrivateKey(String userId, String pemPrivateKey, String priAlg) throws Exception {
        if (StringUtils.isBlank(userId)) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_ACCOUNT_NOT_EXIST);
        }
        try {
            KeyUtils.getRSAKeyPair(pemPrivateKey);
        } catch (Exception e) {
            LOG.error("importPrivateKey failed, reason :", e);
            return "";
        }
        CertKeyInfo certKeyInfo = new CertKeyInfo();
        certKeyInfo.setKeyAlg(priAlg);
        certKeyInfo.setKeyPem(pemPrivateKey);
        certKeyInfo.setUserId(userId);
        certKeyInfo = certDao.save(certKeyInfo);
        return certKeyInfo.getPkId();
    }

    @Transactional
    public CertInfo createRootCert(String userId, String certKeyId, String pemPrivateKey, KeyAlgorithmEnums keyAlgorithm,
            X500NameInfo issuer, KeyUsage keyUsage, Date beginDate, Date endDate) throws Exception {
        if (StringUtils.isBlank(userId)) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_ACCOUNT_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(certKeyId) && pemPrivateKey == null) {
            CertKeyInfo certKeyInfo = certDao.findCertKeyById(certKeyId);
            pemPrivateKey = certKeyInfo.getKeyPem();
            keyAlgorithm = KeyAlgorithmEnums.getByKeyAlg(certKeyInfo.getKeyAlg());
        }
        CertDigestAlgEnums certDigestAlgEnums = getCertDigestAlg(keyAlgorithm);
        KeyPair keyPair = getKeyPair(keyAlgorithm, pemPrivateKey);

        X509Certificate certificate = certService.createRootCertificate(certDigestAlgEnums.getAlgorithmName(), issuer,
                keyUsage, beginDate, endDate, keyPair.getPublic(), keyPair.getPrivate());
        String certStr = CertUtils.readPEMAsString(certificate);

        return certDao.save(buildCertInfo(certStr, issuer.getCommonName(), issuer.getOrganizationName(),
                issuer.getCommonName(), issuer.getOrganizationName(), keyPair.getPublic(), userId,
                certificate.getSerialNumber(), certKeyId, certKeyId, true, ""));
    }

    @Transactional
    public CertRequestInfo createCertRequest(String userId, String certKeyId, String pemPrivateKey,
            KeyAlgorithmEnums keyAlgorithm, String parentCertId, X500NameInfo subject) throws Exception {
        if (StringUtils.isBlank(userId)) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_ACCOUNT_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(certKeyId) && pemPrivateKey == null) {
            CertKeyInfo certKeyInfo = certDao.findCertKeyById(certKeyId);
            pemPrivateKey = certKeyInfo.getKeyPem();
            keyAlgorithm = KeyAlgorithmEnums.getByKeyAlg(certKeyInfo.getKeyAlg());
        }
        CertDigestAlgEnums certDigestAlgEnums = getCertDigestAlg(keyAlgorithm);
        KeyPair keyPair = getKeyPair(keyAlgorithm, pemPrivateKey);

        PKCS10CertificationRequest request = certService.createCertRequest(subject, keyPair.getPublic(),
                keyPair.getPrivate(), certDigestAlgEnums.getAlgorithmName());
        String csrStr = CertUtils.readPEMAsString(request);

        CertInfo certInfo = certDao.findCertById(parentCertId);
        return certDao.save(buildCertRequestInfo(csrStr, subject.getCommonName(), subject.getOrganizationName(),
                parentCertId, userId, certKeyId, certInfo.getUserId()));
    }

    @Transactional
    public CertInfo createChildCert(String userId, String csrId, boolean isCaCert, KeyUsage keyUsage, Date beginDate,
            Date endDate) throws Exception {
        if (StringUtils.isBlank(userId)) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_ACCOUNT_NOT_EXIST);
        }
        CertRequestInfo requestInfo = certDao.findCertRequestById(csrId);
        if (requestInfo == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_REQUEST_NOT_EXIST);
        }
        CertInfo certInfo = certDao.findCertById(requestInfo.getpCertId());
        if (certInfo == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_NOT_EXIST);
        }
        CertKeyInfo keyInfo = certDao.findCertKeyById(certInfo.getIssuerKeyId());
        if (keyInfo == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_KEY_NOT_EXIST);
        }

        KeyAlgorithmEnums keyAlgorithm = KeyAlgorithmEnums.getByKeyAlg(keyInfo.getKeyAlg());
        CertDigestAlgEnums certDigestAlgEnums = getCertDigestAlg(keyAlgorithm);
        KeyPair keyPair = getKeyPair(keyAlgorithm, keyInfo.getKeyPem());

        X509Certificate parentCertificate = CertUtils.convertStrToCert(certInfo.getCertContent());
        try {
            parentCertificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_VALIDITY_FAILURE);
        }

        X509Certificate certificate = certService.createChildCertificate(isCaCert,
                certDigestAlgEnums.getAlgorithmName(), parentCertificate,
                CertUtils.convertStrToCsr(requestInfo.getCertRequestContent()), keyUsage, beginDate, endDate,
                keyPair.getPrivate());

        requestInfo.setIssue(true);
        certDao.save(requestInfo);
        return certDao.save(buildCertInfo(CertUtils.readPEMAsString(certificate), certInfo.getIssuerCN(),
                certInfo.getIssuerOrg(), requestInfo.getSubjectCN(), requestInfo.getSubjectOrg(),
                certificate.getPublicKey(), userId, certificate.getSerialNumber(), keyInfo.getPkId(),
                requestInfo.getSubjectKeyId(), isCaCert, certInfo.getPkId()));
    }

    @Transactional
    public CertInfo resetCertificate(String userId, String certId, KeyUsage keyUsage, Date beginDate, Date endDate)
            throws Exception {
        if (StringUtils.isBlank(userId)) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_ACCOUNT_NOT_EXIST);
        }
        CertInfo certInfo = certDao.findCertById(certId);
        if (certInfo == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_NOT_EXIST);
        }
        CertKeyInfo keyInfo = certDao.findCertKeyById(certInfo.getIssuerKeyId());
        if (keyInfo == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_KEY_NOT_EXIST);
        }

        X509Certificate certificate = CertUtils.convertStrToCert(certInfo.getCertContent());

        KeyAlgorithmEnums keyAlgorithm = KeyAlgorithmEnums.getByKeyAlg(keyInfo.getKeyAlg());
        CertDigestAlgEnums certDigestAlgEnums = getCertDigestAlg(keyAlgorithm);
        KeyPair keyPair = getKeyPair(keyAlgorithm, keyInfo.getKeyPem());

        X509Certificate reCert = null;
        if (certInfo.getSubjectKeyId().equals(certInfo.getIssuerKeyId())) {
            reCert = X509CertHandler.createRootCert(certDigestAlgEnums.getAlgorithmName(),
                    X500Name.getInstance(certificate.getSubjectX500Principal().getEncoded()), null, beginDate, endDate,
                    certificate.getPublicKey(), keyPair.getPrivate());
        } else {
            CertInfo parentCertInfo = certDao.findCertById(certInfo.getpCertId());
            if (parentCertInfo == null) {
                throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_NOT_EXIST);
            }

            CertRequestInfo requestInfo = certDao.findByPCertIdAndSubjectKeyId(certInfo.getpCertId(),
                    certInfo.getSubjectKeyId());
            if (requestInfo == null) {
                throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_REQUEST_NOT_EXIST);
            }

            X509Certificate parentCert = CertUtils.convertStrToCert(parentCertInfo.getCertContent());
            reCert = X509CertHandler.createChildCert(certInfo.getIsCACert(), certDigestAlgEnums.getAlgorithmName(),
                    parentCert, CertUtils.convertStrToCsr(requestInfo.getCertRequestContent()), keyUsage, beginDate,
                    endDate, keyPair.getPrivate());
        }

        String reCertStr = CertUtils.readPEMAsString(reCert);
        certInfo.setUserId(userId);
        certInfo.setCertContent(reCertStr);
        return certDao.save(certInfo);
    }

    public List<CertInfo> queryCertInfoList(String userId, String issuerKeyId, String pCertId, String issuerOrg,
            String issuerCN, Boolean isCACert) {

        return certDao.findCertList(userId, issuerKeyId, pCertId, issuerOrg, issuerCN, isCACert);
    }

    public List<CertRequestInfo> queryCertRequestList(String userId, String subjectKeyId, String pCertId,
            String subjectOrg, String subjectCN, String pCertUserId) {
        return certDao.findCertRequestList(userId, subjectKeyId, pCertId, subjectOrg, subjectCN, pCertUserId);
    }

    public List<CertKeyInfo> queryCertKeyList(String userId) {
        return certDao.findKeyByUserId(userId);
    }

    public CertInfo queryCertInfoByCertId(String certId) {
        return certDao.findCertById(certId);
    }

    public CertRequestInfo queryCertRequestByCsrId(String csrId) {
        return certDao.findCertRequestById(csrId);
    }

    private CertDigestAlgEnums getCertDigestAlg(KeyAlgorithmEnums keyAlgorithm) throws CertMgrException {
        if (keyAlgorithm == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_KEY_ALG_NOT_EXIST);
        }
        CertDigestAlgEnums certDigestAlgEnums = CertDigestAlgEnums.getByKeyAlg(keyAlgorithm.getKeyAlgorithm());
        if (certDigestAlgEnums == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_KEY_ALG_NOT_EXIST);
        }
        return certDigestAlgEnums;
    }

    private KeyPair getKeyPair(KeyAlgorithmEnums keyAlgorithm, String pemPrivateKey) throws Exception {
        KeyPair keyPair = null;
        if (keyAlgorithm.equals(KeyAlgorithmEnums.ECDSA) || keyAlgorithm.equals(KeyAlgorithmEnums.SM2)) {
            keyPair = KeyUtils.getECKeyPair(pemPrivateKey);
        }
        if (keyAlgorithm.equals(KeyAlgorithmEnums.RSA)) {
            keyPair = KeyUtils.getRSAKeyPair(pemPrivateKey);
        }
        if (keyPair == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_KEY_ALG_NOT_EXIST);
        }
        return keyPair;
    }

    private CertInfo buildCertInfo(String certificate, String issuerCommonName, String issuerOrgName,
            String subjectCommonName, String subjectOrgName, PublicKey publicKey, String userId,
            BigInteger serialNumber, String certKeyId, String subjectKeyId, boolean isCACert, String issuerCertId) {
        CertInfo certInfo = new CertInfo();
        certInfo.setUserId(userId);
        certInfo.setIssuerKeyId(certKeyId);
        certInfo.setSubjectKeyId(subjectKeyId);
        certInfo.setCertContent(certificate);
        certInfo.setIssuerCN(issuerCommonName);
        certInfo.setIssuerOrg(issuerOrgName);
        certInfo.setSubjectCN(subjectCommonName);
        certInfo.setSubjectOrg(subjectOrgName);
        certInfo.setSubjectPubKey(CertUtils.readPEMAsString(publicKey));
        certInfo.setSerialNumber(String.valueOf(serialNumber));
        certInfo.setIsCACert(isCACert);
        certInfo.setpCertId(issuerCertId);
        return certInfo;
    }

    private CertRequestInfo buildCertRequestInfo(String csrStr, String commonName, String organizationName,
            String parentCertId, String userId, String certKeyId, String pCertUserId) {
        CertRequestInfo certRequestInfo = new CertRequestInfo();
        certRequestInfo.setUserId(userId);
        certRequestInfo.setpCertId(parentCertId);
        certRequestInfo.setSubjectKeyId(certKeyId);
        certRequestInfo.setSubjectCN(commonName);
        certRequestInfo.setSubjectOrg(organizationName);
        certRequestInfo.setCertRequestContent(csrStr);
        certRequestInfo.setpCertUserId(pCertUserId);
        certRequestInfo.setIssue(false);
        return certRequestInfo;
    }

}
