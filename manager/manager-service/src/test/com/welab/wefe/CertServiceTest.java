
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
        X500NameInfo issuer = X500NameInfo.builder().commonName("welab") // CN 常用名
                .organizationName("汇立集团") // O 组织名称
                .organizationalUnitName("welab inc") // OU 组织单位名称
                .countryName("CN").stateOrProvinceName("GDSZ").build();
        // 自动生成私钥（默认为RSA），并写入指定路径
        // 自动生成公私钥KP以及根证书
        certService.generateKPAndRootCert(issuer, "out1", "root");
        // 导入到jks供Java使用
        // keytool -import -noprompt -file root.crt -alias root -keystore mytrust.jks -storepass 123456
    }

    // csr 全称为Certificate Signing Request，即证书请求文件
    // csr 证书申请请求 生成
    // 生成二级证书csr
    @Test
    public void testGenerateCertRequest() {
        CertService certService = new CertService();
        // 申请人信息
        X500NameInfo subject = X500NameInfo.builder().commonName("welab1") // 常用名
                .organizationName("汇立集团").organizationalUnitName("welab inc").countryName("CN")
                .stateOrProvinceName("GDSZ").build();
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
        // keytool -import -noprompt -file welab1.crt -alias welab1 -keystore mytrust.jks -storepass 123456
    }

    @Test
    public void testCreateCertRequest() throws OperatorCreationException, FileNotFoundException {
        X500NameInfo info = X500NameInfo.builder().commonName("yinlian").organizationalUnitName("银联商务")
                .organizationName("银联商务 inc").build();
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
        // openssl pkcs12 -export -in yinlian.crt -inkey yinlian_pri.key -out yinlian.p12 -name "yinlian"
        // yinlian1
    }

    @Test
    public void testImportCertToTrust() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        X509Certificate rootCert = CertUtils.readCrt("out1/root.crt");
        CertUtils.importCertToTrustStore("root", rootCert, "out1/truststore1.jks", "123456");
        X509Certificate welab1Cert = CertUtils.readCrt("out1/welab1.crt");
        CertUtils.importCertToTrustStore("welab1", welab1Cert, "out1/truststore1.jks", "123456");
    }
}
