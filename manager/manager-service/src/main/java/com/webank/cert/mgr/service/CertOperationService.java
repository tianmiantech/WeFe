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
package com.webank.cert.mgr.service;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import com.welab.wefe.common.web.util.CurrentAccountUtil;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.cert.mgr.db.dao.CertDao;
import com.webank.cert.mgr.enums.MgrExceptionCodeEnums;
import com.webank.cert.mgr.exception.CertMgrException;
import com.webank.cert.mgr.model.vo.CertKeyVO;
import com.webank.cert.mgr.model.vo.CertRequestVO;
import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.utils.TransformUtils;
import com.webank.cert.toolkit.constants.CertConstants;
import com.webank.cert.toolkit.enums.CertDigestAlgEnums;
import com.webank.cert.toolkit.enums.CertStatusEnums;
import com.webank.cert.toolkit.enums.KeyAlgorithmEnums;
import com.webank.cert.toolkit.model.X500NameInfo;
import com.webank.cert.toolkit.service.CertService;
import com.webank.cert.toolkit.utils.CertUtils;
import com.webank.cert.toolkit.utils.KeyUtils;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.CertInfo;
import com.welab.wefe.common.data.mongodb.entity.manager.CertKeyInfo;
import com.welab.wefe.common.data.mongodb.entity.manager.CertRequestInfo;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;

@Service
public class CertOperationService {

    protected static final Logger LOG = LoggerFactory.getLogger(CertOperationService.class);

    @Autowired
    private CertService certService;
    @Autowired
    private CertDao certDao;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // 更新证书状态
    public void updateStatusBySerialNumber(String serialNumber, int status, String reason) {
        if(StringUtils.isBlank(serialNumber)) {
            return;
        }
        certDao.updateStatusBySerialNumber(serialNumber, status, reason);
    }

    // 更新证书状态
    public void updateStatusByUserId(String userId, int status, String reason) {
        if(StringUtils.isBlank(userId)) {
            return;
        }
        certDao.updateStatusByUserId(userId, status, reason);
    }

    // 更新证书信任状态
    public void updateCanTrust(String serialNumber, boolean status) {
        if(StringUtils.isBlank(serialNumber)) {
            return;
        }
        certDao.updateCanTrust(serialNumber, status);
    }

    // 导出证书到文件
    public void exportCertToDerFile(String certId, String filePath) throws Exception {
        CertVO certVO = queryCertInfoByCertId(certId);
        if (certVO != null && !StringUtils.isEmpty(certVO.getCertContent())) {
            CertUtils.writeCrt(CertUtils.convertStrToCert(certVO.getCertContent()), filePath);
        } else {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_NOT_EXIST);
        }
    }

    // 根据id查询证书
    public CertVO queryCertInfoByCertId(String certId) {
        CertInfo certInfo = certDao.findCertById(certId);
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);
    }

    // 根据ID查询证书请求
    public CertRequestVO findCertRequestById(String pkId) {
        CertRequestInfo info = certDao.findCertRequestById(pkId);
        return (CertRequestVO) TransformUtils.simpleTransform(info, CertRequestVO.class);
    }

    // 根据ID查询私钥
    public CertKeyVO findCertKeyById(String certKeyId) {
        CertKeyInfo keyInfo = certDao.findKeyByPkId(certKeyId);
        return (CertKeyVO) TransformUtils.simpleTransform(keyInfo, CertKeyVO.class);
    }

    // 根据序列号查找证书
    public CertVO findBySerialNumber(String serialNumber) {
        CertInfo certInfo = certDao.findBySerialNumber(serialNumber);
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);
    }

    // 证书请求列表查询
    public PageOutput<CertRequestInfo> queryCertRequestList(String userId, String pCertId, int pageIndex,
            int pageSize) {
        return certDao.findCertRequestList(userId, pCertId, pageIndex, pageSize);
    }

    // 私钥列表查询
    public PageOutput<CertKeyInfo> findKeys(String userId, int pageIndex, int pageSize) {
        return certDao.findKeys(userId, pageIndex, pageSize);
    }

    public List<CertInfo> findCertList(String userId, String pCertId, Boolean isCACert, Boolean isRootCert) {
        return certDao.findCertList(userId, pCertId, isCACert, isCACert);
    }
    // 证书列表查询
    public PageOutput<CertInfo> findCertList(String userId, String pCertId, Boolean isCACert, Boolean isRootCert,
            int status, int pageIndex, int pageSize) {
        PageOutput<CertInfo> certInfos = certDao.findCertList(userId, pCertId, isCACert, isRootCert, status, pageIndex,
                pageSize);
        return certInfos;
    }

    // 初始化根证书
    @Transactional
    public CertVO initRootCert(String commonName, String organizationName, String organizationUnitName)
            throws Exception {
        X500NameInfo issuer = X500NameInfo.builder().commonName(commonName).organizationName(organizationName)
                .organizationalUnitName(organizationUnitName).build();
        String userId = CurrentAccountUtil.get().getId();
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + CertConstants.DEFAULT_VALIDITY);
//        KeyUsage keyUsage = new KeyUsage(KeyUsage.dataEncipherment); // 证书使用用途 数据加密

        // 生成公私钥 算法为RSA
        KeyPair keyPair = KeyUtils.generateKeyPair();
        String pemPrivateKey = CertUtils.readPEMAsString(keyPair.getPrivate());
        // 保存私钥
        CertKeyInfo certKeyInfo = savePrivateKey(userId, pemPrivateKey, KeyAlgorithmEnums.RSA.getKeyAlgorithm());

        // 私钥算法
        KeyAlgorithmEnums keyAlgorithm = KeyAlgorithmEnums.getByKeyAlg(certKeyInfo.getKeyAlg());
        CertDigestAlgEnums certDigestAlgEnums = getCertDigestAlg(keyAlgorithm);
        // 生成根证书
        X509Certificate certificate = certService.createRootCertificate(certDigestAlgEnums.getAlgorithmName(), issuer,
                null, beginDate, endDate, keyPair.getPublic(), keyPair.getPrivate());

        CertInfo certInfo = certDao.save(buildCertInfo(CertUtils.readPEMAsString(certificate), issuer.getCommonName(),
                issuer.getOrganizationName(), issuer.getCommonName(), issuer.getOrganizationName(), keyPair.getPublic(),
                userId, certificate.getSerialNumber(), certKeyInfo.getPkId(), certKeyInfo.getPkId(), true, true, null,
                null));
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);
    }

    // 创建issuer证书
    @Transactional
    public CertVO createIssuerCert(String rootCertId, String commonName, String organizationName,
            String organizationUnitName) throws Exception {
        String userId = CurrentAccountUtil.get().getId();
        X500NameInfo subject = X500NameInfo.builder().commonName(commonName).organizationName(organizationName)
                .organizationalUnitName(organizationUnitName).build();
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + 315360000000L);
        boolean isCaCert = true;
        // 生成公私钥 算法为RSA
        KeyPair subjectkeyPair = KeyUtils.generateKeyPair();
        String subjectPemPrivateKey = CertUtils.readPEMAsString(subjectkeyPair.getPrivate());
        // 保存私钥
        CertKeyInfo subjectCertKeyInfo = savePrivateKey(userId, subjectPemPrivateKey,
                KeyAlgorithmEnums.RSA.getKeyAlgorithm());

        // 获取签发机构的证书
        CertInfo issuerCertInfo = certDao.findCertById(rootCertId);
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
        KeyPair issuerKeyPair = getKeyPair(issuerKeyAlgorithm, DatabaseEncryptUtil.decrypt(issuerKeyInfo.getKeyPem()));

        // 签发机构的证书校验
        X509Certificate issuerCertificate = CertUtils.convertStrToCert(issuerCertInfo.getCertContent());
        try {
            issuerCertificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_VALIDITY_FAILURE);
        }

        // 生成证书请求
        CertDigestAlgEnums certDigestAlgEnums = getCertDigestAlg(
                KeyAlgorithmEnums.getByKeyAlg(subjectCertKeyInfo.getKeyAlg()));
        PKCS10CertificationRequest request = certService.createCertRequest(subject, subjectkeyPair.getPublic(),
                subjectkeyPair.getPrivate(), certDigestAlgEnums.getAlgorithmName());

        CertRequestInfo csrInfo = certDao.save(buildCertRequestInfo(CertUtils.readPEMAsString(request),
                subjectCertKeyInfo.getPkId(), subject.getCommonName(), subject.getOrganizationName(), userId));

        // 证书签发
        KeyUsage keyUsage = new KeyUsage(KeyUsage.dataEncipherment);
        X509Certificate certificate = certService.createChildCertificate(isCaCert,
                issuerCertDigestAlgEnums.getAlgorithmName(), issuerCertificate,
                CertUtils.convertStrToCsr(csrInfo.getCertRequestContent()), keyUsage, beginDate, endDate,
                issuerKeyPair.getPrivate());

        // 保存cert
        CertInfo certInfo = certDao
                .save(buildCertInfo(CertUtils.readPEMAsString(certificate), issuerCertInfo.getSubjectCN(),
                        issuerCertInfo.getSubjectOrg(), csrInfo.getSubjectCN(), csrInfo.getSubjectOrg(),
                        certificate.getPublicKey(), userId, certificate.getSerialNumber(), issuerKeyInfo.getPkId(),
                        subjectCertKeyInfo.getPkId(), isCaCert, false, issuerCertInfo.getPkId(), csrInfo.getPkId()));
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);

    }

    /**
     * 签发用户的证书
     */
    @Transactional
    public CertVO createUserCert(String issuerCertId, String memberId, String certRequestContent) throws Exception {
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + 315360000000L);
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature);
        boolean isCaCert = false;
        // 获取签发机构的证书
        CertInfo issuerCertInfo = certDao.findCertById(issuerCertId);
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
        KeyPair issuerKeyPair = getKeyPair(issuerKeyAlgorithm, DatabaseEncryptUtil.decrypt(issuerKeyInfo.getKeyPem()));

        // 签发机构的证书校验
        X509Certificate parentCertificate = CertUtils.convertStrToCert(issuerCertInfo.getCertContent());
        try {
            parentCertificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_VALIDITY_FAILURE);
        }

        PKCS10CertificationRequest csrObject = CertUtils.convertStrToCsr(certRequestContent);

        // 证书签发
        X509Certificate certificate = certService.createChildCertificate(isCaCert,
                issuerCertDigestAlgEnums.getAlgorithmName(), parentCertificate, csrObject, keyUsage, beginDate, endDate,
                issuerKeyPair.getPrivate());

        RDN org = csrObject.getSubject().getRDNs(BCStyle.O)[0];
        RDN cn = csrObject.getSubject().getRDNs(BCStyle.CN)[0];
        // 保存csr
        CertRequestInfo subjectRequestInfo = certDao.save(createUserCertRequest(cn.getFirst().getValue().toString(),
                memberId, certRequestContent, org.getFirst().getValue().toString(), null));
        // 保存cert
        CertInfo certInfo = certDao.save(buildCertInfo(CertUtils.readPEMAsString(certificate),
                issuerCertInfo.getSubjectCN(), issuerCertInfo.getSubjectOrg(), subjectRequestInfo.getSubjectCN(),
                subjectRequestInfo.getSubjectOrg(), certificate.getPublicKey(), memberId, certificate.getSerialNumber(),
                issuerKeyInfo.getPkId(), null, isCaCert, false, issuerCertInfo.getPkId(),
                subjectRequestInfo.getPkId()));
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);
    }

    // 保存私钥
    private CertKeyInfo savePrivateKey(String userId, String pemPrivateKey, String priAlg) throws Exception {
        if (StringUtils.isBlank(userId)) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_ACCOUNT_NOT_EXIST);
        }
        try {
            KeyUtils.getRSAKeyPair(pemPrivateKey);
        } catch (Exception e) {
            LOG.error("importPrivateKey failed, reason :", e);
            throw e;
        }
        CertKeyInfo certKeyInfo = new CertKeyInfo();
        certKeyInfo.setKeyAlg(priAlg);
        certKeyInfo.setKeyPem(DatabaseEncryptUtil.encrypt(pemPrivateKey));
        certKeyInfo.setUserId(userId);
        certKeyInfo.setCreatedBy(CurrentAccountUtil.get().getId());
        certKeyInfo = certDao.save(certKeyInfo);
        return certKeyInfo;
    }

    private CertRequestInfo createUserCertRequest(String commonName, String userId, String certRequestContent,
            String organizationName, X500NameInfo subject) throws Exception {
        if (StringUtils.isBlank(userId)) {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_ACCOUNT_NOT_EXIST);
        }
        return certDao.save(buildCertRequestInfo(certRequestContent, null, commonName, organizationName, userId));
    }

    private CertRequestInfo buildCertRequestInfo(String csrStr, String subjectKeyId, String commonName,
            String organizationName, String userId) {
        CertRequestInfo certRequestInfo = new CertRequestInfo();
        certRequestInfo.setUserId(userId);
        certRequestInfo.setCreatedBy(CurrentAccountUtil.get().getId());
        certRequestInfo.setSubjectKeyId(subjectKeyId);
        certRequestInfo.setSubjectCN(commonName);
        certRequestInfo.setSubjectOrg(organizationName);
        certRequestInfo.setCertRequestContent(csrStr);
        certRequestInfo.setIssue(true);
        return certRequestInfo;
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
     * @param csrId             证书请求ID
     * @return
     */
    private CertInfo buildCertInfo(String certificatePemStr, String issuerCommonName, String issuerOrgName,
            String subjectCommonName, String subjectOrgName, PublicKey publicKey, String userId,
            BigInteger serialNumber, String issuerCertKeyId, String subjectKeyId, boolean isCACert, boolean isRootCert,
            String issuerCertId, String csrId) {
        CertInfo certInfo = new CertInfo();
        certInfo.setUserId(userId);
        certInfo.setCreatedBy(CurrentAccountUtil.get().getId());
        certInfo.setIssuerCN(issuerCommonName);
        certInfo.setIssuerOrg(issuerOrgName);
        certInfo.setpCertId(issuerCertId);
        certInfo.setSubjectCN(subjectCommonName);
        certInfo.setSubjectOrg(subjectOrgName);
        certInfo.setSubjectPubKey(CertUtils.readPEMAsString(publicKey));
        certInfo.setIssuerKeyId(issuerCertKeyId); // 签发机构的证书的私钥ID 只有根证书和ca证书需要
        certInfo.setSubjectKeyId(subjectKeyId); // 自己的私钥ID
        certInfo.setCertContent(certificatePemStr);
        certInfo.setSerialNumber(String.valueOf(serialNumber));
        certInfo.setIsCACert(isCACert);
        certInfo.setIsRootCert(isRootCert);
        certInfo.setCsrId(csrId);
        certInfo.setStatus(CertStatusEnums.VALID.getCode());
        return certInfo;
    }

    /**
     * 根据私钥算法获取数字签名算法
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

    /**
     * 获取私钥对象
     * 
     * @param keyAlgorithm  私钥算法
     * @param pemPrivateKey 私钥pem内容
     * @return
     * @throws Exception
     */
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
}
