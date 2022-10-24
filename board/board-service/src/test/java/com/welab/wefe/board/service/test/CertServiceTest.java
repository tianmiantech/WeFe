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
package com.welab.wefe.board.service.test;

import java.io.FileNotFoundException;
import java.io.IOException;
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
        CertUtils.writeToPKCS8File(privateKey, "out1/welab1_pri.key");
        CertUtils.writePublicKey(publicKey, "out1/welab1_pub.key");
        // CertUtils工具提供了证书读写解析的相关能力
        String priStr = CertUtils.readPEMAsString(privateKey);
        String csrStr = certService.generateCertRequestByDefaultConf(subject, priStr, "out1/", "welab1");
        System.out.println(csrStr);
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
        CertUtils.writeToPKCS8File(privateKey, "out1/yinlian_pri.key");
        CertUtils.writePublicKey(publicKey, "out1/yinlian_pub.key");
        PKCS10CertificationRequest request = certService.createCertRequest(info, publicKey, privateKey,
                SIGNATURE_ALGORITHM);
        CertUtils.writeCsr(request, "out1/yinlian.csr");
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
