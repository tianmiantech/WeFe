package com.webank.cert.toolkit.handler;


import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
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

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
        if (keyUsage != null) {
            v3CertGen.addExtension(Extension.keyUsage, false, keyUsage);
        }
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
