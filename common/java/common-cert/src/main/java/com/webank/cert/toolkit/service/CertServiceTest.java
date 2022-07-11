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
package com.webank.cert.toolkit.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;
import org.web3j.utils.Numeric;

import com.webank.cert.toolkit.model.X500NameInfo;
import com.webank.cert.toolkit.utils.CertUtils;
import com.webank.cert.toolkit.utils.KeyUtils;

public class CertServiceTest {

    private CertService certService = new CertService();

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }
    private static final String SIGNATURE_ALGORITHM = "SHA256WITHRSA"; // SHA256WITHECDSA
    private static final String SIGNATURE_SM2 = "SM3WITHSM2";

    private String caKey = "MIIEowIBAAKCAQEAp5hBxXPHSlY5KtuBWExboMLboVzDPtlypJ6cEeAT8o8mFFUG\n"
            + "LDgSjtTD+zjFYOaLg1d83GcLl0SDBQoaTdZQzoQ7HQHirSyk25kaHR3vdaVSBLE6\n"
            + "n25lCaenfoqSLEyYfkKipyB4ZXG99M+rtDf3+JZXW5ELEvlJ9IHHV77pn84Inp1x\n"
            + "Gu6k+2tQoE1Yw0cp9yaPhP3qJLj5+kpPgCiS2fqpgamCE9e97ulEZvioHHGGyffb\n"
            + "Bmb3k7ATvXLkow127miaxWHFi/QEJ5h40ByDwWozF5wSe03Ur53wJ1/uTOrt+900\n"
            + "ek8j7JxzfJeEUzi5J+YWen8xNsHQSbvHr5405wIDAQABAoIBAQChptWBy4vlYsdf\n"
            + "VDwtW/FhXbIUsXHNtFXE/QvSngz/gB2drOj4S9lylTy3m2mebqEQvZk8ydO/QyER\n"
            + "Ak6e12I34AlYkFIA8TfObJ1oilBHbH9w8TV3RDcGmgSPpL8bJYJM/p7+ju4yAwTT\n"
            + "FmAqw3VV/EUkmAFTespfobISK54SOUYw5zoJQyOspj/lBlF4i+KKDcTDx6Riuwa7\n"
            + "wLngsYu1n6JBoFBm1e1H1g5SVye4zlQf9gdVzf117N/Xa69nTUPOaHkc2zwnwqI3\n"
            + "z6DIf+uKKGkllarc2ZEh/I4aR2EklusOJxk5baUkvgItMSq2aHbw2X2LKmr8tDB/\n"
            + "5cWN62CBAoGBAPZe+nCUWzSlSgNOOaBYSkDezuQzZnPEURljhqeFcqdlWxwjYnDj\n"
            + "hvDvVRh2fGtw48MuJbYNsrQ9zPrdaB9lT5UnYSIuGnhKh8e13L1g+Zv39UTghwkP\n"
            + "8x7glz9YYsu5wMB/+uohLNsU9fdIZsXp/rrXAkShZd3voEONIW6kyVinAoGBAK4l\n"
            + "GMEYgn+ulxWOzILAIi897nO8rtM1aVNDkklbrWSufENDlDpVhyUCcuCqBmM3Xm0f\n"
            + "wCZjvTIg1E3fqtf0kj+78JAzLqigja75kHIMFFaPkKiEpAFMIwFY7Vi+BbHTEwvx\n"
            + "dQVwTrJF910yoka5mBbP24UkZaKTlRwm5ZyMt4nBAoGAHSVFSVYzp/m51MRHSvHG\n"
            + "7v+syBBQmXdrBK7ieiTuWGFEMwL4nVQ9XXlivr8dnvQ+7ZMjAPOD5ZC+FBtnOveY\n"
            + "P5PmCM4dcYeXooegMoMrZEkkKd7J+sd5QnjdS7AgF+vEosFBJLuB+/Tx2CwnhFhX\n"
            + "OzE+YnIZg/TaJ8OlZdp2u1cCgYBMFArPePyx+T0p/tubl4KXru+4gkrCHMhpxtBm\n"
            + "2fVTUeMZo7FjFrBW284CFmV5/Nt0wvU4EES8XJlDeB5z//XQgDOlW6bbpmCfe4m+\n"
            + "OUa9VjT1WhUoN/HnCcmPBl0IhdUBV7gu6xSGT4i0n4VDbptiA+a8MN1x/BWdWeTf\n"
            + "0p3wQQKBgAlJYogOcZku4B0NIIvH6MGZekr/5p1jeOeO0SBOVSNPaZdtnMqnZBPm\n"
            + "IdWWk5Pykg9FJLJpjobhURO3J4T3Y9SC6lf6fX2Kib2jk843s+4KAlEQhblMOHpl\n"
            + "ZXo/AB1yvdkyYN3uRgQ1cWAP9D5eNtKaIzL3MQoJnMSXDJGDPquV";

    // 根证书生成
    @Test
    public void testGenerateKPAndRootCert() throws Exception {
        // 签发机构
        X500NameInfo issuer = X500NameInfo.builder().commonName("wefe_root") // CN 常用名
                .organizationName("tianmian") // O 组织名称
                .organizationalUnitName("welab") // OU 组织单位名称
                .countryName("CN").stateOrProvinceName("GDSZ").build();
        // 自动生成私钥（默认为RSA），并写入指定路径
        // 自动生成公私钥KP以及根证书
        certService.generateKPAndRootCert(issuer, "out1", "root");

        // openssl pkcs12 -export -in root.crt -inkey root_pri.key -out root.p12 -name
        // "root1"
        // root123
    }

    // 指定私钥生成根证书
//    @Test
    public void testGenerateRootCertByDefaultConf() throws Exception {
        // 签发机构
        X500NameInfo issuer = X500NameInfo.builder().commonName("chain").organizationName("fisco-bcos")
                .organizationalUnitName("chain").build();
        String caStr = certService.generateRootCertByDefaultConf(issuer, caKey);
        System.out.println(caStr);
    }

    // 详细的配置 用来生成根证书
//    @Test
    public void testCreateRootCertificate() throws Exception {
        // 签发机构
        X500NameInfo info = X500NameInfo.builder().commonName("chain").organizationName("fisco-bcos")
                .organizationalUnitName("chain").build();
        // 自动生成公私钥
        KeyPair keyPair = KeyUtils.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + 3650 * 24L * 60L * 60L * 1000); // 过期时间
        // 签名算法
        X509Certificate certificate = certService.createRootCertificate(SIGNATURE_ALGORITHM, info, null, beginDate,
                endDate, publicKey, privateKey);
        certificate.verify(publicKey);
        new File("out").mkdirs();
        CertUtils.writeCrt(certificate, "dist/ca.crt");
    }

    // 详细的配置 用来生成根证书
//    @Test
    public void testCreateGMRootCertificate() throws Exception {
        X500NameInfo info = X500NameInfo.builder().commonName("chain").organizationName("fisco-bcos")
                .organizationalUnitName("chain").build();

        KeyPair keyPair = KeyUtils.generateSM2KeyPair();

        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + 3650 * 24L * 60L * 60L * 1000);
        X509Certificate certificate = certService.createRootCertificate(SIGNATURE_SM2, info, null, beginDate, endDate,
                publicKey, privateKey);
        certificate.verify(publicKey);
        new File("out").mkdirs();
        CertUtils.writeKey(privateKey, "out/ca.key");
        CertUtils.writeCrt(certificate, "out/ca.crt");
    }

    // csr 全称为Certificate Signing Request，即证书请求文件
    // csr 证书申请请求 生成
//    @Test
    public void testGenerateCertRequest() {
        CertService certService = new CertService();
        // 申请人信息
        X500NameInfo subject = X500NameInfo.builder().commonName("client1") // 常用名
                .organizationName("client").organizationalUnitName("client").countryName("CN")
                .stateOrProvinceName("GDSZ")
//                .emailAddress("")
//                .localityName("")
//                .streetAddress("")
                .build();
        // 自动生成RSA私钥，KeyUtils为证书组件密钥工具类
        KeyPair keyPair = KeyUtils.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        CertUtils.writeKey(privateKey, "out1/client1_pri.key");
        CertUtils.writeKey(publicKey, "out1/client1_pub.key");
        // CertUtils工具提供了证书读写解析的相关能力
        String priStr = CertUtils.readPEMAsString(privateKey);
        String csrStr = certService.generateCertRequestByDefaultConf(subject, priStr, "out1/", "client1");
        System.out.println(csrStr);
    }

    // 通过根证书和其私钥对子证书申请进行签发
    // 根证书通过其私钥对请求文件签名来颁发子证书
    // 以文件路径为入参示例
//    @Test
    public void testGenerateChildCertByDefaultConf() throws Exception {
//        String caStr = ""; // 根证书
//        String caKey = ""; // 私钥
//        String csrStr = ""; // csr
//        String childStr = certService.generateChildCertByDefaultConf(caStr,csrStr,caKey);
//        System.out.println(childStr);
        // 根证书，请求文件，私钥，子证书存放路径与名称
        String childStr2 = certService.generateChildCertByDefaultConf("out1/root.crt", "out1/client1.csr",
                "out1/root_pri.key", "out1", "client1");
        System.out.println(childStr2);

        // openssl pkcs12 -export -in subject.crt -inkey subject_pri.key -out
        // subject.p12 -name "subject1"
        // subject1
    }

    // 暂时没用
//    @Test
    public void testGenerateCertRequestByDefaultConf() throws Exception {
        X500NameInfo info = X500NameInfo.builder().commonName("agency").organizationalUnitName("agency")
                .organizationName("fisco-bcos").build();
        KeyPair keyPair = KeyUtils.generateKeyPair();
        String hex = Numeric.toHexString(keyPair.getPrivate().getEncoded());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Numeric.hexStringToByteArray(hex));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
//        String csrStr = certService.generateCertRequestByDefaultConf(info,
//               "asdasd");
        String csrStr = certService.generateCertRequestByDefaultConf(info,
                CertUtils.readPEMAsString(keyPair.getPrivate()));
        System.out.println(csrStr);
    }

//    @Test
    public void testCreateCertRequest() throws OperatorCreationException, FileNotFoundException {
        X500NameInfo info = X500NameInfo.builder().commonName("agency").organizationalUnitName("agency")
                .organizationName("fisco-bcos").build();
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
//        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
//        keyPairGenerator.initialize(ecGenParameterSpec, SECURE_RANDOM);
//        KeyPair keyPair = keyPairGenerator.generateKeyPair();
//        PublicKey publicKey = keyPair.getPublic();
//        PrivateKey privateKey = keyPair.getPrivate();

        KeyPair keyPair = KeyUtils.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        CertUtils.writeKey(privateKey, "out/agency.key");
        PKCS10CertificationRequest request = certService.createCertRequest(info, publicKey, privateKey,
                SIGNATURE_ALGORITHM);
        CertUtils.writeCsr(request, "out/agency.csr");
        CertUtils.readCsr("out/agency.csr");
    }

//    @Test
    public void testCreateChildCertificate() throws Exception {
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + 3650 * 24L * 60L * 60L * 1000);

//        PKCS10CertificationRequest request = CertUtils.readCsr("out/agency.csr");
//        X509Certificate parentCert = CertUtils.readCrt("out/ca.crt");
//        PEMKeyPair pemKeyPair=  CertUtils.readKey("out/ca.key");
//        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(
//                new PKCS8EncodedKeySpec(pemKeyPair.getPrivateKeyInfo().getEncoded()));
//        X509Certificate childCert = certService.createChildCertificate(true,SIGNATURE_ALGORITHM, parentCert,
//                request,null, beginDate, endDate,privateKey);
//        childCert.verify(parentCert.getPublicKey());
    }

    // 撤销证书
//    @Test
    public void testRevoke() {
        CertService certService = new CertService();
        try {
            // 从文件中读取证书（上述步骤中生成的证书路径）
            X509Certificate root = CertUtils.readCrt("dist/company.crt"); // 根证书
            X509Certificate child = CertUtils.readCrt("dist/certChain.crt"); // 子证书
            // 从文件中读取私钥（上述步骤中生成的私钥路径）
            PrivateKey caPrivateKey = (PrivateKey) CertUtils.readRSAKey("dist/company_pri.key"); // 私钥
            List<X509Certificate> revokeCertificates = new ArrayList<>();
            revokeCertificates.add(child);// 添加撤销列表
            // 撤销上述步骤中签发的子证书
            X509CRL X509Crl = certService.createCRL(root, caPrivateKey, revokeCertificates, "SHA256WITHRSA");
            System.out.println("吊销证书路径：dist/certChain.crt");
            X509Crl.getRevokedCertificates().forEach(x509CRLEntry -> {
                System.out.println("吊销证书序列号：" + x509CRLEntry.getSerialNumber());
            });

            // 验证吊销证书后的证书链
            List<X509Certificate> certChain = new ArrayList<>();
            // 可添加多级证书...这里以上述步骤中生成的两个证书为例
            certChain.add(root);
            certChain.add(child);
            System.out.println("证书链验证结果 = " + certService.verify(root, certChain, X509Crl));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // PFX 证书读写
//    @Test
    public void readWritePFX() {
        try {
            List<X509Certificate> list = new ArrayList<>();
            list.add(CertUtils.readCrt("dist/company.crt"));
            // 生成pfx文件，参数分别为：证书别名，私钥，keyStore密码，证书信息，保存路径，证书名
            CertUtils.savePfx("fisco", (PrivateKey) CertUtils.readRSAKey("dist/company_pri.key"), "123", list, "dist/",
                    "ca");
            // 从pfx中导出私钥信息
            PrivateKey key = CertUtils.readPriKeyFromPfx("dist/ca.pfx", "123");
            // 在控制台输出导出私钥的BASE64编码信息
            System.out.println(Base64.toBase64String(key.getEncoded()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
