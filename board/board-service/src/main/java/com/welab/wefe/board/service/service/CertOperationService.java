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
package com.welab.wefe.board.service.service;

import com.webank.cert.toolkit.enums.CertDigestAlgEnums;
import com.webank.cert.toolkit.enums.CertStatusEnums;
import com.webank.cert.toolkit.enums.KeyAlgorithmEnums;
import com.webank.cert.toolkit.model.X500NameInfo;
import com.webank.cert.toolkit.service.CertService;
import com.webank.cert.toolkit.utils.CertUtils;
import com.webank.cert.toolkit.utils.KeyUtils;
import com.welab.wefe.board.service.database.entity.cert.CertInfoMysqlModel;
import com.welab.wefe.board.service.database.entity.cert.CertKeyInfoMysqlModel;
import com.welab.wefe.board.service.database.entity.cert.CertRequestInfoMysqlModel;
import com.welab.wefe.board.service.database.repository.CertInfoRepository;
import com.welab.wefe.board.service.database.repository.CertKeyInfoRepository;
import com.welab.wefe.board.service.database.repository.CertRequestInfoRepository;
import com.welab.wefe.board.service.sdk.union.UnionService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.AESUtil;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.wefe.dto.global_config.MemberInfoModel;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

@Service
public class CertOperationService {

    protected static final Logger LOG = LoggerFactory.getLogger(CertOperationService.class);
    @Autowired
    private CertInfoRepository certInfoRepository;
    @Autowired
    private CertRequestInfoRepository certRequestInfoRepository;
    @Autowired
    private CertKeyInfoRepository certKeyInfoRepository;
    @Autowired
    private CertService certService;
    @Autowired
    private GlobalConfigService globalConfigService;
    @Autowired
    protected GatewayService gatewayService;
    @Autowired
    protected UnionService unionService;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // 更新证书状态
    public void updateStatus(String serialNumber, String status) throws StatusCodeWithException {
        CertInfoMysqlModel certInfo = findBySerialNumber(serialNumber);
        if (certInfo == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "数据不存在");
        }
        certInfo.setStatus(status);
        certInfo.setUpdatedBy(CurrentAccountUtil.get().getId());
        certInfo.setUpdatedTime(new Date());
        certInfoRepository.save(certInfo);
    }

    public void resetCert() {
        certInfoRepository.resetCert(CertStatusEnums.INVALID.name());
    }

    public void saveCertInfo(String certRequestId, String certPemContent, String certStatus)
            throws StatusCodeWithException {
        try {
            if (StringUtils.isBlank(certPemContent)) {
                return;
            }
            CertRequestInfoMysqlModel certRequestInfoMysqlModel = findCertRequestById(certRequestId);
            if (CertStatusEnums.WAIT_VERIFY.name().equalsIgnoreCase(certStatus)) {
                return;
            }

            // 根据证书内容读取证书
            X509Certificate certificate = CertUtils.convertStrToCert(certPemContent);
            CertInfoMysqlModel certInfo = findBySerialNumber(String.valueOf(certificate.getSerialNumber()));

            // 如果证书请求 已经 签发过了并且状态没有改变
            if (certRequestInfoMysqlModel.getIssue() && certInfo.getStatus().equalsIgnoreCase(certStatus)) {
                return;
            }

            PublicKey publicKey = null;
            if (certInfo == null) {
                certRequestInfoMysqlModel.setIssue(true);
                certRequestInfoMysqlModel.setUpdatedTime(new Date());
                certRequestInfoRepository.save(certRequestInfoMysqlModel);

                // 如果被拒绝了，则不需要保存证书信息
                if (CertStatusEnums.VALID.name().equalsIgnoreCase(certStatus)) {
                    CertKeyInfoMysqlModel certKeyInfoMysqlModel = queryCertKeyInfoById(
                            certRequestInfoMysqlModel.getSubjectKeyId());
                    try {
                        KeyPair keyPair = getKeyPair(KeyAlgorithmEnums.getByKeyAlg(certKeyInfoMysqlModel.getKeyAlg()),
                                AESUtil.decrypt(certKeyInfoMysqlModel.getKeyPem(), CacheObjects.getMemberId()));
                        publicKey = keyPair.getPublic();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    certInfo = buildCertInfo(certPemContent, certRequestInfoMysqlModel.getSubjectCN(),
                            certRequestInfoMysqlModel.getSubjectOrg(), publicKey, CacheObjects.getMemberId(),
                            certificate.getSerialNumber(), certRequestId, certStatus);
                    certInfoRepository.save(certInfo);
                }

            } else {
                if (!certRequestInfoMysqlModel.getIssue()) {
                    certRequestInfoMysqlModel.setIssue(true);
                    certRequestInfoMysqlModel.setUpdatedTime(new Date());
                    certRequestInfoRepository.save(certRequestInfoMysqlModel);
                }
                if (!certInfo.getStatus().equalsIgnoreCase(certStatus)) {
                    certInfo.setStatus(certStatus);
                    certInfoRepository.save(certInfo);
                }
            }
            MemberInfoModel model = globalConfigService.getModel(MemberInfoModel.class);
            if (CertStatusEnums.INVALID.name().equalsIgnoreCase(certStatus) && model.getMemberGatewayTlsEnable()) {
                model.setMemberGatewayTlsEnable(false);
                globalConfigService.put(model);
                unionService.uploadMemberInfoExcludeLogo(model);
                CacheObjects.refreshMemberInfo();
                try {
                    gatewayService.restartExternalGrpcServer();
                } catch (Exception e) {
                    LOG.error("restartExternalGrpcServer error", e);
                }
            }

        } catch (CertificateException e) {
            throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST, e.getMessage());
        }
    }

    // 导出证书到文件
    public void exportCertToDerFile(String certId, String filePath) throws Exception {
        CertInfoMysqlModel model = queryCertInfoById(certId);
        if (model != null && !StringUtils.isEmpty(model.getCertContent())) {
            CertUtils.writeCrt(CertUtils.convertStrToCert(model.getCertContent()), filePath);
        } else {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "数据不存在");
        }
    }

    // 根据id查询证书
    public CertInfoMysqlModel queryCertInfoById(String id) {
        return certInfoRepository.findOne("id", id, CertInfoMysqlModel.class);
    }

    // 查找所有的证书
    public List<CertInfoMysqlModel> queryAllCertInfo() {
        return certInfoRepository.findAll();
    }

    // 根据ID查询证书请求
    public CertRequestInfoMysqlModel findCertRequestById(String id) {
        CertRequestInfoMysqlModel info = certRequestInfoRepository.findOne("id", id, CertRequestInfoMysqlModel.class);
        return info;
    }

    // 查询所有的证书请求
    public List<CertRequestInfoMysqlModel> queryAllCertRequestInfo() {
        return certRequestInfoRepository.findAll();
    }

    // 根据序列号查找证书
    public CertInfoMysqlModel findBySerialNumber(String serialNumber) {
        return certInfoRepository.findOne("serialNumber", serialNumber, CertInfoMysqlModel.class);
    }

    // 根据id查询私钥
    public CertKeyInfoMysqlModel queryCertKeyInfoById(String id) {
        return certKeyInfoRepository.findOne("id", id, CertKeyInfoMysqlModel.class);
    }

    // 查找所有的证书
    public List<CertKeyInfoMysqlModel> queryAllCertKey() {
        return certKeyInfoRepository.findAll();
    }

    // 创建证书请求
    public CertRequestInfoMysqlModel createCertRequestInfo(String commonName, String organizationName,
            String organizationUnitName) throws Exception {
        String userId = CacheObjects.getMemberId();
        X500NameInfo subject = X500NameInfo.builder().commonName(commonName).organizationName(organizationName)
                .organizationalUnitName(organizationUnitName).build();
        // 生成公私钥 算法为RSA
        KeyPair subjectkeyPair = KeyUtils.generateKeyPair();
        String subjectPemPrivateKey = CertUtils.readPEMAsString(subjectkeyPair.getPrivate());
        // 保存私钥
        CertKeyInfoMysqlModel subjectCertKeyInfo = savePrivateKey(userId, subjectPemPrivateKey,
                KeyAlgorithmEnums.RSA.getKeyAlgorithm());
        // 生成证书请求
        CertDigestAlgEnums certDigestAlgEnums = getCertDigestAlg(
                KeyAlgorithmEnums.getByKeyAlg(subjectCertKeyInfo.getKeyAlg()));
        PKCS10CertificationRequest request = certService.createCertRequest(subject, subjectkeyPair.getPublic(),
                subjectkeyPair.getPrivate(), certDigestAlgEnums.getAlgorithmName());
        CertRequestInfoMysqlModel csrInfo = certRequestInfoRepository
                .save(buildCertRequestInfo(CertUtils.readPEMAsString(request), subjectCertKeyInfo.getId(),
                        subject.getCommonName(), subject.getOrganizationName(), userId, false));
        return csrInfo;
    }

    // 保存私钥
    private CertKeyInfoMysqlModel savePrivateKey(String memberId, String pemPrivateKey, String priAlg)
            throws Exception {
        if (StringUtils.isBlank(memberId)) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "memberId is empty");
        }
        try {
            KeyUtils.getRSAKeyPair(pemPrivateKey);
        } catch (Exception e) {
            LOG.error("importPrivateKey failed, reason :", e);
            throw e;
        }
        CertKeyInfoMysqlModel certKeyInfo = new CertKeyInfoMysqlModel();
        certKeyInfo.setKeyAlg(priAlg);
        certKeyInfo.setKeyPem(AESUtil.encrypt(pemPrivateKey, CacheObjects.getMemberId()));
        certKeyInfo.setMemberId(memberId);
        certKeyInfo = certKeyInfoRepository.save(certKeyInfo);
        return certKeyInfo;
    }

    private CertRequestInfoMysqlModel buildCertRequestInfo(String csrStr, String subjectKeyId, String commonName,
            String organizationName, String memberId, boolean issue) {
        CertRequestInfoMysqlModel certRequestInfo = new CertRequestInfoMysqlModel();
        certRequestInfo.setMemberId(memberId);
        certRequestInfo.setSubjectKeyId(subjectKeyId);
        certRequestInfo.setSubjectCN(commonName);
        certRequestInfo.setSubjectOrg(organizationName);
        certRequestInfo.setCertRequestContent(csrStr);
        certRequestInfo.setIssue(issue);
        certRequestInfo.setCreatedTime(new Date());
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
     * @param isCACert          是否是根证书
     * @param issuerCertId      签发机构证书ID
     * @param csrId             证书请求ID
     * @return
     */
    private CertInfoMysqlModel buildCertInfo(String certificatePemStr, String subjectCommonName, String subjectOrgName,
            PublicKey publicKey, String memberId, BigInteger serialNumber, String csrId, String certStatus) {
        CertInfoMysqlModel certInfo = new CertInfoMysqlModel();
        certInfo.setMemberId(memberId);
        certInfo.setSubjectCN(subjectCommonName);
        certInfo.setSubjectOrg(subjectOrgName);
        certInfo.setCreatedTime(new Date());
        if (publicKey != null) {
            certInfo.setSubjectPubKey(CertUtils.readPEMAsString(publicKey));
        }
        certInfo.setCertContent(certificatePemStr);
        certInfo.setSerialNumber(String.valueOf(serialNumber));
        certInfo.setCsrId(csrId);
        certInfo.setStatus(certStatus);
        return certInfo;
    }

    /**
     * 根据私钥算法获取数字签名算法
     * 
     * @throws StatusCodeWithException
     */
    private CertDigestAlgEnums getCertDigestAlg(KeyAlgorithmEnums keyAlgorithm) throws StatusCodeWithException {
        if (keyAlgorithm == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "key algorithms are not supported");
        }
        CertDigestAlgEnums certDigestAlgEnums = CertDigestAlgEnums.getByKeyAlg(keyAlgorithm.getKeyAlgorithm());
        if (certDigestAlgEnums == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "key algorithms are not supported");
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
    public KeyPair getKeyPair(KeyAlgorithmEnums keyAlgorithm, String pemPrivateKey) throws Exception {
        KeyPair keyPair = null;
        if (keyAlgorithm.equals(KeyAlgorithmEnums.ECDSA) || keyAlgorithm.equals(KeyAlgorithmEnums.SM2)) {
            keyPair = KeyUtils.getECKeyPair(pemPrivateKey);
        }
        if (keyAlgorithm.equals(KeyAlgorithmEnums.RSA)) {
            keyPair = KeyUtils.getRSAKeyPair(pemPrivateKey);
        }
        if (keyPair == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "key algorithms are not supported");
        }
        return keyPair;
    }
}
