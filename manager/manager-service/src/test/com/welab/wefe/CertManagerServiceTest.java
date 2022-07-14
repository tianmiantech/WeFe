package com.welab.wefe;

import java.security.KeyPair;
import java.security.Security;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
        System.out.println(cert.getUserId());
        System.out.println(cert.getIssuerKeyId());
    }

    @Test
    public void testImportKey() throws Exception {
        String userId = UUID.randomUUID().toString().replaceAll("-", "");
        // 生成公私钥 算法为RSA
        KeyPair keyPair = KeyUtils.generateKeyPair();
        String pemPrivateKey = CertUtils.readPEMAsString(keyPair.getPrivate());
        // 保存私钥
        String certKeyId = certManagerService.importPrivateKey(userId, pemPrivateKey,
                KeyAlgorithmEnums.RSA.getKeyAlgorithm());
        System.out.println(certKeyId);
    }

    // 生成csr
    @Test
    public void testCreateCertRequestByKey() throws Exception {
        X500NameInfo subject = X500NameInfo.builder().commonName("yinlian").organizationName("yinlian")
                .organizationalUnitName("it").build();
        // 申请人私钥ID
        String subjectKeyId = "40c01108bd644ace98dce22dfb90faaa";
        // 申请人用户ID
        String subjectUserId = "d84194f4ba1d40a2a06b7c63132c8ea9";
        // issuer证书ID
        String issuerCertId = "f3a76f8b1ac8414ebf3872be01dd8a17";
        CertRequestVO vo = certManagerService.createCertRequestByKey(subjectUserId, subjectKeyId, issuerCertId,
                subject);
        System.out.println(vo.getCertRequestContent());
    }

    // 签发证书
    @Test
    public void testCreateChildCert() throws Exception {
        String userId = "d84194f4ba1d40a2a06b7c63132c8ea9";
        String csrId = "c1c8325d6ddd4923971dfbb3344280aa";
        String certContent = certManagerService.createChildCert(userId, csrId).getCertContent();
        System.out.println(certContent);
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

    // 导出证书
    @Test
    public void testExportCertToFile() throws Exception {
        String certId = "f3a76f8b1ac8414ebf3872be01dd8a17";
        FileOperationUtils.mkdir("out1");
        certManagerService.exportCertToFile(certId, "out1/root.crt");
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
