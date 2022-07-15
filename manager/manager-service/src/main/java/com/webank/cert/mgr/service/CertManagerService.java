//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.webank.cert.mgr.service;

import java.security.KeyPair;
import java.security.Security;
import java.util.Date;
import java.util.List;

import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.webank.cert.mgr.enums.KeyAlgorithmEnums;
import com.webank.cert.mgr.enums.MgrExceptionCodeEnums;
import com.webank.cert.mgr.exception.CertMgrException;
import com.webank.cert.mgr.handler.CertHandler;
import com.webank.cert.mgr.model.vo.CertKeyVO;
import com.webank.cert.mgr.model.vo.CertRequestVO;
import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.utils.TransformUtils;
import com.webank.cert.toolkit.model.X500NameInfo;
import com.webank.cert.toolkit.utils.CertUtils;
import com.webank.cert.toolkit.utils.KeyUtils;
import com.welab.wefe.common.data.mongodb.entity.manager.CertInfo;
import com.welab.wefe.common.data.mongodb.entity.manager.CertKeyInfo;
import com.welab.wefe.common.data.mongodb.entity.manager.CertRequestInfo;

@Service
public class CertManagerService {
    @Autowired
    private CertHandler certHandler;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public CertManagerService() {
    }

    // 创建根证书1
    public CertVO createRootCert(String userId, X500NameInfo issuer) throws Exception {
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + 315360000000L); // 过期时间
        return this.createRootCert(userId, issuer, beginDate, endDate);
    }

    // 创建根证书2
    public CertVO createRootCert(String userId, X500NameInfo issuer, Date beginDate, Date endDate) throws Exception {
        // 生成公私钥 算法为RSA
        KeyPair keyPair = KeyUtils.generateKeyPair();
        String pemPrivateKey = CertUtils.readPEMAsString(keyPair.getPrivate());
        // 保存私钥
        String certKeyId = this.importPrivateKey(userId, pemPrivateKey, KeyAlgorithmEnums.RSA.getKeyAlgorithm());
        // 根据私钥创建根证书
        return this.createRootCert(userId, certKeyId, issuer, beginDate, endDate);
    }

    // 创建根证书3
    public CertVO createRootCert(String userId, String certKeyId, X500NameInfo issuer, Date beginDate, Date endDate)
            throws Exception {
        return this.createRootCert(userId, certKeyId, issuer, (KeyUsage) null, beginDate, endDate);
    }

    // 创建根证书4
    public CertVO createRootCert(String userId, String certKeyId, X500NameInfo issuer, KeyUsage keyUsage,
            Date beginDate, Date endDate) throws Exception {
        CertInfo certInfo = this.certHandler.createRootCert(userId, certKeyId, (String) null, (KeyAlgorithmEnums) null,
                issuer, keyUsage, beginDate, endDate);
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);
    }

    // 创建根证书5
    public CertVO createRootCert(String userId, X500NameInfo issuer, KeyUsage keyUsage, Date beginDate, Date endDate)
            throws Exception {
        // 生成公私钥 算法为RSA
        KeyPair keyPair = KeyUtils.generateKeyPair();
        String pemPrivateKey = CertUtils.readPEMAsString(keyPair.getPrivate());
        // 保存私钥
        String certKeyId = this.importPrivateKey(userId, pemPrivateKey, KeyAlgorithmEnums.RSA.getKeyAlgorithm());

        CertInfo certInfo = this.certHandler.createRootCert(userId, certKeyId, (String) null, (KeyAlgorithmEnums) null,
                issuer, keyUsage, beginDate, endDate);
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);
    }

    // 私钥pem格式作为入参生成根证书
    public CertVO createRootCert(String userId, String pemPrivateKey, KeyAlgorithmEnums keyAlgorithm,
            X500NameInfo issuer) throws Exception {
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + 315360000000L);
        return this.createRootCert(userId, pemPrivateKey, keyAlgorithm, issuer, beginDate, endDate);
    }

    // 私钥pem格式作为入参生成根证书
    public CertVO createRootCert(String userId, String pemPrivateKey, KeyAlgorithmEnums keyAlgorithm,
            X500NameInfo issuer, Date beginDate, Date endDate) throws Exception {
        return this.createRootCert(userId, pemPrivateKey, keyAlgorithm, issuer, (KeyUsage) null, beginDate, endDate);
    }

    // 私钥pem格式作为入参生成根证书
    public CertVO createRootCert(String userId, String pemPrivateKey, KeyAlgorithmEnums keyAlgorithm,
            X500NameInfo issuer, KeyUsage keyUsage, Date beginDate, Date endDate) throws Exception {
        String certKeyId = this.importPrivateKey(userId, pemPrivateKey, keyAlgorithm.getKeyAlgorithm());
        CertInfo certInfo = this.certHandler.createRootCert(userId, certKeyId, pemPrivateKey, keyAlgorithm, issuer,
                keyUsage, beginDate, endDate);
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);
    }

    public CertRequestVO createCertRequestByKey(String userId, String subjectKeyId, String issuerCertId,
            X500NameInfo subject) throws Exception {
        CertRequestInfo certRequestInfo = this.certHandler.createCertRequest(userId, subjectKeyId, null, null,
                issuerCertId, subject);
        return (CertRequestVO) TransformUtils.simpleTransform(certRequestInfo, CertRequestVO.class);
    }

    // 生成子证书
    public CertVO createChildCert(String userId, String csrId) throws Exception {
        return this.createChildCert(userId, csrId, true);
    }

    // 生成子证书
    public CertVO createChildCert(String userId, String csrId, boolean isCaCert) throws Exception {
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + 315360000000L);
        return this.createChildCert(userId, csrId, isCaCert, beginDate, endDate);
    }

    // 生成子证书
    public CertVO createChildCert(String userId, String csrId, boolean isCaCert, Date beginDate, Date endDate)
            throws Exception {
        return this.createChildCert(userId, csrId, isCaCert, (KeyUsage) null, beginDate, endDate);
    }

    // 生成子证书
    public CertVO createChildCert(String userId, String csrId, boolean isCaCert, KeyUsage keyUsage, Date beginDate,
            Date endDate) throws Exception {
        CertInfo certInfo = this.certHandler.createChildCert(userId, csrId, isCaCert, keyUsage, beginDate, endDate);
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);
    }

    // 导出证书到文件
    public void exportCertToFile(String certId, String filePath) throws Exception {
        CertVO certVO = this.queryCertInfoByCertId(certId);
        if (certVO != null && !StringUtils.isEmpty(certVO.getCertContent())) {
            CertUtils.writeCrt(CertUtils.convertStrToCert(certVO.getCertContent()), filePath);
        } else {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_NOT_EXIST);
        }
    }

    // 证书重置
    public CertVO resetCertificate(String userId, String certId) throws Exception {
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + 315360000000L);
        return this.resetCertificate(userId, certId, (KeyUsage) null, beginDate, endDate);
    }

    // 证书重置
    public CertVO resetCertificate(String userId, String certId, KeyUsage keyUsage, Date beginDate, Date endDate)
            throws Exception {
        CertInfo certInfo = this.certHandler.resetCertificate(userId, certId, keyUsage, beginDate, endDate);
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);
    }

    // 证书列表查询
    public List<CertVO> queryCertInfoList() {
        return queryCertList(null, null, null, null, null, null);
    }

    // 证书列表查询
    public List<CertVO> queryCertList(String userId, String issuerKeyId, String pCertId, String issuerOrg,
            String issuerCN, Boolean isCACert) {
        List<CertInfo> certInfos = this.certHandler.queryCertInfoList(userId, issuerKeyId, pCertId, issuerOrg, issuerCN,
                isCACert);
        return TransformUtils.simpleTransform(certInfos, CertVO.class);
    }

    // 证书请求列表查询
    public List<CertRequestVO> queryCertRequestList() {
        return queryCertRequestList(null, null, null, null, null, null);
    }

    // 证书请求列表查询
    public List<CertRequestVO> queryCertRequestList(String userId, String subjectKeyId, String pCertId,
            String subjectOrg, String subjectCN, String pCertUserId) {
        List<CertRequestInfo> certRequestInfos = this.certHandler.queryCertRequestList(userId, subjectKeyId, pCertId,
                subjectOrg, subjectCN, pCertUserId);
        return TransformUtils.simpleTransform(certRequestInfos, CertRequestVO.class);
    }

    // 证书私钥列表查询
    public List<CertKeyVO> queryCertKeyList(String userId) {
        List<CertKeyInfo> certKeyInfos = this.certHandler.queryCertKeyList(userId);
        return TransformUtils.simpleTransform(certKeyInfos, CertKeyVO.class);
    }

    // 根据id查询证书
    public CertVO queryCertInfoByCertId(String certId) {
        CertInfo certInfo = this.certHandler.queryCertInfoByCertId(certId);
        return (CertVO) TransformUtils.simpleTransform(certInfo, CertVO.class);
    }

    // 根据id查询证书请求
    public CertRequestVO queryCertRequestByCsrId(String csrId) {
        CertRequestInfo certRequestInfo = this.certHandler.queryCertRequestByCsrId(csrId);
        return (CertRequestVO) TransformUtils.simpleTransform(certRequestInfo, CertRequestVO.class);
    }

    // 导入私钥
    public String importPrivateKey(String userId, String pemPrivateKey, String priAlg) throws Exception {
        return this.certHandler.importPrivateKey(userId, pemPrivateKey, priAlg);
    }

    // 查询私钥
    public CertKeyVO queryCertKey(String pkId) {
        CertKeyInfo info = this.certHandler.queryCertKey(pkId);
        return (CertKeyVO) TransformUtils.simpleTransform(info, CertKeyVO.class);
    }

}
