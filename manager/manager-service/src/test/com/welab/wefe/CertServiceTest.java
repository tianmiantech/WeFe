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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.Test;

import com.webank.cert.toolkit.model.X500NameInfo;
import com.webank.cert.toolkit.service.CertService;
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

    // 根证书生成
    @Test
    public void testGenerateKPAndRootCert() throws Exception {
        // 签发机构
        X500NameInfo issuer = X500NameInfo.builder().commonName("root") // CN 常用名
                .organizationName("Welab") // O 组织名称
                .organizationalUnitName("IT") // OU 组织单位名称
                .countryName("CN").stateOrProvinceName("GuangDong").localityName("ShenZhen").build();
        // 自动生成私钥（默认为RSA），并写入指定路径
        // 自动生成公私钥KP以及根证书
        certService.generateKPAndRootCert(issuer, "out1", "root");
        // 导入到jks供Java使用
        // keytool -import -noprompt -file root.crt -alias root -keystore mytrust.jks
        // -storepass 123456
    }

    // csr 全称为Certificate Signing Request，即证书请求文件
    // csr 证书申请请求 生成
    // 生成二级证书csr
    @Test
    public void testGenerateCertRequest() {
        CertService certService = new CertService();
        // 申请人信息
        X500NameInfo subject = X500NameInfo.builder().commonName("welab") // CN 常用名
                .organizationName("Welab") // O 组织名称
                .organizationalUnitName("IT") // OU 组织单位名称
                .countryName("CN").stateOrProvinceName("GuangDong").localityName("ShenZhen").build();
        // 自动生成RSA私钥，KeyUtils为证书组件密钥工具类
        KeyPair keyPair = KeyUtils.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        CertUtils.writeKey(privateKey, "out1/welab1_pri.key");
        CertUtils.writeKey(publicKey, "out1/welab1_pub.key");
        // CertUtils工具提供了证书读写解析的相关能力
        String priStr = CertUtils.readPEMAsString(privateKey);
        String csrStr = certService.generateCertRequestByDefaultConf(subject, priStr, "out1/", "welab1");
        System.out.println(csrStr);
    }

    // 通过根证书和其私钥对子证书申请进行签发
    // 根证书通过其私钥对请求文件签名来颁发子证书
    // 以文件路径为入参示例
    // 生成二级证书
    @Test
    public void testGenerateChildCertByDefaultConf() throws Exception {
        // 根证书，请求文件，私钥，子证书存放路径与名称
        String childStr2 = certService.generateChildCertByDefaultConf("out1/root.crt", "out1/welab1.csr",
                "out1/root_pri.key", "out1", "welab1");
        System.out.println(childStr2);
        // keytool -import -noprompt -file welab1.crt -alias welab1 -keystore
        // mytrust.jks -storepass 123456
    }

    @Test
    public void testCreateCertRequest() throws OperatorCreationException, FileNotFoundException {
        X500NameInfo info = X500NameInfo.builder().commonName("yinlian") // CN 常用名
                .organizationName("yinlian") // O 组织名称
                .organizationalUnitName("IT") // OU 组织单位名称
                .countryName("CN").stateOrProvinceName("GuangDong").localityName("ShenZhen").build();
        KeyPair keyPair = KeyUtils.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        CertUtils.writeKey(privateKey, "out1/yinlian_pri.key");
        CertUtils.writeKey(publicKey, "out1/yinlian_pub.key");
        PKCS10CertificationRequest request = certService.createCertRequest(info, publicKey, privateKey,
                SIGNATURE_ALGORITHM);
        CertUtils.writeCsr(request, "out1/yinlian.csr");
    }

    @Test
    public void testCreateChildCertificate() throws Exception {
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + 3650 * 24L * 60L * 60L * 1000);
        PKCS10CertificationRequest request = CertUtils.readCsr("out1/yinlian.csr");
        X509Certificate parentCert = CertUtils.readCrt("out1/welab1.crt");
        PEMKeyPair pemKeyPair = CertUtils.readKey("out1/welab1_pri.key");
        PrivateKey privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(pemKeyPair.getPrivateKeyInfo().getEncoded()));
        X509Certificate childCert = certService.createChildCertificate(true, SIGNATURE_ALGORITHM, parentCert, request,
                null, beginDate, endDate, privateKey);
        childCert.verify(parentCert.getPublicKey());
        CertUtils.writeCrt(childCert, "out1/yinlian.crt");
        // crt -> p12
        // openssl pkcs12 -export -in yinlian.crt -inkey yinlian_pri.key -out
        // yinlian.p12 -name "yinlian"
        // yinlian1
    }

    @Test
    public void test() throws CertificateException {
        String str = "-----BEGIN CERTIFICATE-----\nMIIDLjCCAhagAwIBAgIJAIpEr7/Bjx+LMA0GCSqGSIb3DQEBCwUAMDoxFjAUBgNV\nBAMMDVdlbGFiIFJvb3QgQ0ExEzARBgNVBAoMCldlbGFiIEluYy4xCzAJBgNVBAsM\nAklUMB4XDTIyMDgxNzAyMzYwOFoXDTMyMDgxNDAyMzYwOFowOjEWMBQGA1UEAwwN\nV2VsYWIgUm9vdCBDQTETMBEGA1UECgwKV2VsYWIgSW5jLjELMAkGA1UECwwCSVQw\nggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCH6vi+VosXUlixlRphvN5q\nOcXhvb5T1ZNzbYZFhJtkJ53/mo4Smh5iXuOTfyA+4y0MaZ1yZhzoFpxWGHcRcK7D\n4yJ9965vdRBq0Ei8pIHjWCSCN+MHOQl9kST9+b8SJ+ceiMbvaFv0EgPbgAl7irdW\nl2D0yb3EUvOpknIEJpIcFee0c3wzRGzzsGZd2V9li/YHldwyIA9Gek26u28XT/4P\nBKJc6i3vcD5XLBULPil+KcLJz31NTYsP50UKi+dmezUi+XuEJk1KO8YFRThqRg+u\nmDaNcwDW94fLyQ3A5DbhvIY+Ix0kSOGZjPEdCtM1S/HdIoUBGGBDZMunJGcuYPUB\nAgMBAAGjNzA1MAwGA1UdEwQFMAMBAf8wJQYDVR0RBB4wHIIad2VmZS50aWFubWlh\nbnRlY2guY29tLnRlc3QwDQYJKoZIhvcNAQELBQADggEBAEWc0vYnm1SVJU+X2C/E\nEXZmllJ1UdnCTxivW0DoXLfKY09T1YH2lf+nMq0Imuz7FNRCI9ueXrUZUlxluBkL\nB1lKikBcGLkSwGDsPPWjtV7k5oK8vNdTK44OMwBtcx/KtNnyN+pHjBmvmOI+WzVV\n4yh7Jkq1JMerBueIt6DnsnvbY8+NJbOo9ER0BFI/B3MVujsEYw8yE7WyNDvU1vmR\nAPhlX31BFF7woX30bJJ5p7BMD4dP2GmVjj88NYy9r90iVnVmHE3z5H0qdNvuQ7Dw\nv4mdKQJOhdhrC4K1MrTGEpTZ7psOz7MHoH7L9C+CDsNrZMtc7wRmn57NN08seR32\nA8s=\n-----END CERTIFICATE-----";
        X509Certificate cert = CertUtils.convertStrToCert(str);
        System.out.println(cert);
    }
    @Test
    public void testImportCertToTrust()
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        X509Certificate rootCert = CertUtils.readCrt("out1/root.crt");
        CertUtils.importCertToTrustStore("root", rootCert, "out1/truststore1.jks", "123456");
        X509Certificate welab1Cert = CertUtils.readCrt("out1/welab1.crt");
        CertUtils.importCertToTrustStore("welab1", welab1Cert, "out1/truststore1.jks", "123456");
    }
}
