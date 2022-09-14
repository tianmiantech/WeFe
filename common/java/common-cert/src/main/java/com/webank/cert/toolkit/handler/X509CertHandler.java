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
package com.webank.cert.toolkit.handler;


import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

/**
 * X509CertHandler
 *
 * @author wesleywang
 */
public class X509CertHandler {


    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static X509Certificate createRootCert(String signAlg, X500Name subject, KeyUsage keyUsage,
                                                 Date startDate, Date endDate,
                                                 PublicKey publicKey, PrivateKey privateKey) throws Exception {

        return createCert(true, signAlg, subject, subject, keyUsage,
                publicKey, privateKey, startDate, endDate);
    }

    public static X509Certificate createChildCert(boolean isCaCert, String signAlg, X509Certificate parentCertificate,
                                                  PKCS10CertificationRequest request, KeyUsage keyUsage,
                                                  Date startDate, Date endDate, PrivateKey privateKey)
            throws Exception {

        JcaPKCS10CertificationRequest jcaRequest = new JcaPKCS10CertificationRequest(request);
        PublicKey publicKey = jcaRequest.getPublicKey();
        return createCert(isCaCert, signAlg, request.getSubject(),
                X500Name.getInstance(parentCertificate.getSubjectX500Principal().getEncoded()), keyUsage, publicKey,
                privateKey, startDate, endDate);
    }


    public static X509Certificate createCert(boolean isCaCert, String signAlg, X500Name subject, X500Name issuer,
                                             KeyUsage keyUsage, PublicKey publicKey, PrivateKey privateKey,
                                             Date startDate, Date endDate)
            throws Exception {

        X509v3CertificateBuilder v3CertGen = new JcaX509v3CertificateBuilder(issuer,
                BigInteger.probablePrime(64, new Random()),
                startDate, endDate, subject, publicKey);

        buildCertExtension(v3CertGen, isCaCert, keyUsage);
        JcaContentSignerBuilder contentSignerBuilder = makeContentSignerBuilder(signAlg);
        X509Certificate cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(v3CertGen.build(contentSignerBuilder.build(privateKey)));
        return cert;
    }


    private static void buildCertExtension(X509v3CertificateBuilder v3CertGen,  boolean isCaCert, KeyUsage keyUsage)
            throws CertIOException {
        /*
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        v3CertGen.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded())));
        v3CertGen.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded())));
         */
        v3CertGen.addExtension(Extension.basicConstraints, false, new BasicConstraints(isCaCert));
//        KeyUsage ku2 = new KeyUsage(KeyUsage.digitalSignature); // 数字签名
//        v3CertGen.addExtension(Extension.keyUsage, false, ku2);
//        if (keyUsage != null) {
//            KeyUsage ku1 = new KeyUsage(KeyUsage.keyEncipherment); // 密钥加密
//            KeyUsage ku3 = new KeyUsage(KeyUsage.nonRepudiation); // 认可签名
//            v3CertGen.addExtension(Extension.keyUsage, false, ku1);
//            v3CertGen.addExtension(Extension.keyUsage, false, ku3);
//        }
        if (keyUsage != null) {
            v3CertGen.addExtension(Extension.keyUsage, false, keyUsage);
        } else {
            KeyUsage ku2 = new KeyUsage(KeyUsage.digitalSignature); // 数字签名
            v3CertGen.addExtension(Extension.keyUsage, false, ku2);
        }
        List<GeneralName> namesList = new ArrayList<GeneralName>();
        namesList.add(new GeneralName(GeneralName.dNSName, "wefe.tianmiantech.com.test"));
//        namesList.add(new GeneralName(GeneralName.iPAddress, "127.0.0.1"));
        
        GeneralNames subjectAltNames = new GeneralNames((GeneralName[])namesList.toArray(new GeneralName [] {}));
        v3CertGen.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);
    }


    public static PKCS10CertificationRequest createCSR(X500Name subject, PublicKey pubKey, PrivateKey priKey, String signAlg)
            throws OperatorCreationException {
        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(subject, pubKey);
        ContentSigner signerBuilder = new JcaContentSignerBuilder(signAlg)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(priKey);
        return csrBuilder.build(signerBuilder);
    }

    private static JcaContentSignerBuilder makeContentSignerBuilder(String signAlg) {
        JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(signAlg);
        contentSignerBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
        return contentSignerBuilder;
    }

    public static X509CRL revokeCert(X509Certificate caCertificate,
                                     PrivateKey caPrivateKey,
                                     List<X509Certificate> revokeCertificates,
                                     String signAlg,
                                     int reason,
                                     Date period)
            throws CRLException, OperatorCreationException {

        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(
                new X500Name(caCertificate.getSubjectDN().getName()),
                new Date()
        );
        if (period != null) {
            crlBuilder.setNextUpdate(period);
        }
        revokeCertificates.forEach(revokeCertificate -> {
            crlBuilder.addCRLEntry(revokeCertificate.getSerialNumber(), new Date() , reason);
        });
        JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(signAlg);
        contentSignerBuilder.setProvider("BC");
        X509CRLHolder crlHolder = crlBuilder.build(contentSignerBuilder.build(caPrivateKey));
        JcaX509CRLConverter converter = new JcaX509CRLConverter();
        converter.setProvider("BC");
        return converter.getCRL(crlHolder);
    }

}
