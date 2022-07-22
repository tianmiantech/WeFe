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

@Service
public class CertHandler {

    protected static final Logger LOG = LoggerFactory.getLogger(CertHandler.class);

    @Autowired
    private CertService certService;
    @Autowired
    private CertDao certDao;

    /**
     * 用户ID
     * 
     * 根证书私钥ID / 根证书私钥pem内容
     * 
     * 根证书私钥算法
     * 
     * 签发机构信息
     * 
     * 证书使用用途
     * 
     * 证书有效期
     * 
     * @throws Exception
     */
    public CertInfo createRootCert(String userId, String certKeyId, String pemPrivateKey,
            KeyAlgorithmEnums keyAlgorithm, X500NameInfo issuer, KeyUsage keyUsage, Date beginDate, Date endDate)
            throws Exception {
        if (StringUtils.isBlank(userId)) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_ACCOUNT_NOT_EXIST);
        }
        // 如果根证书私钥ID不为空
        if (StringUtils.isNotBlank(certKeyId) && pemPrivateKey == null) {
            // 私钥信息
            CertKeyInfo certKeyInfo = certDao.findCertKeyById(certKeyId);
            // 私钥pem内容
            pemPrivateKey = certKeyInfo.getKeyPem();
            // 私钥算法
            keyAlgorithm = KeyAlgorithmEnums.getByKeyAlg(certKeyInfo.getKeyAlg());
        }
        CertDigestAlgEnums certDigestAlgEnums = getCertDigestAlg(keyAlgorithm);
        // 获取公私钥对
        KeyPair keyPair = getKeyPair(keyAlgorithm, pemPrivateKey);
        // 生成根证书
        X509Certificate certificate = certService.createRootCertificate(certDigestAlgEnums.getAlgorithmName(), issuer,
                keyUsage, beginDate, endDate, keyPair.getPublic(), keyPair.getPrivate());
        // 证书转为pem格式
        String certificatePemStr = CertUtils.readPEMAsString(certificate);
        return certDao.save(buildCertInfo(certificatePemStr, issuer.getCommonName(), issuer.getOrganizationName(),
                issuer.getCommonName(), issuer.getOrganizationName(), keyPair.getPublic(), userId,
                certificate.getSerialNumber(), certKeyId, certKeyId, true, ""));
    }

    /**
     * 私钥算法
     */
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

    public CertKeyInfo queryCertKey(String pkId) {
        return certDao.findByPkId(pkId);
    }

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

    /**
     * @param commonName 证书常用名
     * @param userId     证书申请人用户ID
     * @param csrId      证书申请ID
     * @param isCaCert   是否是根证书
     */
    @Transactional
    public CertInfo createChildCert(String commonName, String userId, String csrId, boolean isCaCert, KeyUsage keyUsage,
            Date beginDate, Date endDate) throws Exception {
        if (StringUtils.isBlank(userId)) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_ACCOUNT_NOT_EXIST);
        }
        CertRequestInfo subjectRequestInfo = certDao.findCertRequestById(csrId);
        if (subjectRequestInfo == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_REQUEST_NOT_EXIST);
        }
        // 签发机构的证书
        CertInfo issuerCertInfo = certDao.findCertById(subjectRequestInfo.getIssuerCertId());
        if (issuerCertInfo == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_NOT_EXIST);
        }
        // 签发机构的证书对应的私钥ID
        CertKeyInfo issuerKeyInfo = certDao.findCertKeyById(issuerCertInfo.getSubjectKeyId());
        if (issuerKeyInfo == null) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_KEY_NOT_EXIST);
        }

        KeyAlgorithmEnums issuerKeyAlgorithm = KeyAlgorithmEnums.getByKeyAlg(issuerKeyInfo.getKeyAlg());
        CertDigestAlgEnums issuerCertDigestAlgEnums = getCertDigestAlg(issuerKeyAlgorithm);
        KeyPair issuerKeyPair = getKeyPair(issuerKeyAlgorithm, issuerKeyInfo.getKeyPem());

        X509Certificate parentCertificate = CertUtils.convertStrToCert(issuerCertInfo.getCertContent());
        try {
            parentCertificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_VALIDITY_FAILURE);
        }

        X509Certificate certificate = certService.createChildCertificate(isCaCert,
                issuerCertDigestAlgEnums.getAlgorithmName(), parentCertificate,
                CertUtils.convertStrToCsr(subjectRequestInfo.getCertRequestContent()), keyUsage, beginDate, endDate,
                issuerKeyPair.getPrivate());
        subjectRequestInfo.setIssue(true);
        certDao.save(subjectRequestInfo);
        return certDao.save(buildCertInfo(CertUtils.readPEMAsString(certificate), issuerCertInfo.getSubjectCN(),
                issuerCertInfo.getSubjectOrg(), subjectRequestInfo.getSubjectCN(), subjectRequestInfo.getSubjectOrg(),
                certificate.getPublicKey(), userId, certificate.getSerialNumber(), issuerKeyInfo.getPkId(),
                subjectRequestInfo.getSubjectKeyId(), isCaCert, issuerCertInfo.getPkId()));
    }

    @Transactional
    public CertRequestInfo createCertRequest(String commonName, String userId, String certKeyId, String pemPrivateKey,
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

    public List<CertInfo> queryCertInfoList(String userId, String pCertId, Boolean isCACert, Boolean isRootCert) {
        return certDao.findCertList(userId, pCertId, isCACert, isRootCert);
    }

    public List<CertRequestInfo> queryCertRequestList(String userId, String pCertId) {
        return certDao.findCertRequestList(userId, pCertId);
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

    /**
     * @param certificatePemStr 证书的pem格式内容
     * @param issuerCommonName  签发机构的常用名
     * @param issuerOrgName     签发机构的组织机构名称
     * @param subjectCommonName 证书申请人的常用名
     * @param subjectOrgName    证书申请人的组织名称
     * @param publicKey         证书的公钥
     * @param userId            证书申请人用户ID
     * @param serialNumber      证书序列号
     * @param issuerCertKeyId   签发机构的证书的私钥ID
     * @param subjectKeyId      证书申请人私钥ID
     * @param isCACert          是否是根证书
     * @param issuerCertId      签发机构证书ID
     */
    private CertInfo buildCertInfo(String certificatePemStr, String issuerCommonName, String issuerOrgName,
            String subjectCommonName, String subjectOrgName, PublicKey publicKey, String userId,
            BigInteger serialNumber, String issuerCertKeyId, String subjectKeyId, boolean isCACert,
            String issuerCertId) {
        CertInfo certInfo = new CertInfo();
        certInfo.setUserId(userId);

//        certInfo.setIssuerKeyId(issuerCertKeyId);// 根证书需要？？？？
        certInfo.setIssuerCN(issuerCommonName);
        certInfo.setIssuerOrg(issuerOrgName);
        certInfo.setpCertId(issuerCertId); // optional 根证书为空

        certInfo.setSubjectKeyId(subjectKeyId); // optional 非根证书为空
        certInfo.setSubjectCN(subjectCommonName);
        certInfo.setSubjectOrg(subjectOrgName);
        certInfo.setSubjectPubKey(CertUtils.readPEMAsString(publicKey));

        certInfo.setCertContent(certificatePemStr);
        certInfo.setSerialNumber(String.valueOf(serialNumber)); // optional
        certInfo.setIsCACert(isCACert);

        return certInfo;
    }

    private CertRequestInfo buildCertRequestInfo(String csrStr, String commonName, String organizationName,
            String parentCertId, String userId, String certKeyId, String pCertUserId) {
        CertRequestInfo certRequestInfo = new CertRequestInfo();
        certRequestInfo.setUserId(userId);
        certRequestInfo.setIssuerCertId(parentCertId);
        certRequestInfo.setSubjectKeyId(certKeyId);
        certRequestInfo.setSubjectCN(commonName);
        certRequestInfo.setSubjectOrg(organizationName);
        certRequestInfo.setCertRequestContent(csrStr);
        certRequestInfo.setIssuerCertUserId(pCertUserId);
        certRequestInfo.setIssue(false);
        return certRequestInfo;
    }
}
