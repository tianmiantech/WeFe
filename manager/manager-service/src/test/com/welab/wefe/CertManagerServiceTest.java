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
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
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

    public static final String basePATH = "out";
    @Autowired
    private CertManagerService certManagerService;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Test
    public void testCreateRootCert() throws Exception {
        FileOperationUtils.mkdir(basePATH);
        X500NameInfo issuer = X500NameInfo.builder().commonName("root").organizationName("welab")
                .organizationalUnitName("it").build();
        String userId = UUID.randomUUID().toString().replaceAll("-", "");
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + CertConstants.DEFAULT_VALIDITY);
        KeyUsage keyUsage = new KeyUsage(KeyUsage.dataEncipherment); // 证书使用用途
        CertVO cert = certManagerService.createRootCert(userId, issuer, keyUsage, beginDate, endDate);
        System.out.println(cert.getUserId());
        System.out.println(cert.getIssuerKeyId());
        System.out.println(cert.getPkId());
        // 导出证书
        certManagerService.exportCertToPemFile(cert.getPkId(), basePATH + "/root.crt");
        // 私钥ID
        String userKeyId = cert.getIssuerKeyId();
        CertKeyVO keyVo = certManagerService.queryCertKey(userKeyId);
        PrivateKey privateKey = KeyUtils.getRSAPrivateKey(keyVo.getKeyPem());
        CertUtils.writeToPKCS8File(privateKey, basePATH + "/root_pri.key");
    }

    // 生成私钥
    @Test
    public void testImportKey() throws Exception {
        String userId = UUID.randomUUID().toString().replaceAll("-", "");
        // 生成公私钥 算法为RSA
        KeyPair keyPair = KeyUtils.generateKeyPair();
        CertUtils.writeKey(keyPair.getPrivate(), basePATH + "/welab_pri.key");
        CertUtils.writeKey(keyPair.getPublic(), basePATH + "/welab_pub.key");
        String pemPrivateKey = CertUtils.readPEMAsString(keyPair.getPrivate());
        // 保存私钥
        String certKeyId = certManagerService.importPrivateKey(userId, pemPrivateKey,
                KeyAlgorithmEnums.RSA.getKeyAlgorithm());
        System.out.println(certKeyId);
        System.out.println(userId);
    }

    // 生成csr
    @Test
    public void testCreateCertRequestByKey() throws Exception {
        String commonName = "wefe";
        X500NameInfo subject = X500NameInfo.builder().commonName(commonName).organizationName("welab")
                .organizationalUnitName("it").build();
        // 申请人私钥ID
        String subjectKeyId = "8f9556c985fc4838b9e5c2a89263a358";
        // 申请人用户ID
        String subjectUserId = "3db9399b780949c6ae18b3f55f2200e8";
        // issuer证书ID
        String issuerCertId = "419d830e12514d3db9f1ff0e6c7032f5";
        CertRequestVO vo = certManagerService.createCertRequestByKey(commonName, subjectUserId, subjectKeyId,
                issuerCertId, subject);
        System.out.println(vo.getPkId());
    }

    // 签发证书
    @Test
    public void testCreateChildCert() throws Exception {
        String userId = "3db9399b780949c6ae18b3f55f2200e8";
        String csrId = "7de5dae82ad14a21b6f6ed9cdc306f0e";
        CertVO cert = certManagerService.createChildCert("welab", userId, csrId);
        System.out.println(cert.getPkId());
    }

    // 一次性签发证书
    @Test
    public void testOnceCreateChildCert() throws Exception {
        String commonName = "yinlian1";
        // issuer签发机构证书ID
        String issuerCertId = "ffc4f9f92f9c4cfeb8fd49d189f7d15d";

        String userId = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println("为用户签发证书 userId=: " + userId);
        // 生成公私钥 算法为RSA
        KeyPair keyPair = KeyUtils.generateKeyPair();
        CertUtils.writeToPKCS8File(keyPair.getPrivate(), basePATH + "/" + commonName + "_pri.key");
        CertUtils.writePublicKey(keyPair.getPublic(), basePATH + "/" + commonName + "_pub.key");
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
        CertRequestVO vo = certManagerService.createCertRequestByKey(commonName, subjectUserId, subjectKeyId,
                issuerCertId, subject);
        System.out.println("为用户生成证书请求 csrId = " + vo.getPkId());
        PKCS10CertificationRequest cr = CertUtils.convertStrToCsr(vo.getCertRequestContent());
        CertUtils.writeCsr(cr, basePATH + "/" + commonName + ".csr");
        String csrId = vo.getPkId();
        CertVO cert = certManagerService.createChildCert(commonName, userId, csrId);
        System.out.println("为用户签发证书 : certId = " + cert.getPkId());

        certManagerService.exportCertToPemFile(cert.getPkId(), basePATH + "/" + commonName + ".crt");
        System.out.println("为用户导出证书 : file = " + basePATH + "/" + commonName + ".crt");

        List<X509Certificate> list = new ArrayList<>();
        X509Certificate certificate = CertUtils.convertStrToCert(cert.getCertContent());
        list.add(certificate);
        // 导出证书到p12/pfx/jks, 参数分别为：证书别名，私钥，keyStore密码，证书信息，保存路径
        CertUtils.savePfx(commonName, keyPair.getPrivate(), commonName, list, basePATH + "/" + commonName + ".jks");
        // 导入到jks供Java使用
        // keytool -import -noprompt -file root.crt -alias root -keystore mytrust.jks
        // -storepass 123456
        // keytool -import -noprompt -file welab.crt -alias welab1 -keystore mytrust.jks
        // -storepass 123456
        // crt -> p12
        // openssl pkcs12 -export -in yinlian.crt -inkey yinlian_pri.key -out
        // yinlian.p12 -name "yinlian"
    }

    // 通过私钥将证书转为p12/pfx/jks
    @Test
    public void cert2P12() throws Exception {
        // 私钥ID
        String userKeyId = "7f4075ec23c2462e80f1d038cfa0f9b8";
        // 证书ID
        String certId = "41550ac12b904bf3b0c4a4f8af6f4a69";
        CertKeyVO keyVo = certManagerService.queryCertKey(userKeyId);
        CertVO certVO = certManagerService.queryCertInfoByCertId(certId);

        PrivateKey privateKey = KeyUtils.getRSAPrivateKey(keyVo.getKeyPem());
        X509Certificate cert = CertUtils.convertStrToCert(certVO.getCertContent());

        List<X509Certificate> list = new ArrayList<>();
        list.add(cert);

        CertUtils.savePfx("yinlian", privateKey, "yinlian", list, basePATH + "/yinlian.jks");
        // 从pfx中导出私钥信息
        PrivateKey key = CertUtils.readPriKeyFromJks(basePATH + "/yinlian1.p12", "yinlian");
//         在控制台输出导出私钥的BASE64编码信息
        System.out.println(Base64.toBase64String(key.getEncoded()));
    }

    // 导出私钥
    @Test
    public void exportKey() throws Exception {
        // 私钥ID
        String userKeyId = "120c5633b404497690b33a8aa5143b5b";
        CertKeyVO keyVo = certManagerService.queryCertKey(userKeyId);
        PrivateKey privateKey = KeyUtils.getRSAPrivateKey(keyVo.getKeyPem());
        CertUtils.writeToPKCS8File(privateKey, basePATH + "/welab1_pri.key");
    }

    // 将证书导入到jks中
    // 如果重复导入也不会报错
    @Test
    public void testImportCertToTrust()
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
//         证书ID
        String rootCertId = "f3a76f8b1ac8414ebf3872be01dd8a17";
        CertVO rootCertVO = certManagerService.queryCertInfoByCertId(rootCertId);
        X509Certificate rootCert = CertUtils.convertStrToCert(rootCertVO.getCertContent());
        CertUtils.importCertToTrustStore("root", rootCert, basePATH + "/truststore.jks", "123456");
        String welabCertId = "9128d83224b84d70b906d84a6d688b94";
        CertVO welabCertVO = certManagerService.queryCertInfoByCertId(welabCertId);
        X509Certificate welabCert = CertUtils.convertStrToCert(welabCertVO.getCertContent());
        CertUtils.importCertToTrustStore("root", welabCert, basePATH + "/truststore.jks", "123456");
    }

    // 导出证书
    @Test
    public void testExportCertToFile() throws Exception {
        String certId = "9c29d8f181b34d3f8eb5cb07ca6e43c6";
        FileOperationUtils.mkdir(basePATH);
//        certManagerService.exportCertToPemFile(certId, basePATH + "/yinlian2.crt");
        certManagerService.exportCertToDerFile(certId, basePATH + "/yinlian2.cer");
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
        List<CertVO> list = certManagerService.queryCertList(userId, null, null, null);
        list.stream().forEach(System.out::println);
    }

    @Test
    public void testQueryCertRequestList() {
        String userId = "d84194f4ba1d40a2a06b7c63132c8ea9";
        List<CertRequestVO> list = certManagerService.queryCertRequestList(userId, null, 0, 100);
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
