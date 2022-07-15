package com.welab.wefe;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.cert.mgr.enums.KeyAlgorithmEnums;
import com.webank.cert.mgr.model.vo.CertKeyVO;
import com.webank.cert.mgr.model.vo.CertRequestVO;
import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.service.CertManagerService;
import com.webank.cert.toolkit.constants.CertConstants;
import com.webank.cert.toolkit.model.X500NameInfo;
import com.webank.cert.toolkit.utils.CertUtils;
import com.webank.cert.toolkit.utils.FileOperationUtils;
import com.webank.cert.toolkit.utils.KeyUtils;
import com.welab.wefe.manager.service.ManagerService;

/**
 * @author wesleywang
 * @Description:
 * @date 2020-05-20
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ManagerService.class })
public class CertManagerServiceTest {

    @Autowired
    private CertManagerService certManagerService;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Test
    public void testCreateRootCert() throws Exception {
        X500NameInfo issuer = X500NameInfo.builder().commonName("root").organizationName("welab")
                .organizationalUnitName("it").build();
        String userId = UUID.randomUUID().toString().replaceAll("-", "");
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + CertConstants.DEFAULT_VALIDITY);
        KeyUsage keyUsage = new KeyUsage(KeyUsage.dataEncipherment); // 证书使用用途
        CertVO cert = certManagerService.createRootCert(userId, issuer, keyUsage, beginDate, endDate);
        System.out.println(cert.getUserId()); // 1672c86067944280b66c818b28ef8921
        System.out.println(cert.getIssuerKeyId());// 4b02a3b3ba5e47ba9e708a21ded28439
        System.out.println(cert.getPkId());// f3a76f8b1ac8414ebf3872be01dd8a17
    }

    // 生成私钥
    @Test
    public void testImportKey() throws Exception {
        String userId = UUID.randomUUID().toString().replaceAll("-", "");
        // 生成公私钥 算法为RSA
        KeyPair keyPair = KeyUtils.generateKeyPair();
        String pemPrivateKey = CertUtils.readPEMAsString(keyPair.getPrivate());
        // 保存私钥
        String certKeyId = certManagerService.importPrivateKey(userId, pemPrivateKey,
                KeyAlgorithmEnums.RSA.getKeyAlgorithm());
        System.out.println(certKeyId); // 677150aa36fe470dbba68603e0e0f714
        System.out.println(userId);
    }

    // 生成csr
    @Test
    public void testCreateCertRequestByKey() throws Exception {
        X500NameInfo subject = X500NameInfo.builder().commonName("wefe").organizationName("welab")
                .organizationalUnitName("it").build();
        // 申请人私钥ID
        String subjectKeyId = "677150aa36fe470dbba68603e0e0f714";
        // 申请人用户ID
        String subjectUserId = "a2c9a8edcf754bcb9370d27f84fc719b";
        // issuer证书ID
        String issuerCertId = "f3a76f8b1ac8414ebf3872be01dd8a17";
        CertRequestVO vo = certManagerService.createCertRequestByKey(subjectUserId, subjectKeyId, issuerCertId,
                subject);
        System.out.println(vo.getPkId()); // a3574b69a19f46a9909ec7bae2519735
    }

    // 签发证书
    @Test
    public void testCreateChildCert() throws Exception {
        String userId = "a2c9a8edcf754bcb9370d27f84fc719b";
        String csrId = "a3574b69a19f46a9909ec7bae2519735";
        CertVO cert = certManagerService.createChildCert(userId, csrId);
        System.out.println(cert.getPkId());// f2555e5664294e47b7367960a6d98187
    }

    // 一次性签发证书
    @Test
    public void testOnceCreateChildCert() throws Exception {
        String commonName = "yinlian";
        // issuer签发机构证书ID
        String issuerCertId = "f2555e5664294e47b7367960a6d98187";

        String userId = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println("为用户生成证书 userId=: " + userId);
        // 生成公私钥 算法为RSA
        KeyPair keyPair = KeyUtils.generateKeyPair();
        String pemPrivateKey = CertUtils.readPEMAsString(keyPair.getPrivate());
        // 保存私钥
        String certKeyId = certManagerService.importPrivateKey(userId, pemPrivateKey,
                KeyAlgorithmEnums.RSA.getKeyAlgorithm());
        System.out.println("为用户生成私钥 certKeyId=" + certKeyId);

        X500NameInfo subject = X500NameInfo.builder().commonName(commonName).organizationName(commonName)
                .organizationalUnitName("it").build();
        // 申请人私钥ID
        String subjectKeyId = certKeyId;
        // 申请人用户ID
        String subjectUserId = userId;
        CertRequestVO vo = certManagerService.createCertRequestByKey(subjectUserId, subjectKeyId, issuerCertId,
                subject);
        System.out.println("为用户生成证书请求 csrId = " + vo.getPkId());

        String csrId = vo.getPkId();
        CertVO cert = certManagerService.createChildCert(userId, csrId);
        System.out.println("为用户生成证书 : certId = " + cert.getPkId());

        String certId = cert.getPkId();
        FileOperationUtils.mkdir("out1");
        certManagerService.exportCertToFile(certId, "out1/" + commonName + ".crt");
        System.out.println("为用户导出证书 : file = " + "out1/" + commonName + ".crt");
    }

    // 通过私钥将证书转为p12/pfx
    @Test
    public void cert2P12() throws Exception {
        // 私钥ID
        String userKeyId = "359e39346baf43bfb7ff3121c0f27ec2";
        // 证书ID
        String certId = "63cd3411ba2441d19daefcf944ae432e";
        CertKeyVO keyVo = certManagerService.queryCertKey(userKeyId);
        CertVO certVO = certManagerService.queryCertInfoByCertId(certId);

        PrivateKey privateKey = KeyUtils.getRSAPrivateKey(keyVo.getKeyPem());
        X509Certificate cert = CertUtils.convertStrToCert(certVO.getCertContent());

        List<X509Certificate> list = new ArrayList<>();
        list.add(cert);
        // 生成pfx文件，参数分别为：证书别名，私钥，keyStore密码，证书信息，保存路径，证书名
        CertUtils.saveJks("yinlian", privateKey, "yinlian", list, "out1/", "yinlian");
        // 从pfx中导出私钥信息
        PrivateKey key = CertUtils.readPriKeyFromJks("out1/yinlian.jks", "yinlian");
        // 在控制台输出导出私钥的BASE64编码信息
        System.out.println(Base64.toBase64String(key.getEncoded()));
    }

    // 将证书导入到jks中
    // 如果重复导入也不会报错
    @Test
    public void testImportCertToTrust()
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        // 证书ID
//        String rootCertId = "f3a76f8b1ac8414ebf3872be01dd8a17";
//        CertVO rootCertVO = certManagerService.queryCertInfoByCertId(rootCertId);
//        X509Certificate rootCert = CertUtils.convertStrToCert(rootCertVO.getCertContent());
//        CertUtils.importCertToTrustStore("root", rootCert, "out1/truststore.jks", "123456");
//        
        String welabCertId = "f2555e5664294e47b7367960a6d98187";
        CertVO welabCertVO = certManagerService.queryCertInfoByCertId(welabCertId);
        X509Certificate welabCert = CertUtils.convertStrToCert(welabCertVO.getCertContent());
        CertUtils.importCertToTrustStore("welab", welabCert, "out1/truststore.jks", "123456");
    }

    // 导出证书
    @Test
    public void testExportCertToFile() throws Exception {
        String certId = "f2555e5664294e47b7367960a6d98187";
        FileOperationUtils.mkdir("out1");
        certManagerService.exportCertToFile(certId, "out1/welab.crt");
    }

    // 重置证书
    @Test
    public void testResetCertificate() throws Exception {
        String userId = "d84194f4ba1d40a2a06b7c63132c8ea9";
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + CertConstants.DEFAULT_VALIDITY);
        String certId = "56b0e79603774388a084cd97aeb2617c";
        String root = certManagerService
                .resetCertificate(userId, certId, new KeyUsage(KeyUsage.dataEncipherment), beginDate, endDate)
                .getCertContent();
        System.out.println(root);
    }

    @Test
    public void testQueryCertList() {
        String userId = "1672c86067944280b66c818b28ef8921";
        List<CertVO> list = certManagerService.queryCertList(userId, null, null, null, null, null);
        list.stream().forEach(System.out::println);
    }

    @Test
    public void testQueryCertRequestList() {
        String userId = "d84194f4ba1d40a2a06b7c63132c8ea9";
        List<CertRequestVO> list = certManagerService.queryCertRequestList(userId, null, null, null, null, null);
        list.stream().forEach(System.out::println);
    }

    @Test
    public void testQueryCertKeyList() {
        String userId = "1672c86067944280b66c818b28ef8921";
        List<CertKeyVO> list = certManagerService.queryCertKeyList(userId);
        list.stream().forEach(System.out::println);
    }

    @Test
    public void testQueryCertInfoByCertId() {
        String certId = "56b0e79603774388a084cd97aeb2617c";
        CertVO certInfo = certManagerService.queryCertInfoByCertId(certId);
        System.out.println(certInfo);
    }

    @Test
    public void testQueryCertRequestByCsrId() {
        String csrId = "c1c8325d6ddd4923971dfbb3344280aa";
        CertRequestVO keyRequestVO = certManagerService.queryCertRequestByCsrId(csrId);
        System.out.println(keyRequestVO);
    }

}
