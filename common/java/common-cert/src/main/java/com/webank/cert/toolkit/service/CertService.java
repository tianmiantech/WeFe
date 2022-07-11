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

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.cert.toolkit.constants.CertConstants;
import com.webank.cert.toolkit.handler.X509CertHandler;
import com.webank.cert.toolkit.model.X500NameInfo;
import com.webank.cert.toolkit.utils.CertUtils;
import com.webank.cert.toolkit.utils.FileOperationUtils;
import com.webank.cert.toolkit.utils.KeyUtils;


/**
 * CertService
 *
 * @author wesleywang
 */
public class CertService {

    protected static final Logger LOG = LoggerFactory.getLogger(CertService.class);
    
    /**
     * generate RSA keyPair and CA certificate by default configuration (signature algorithm is SHA256WITHRSA,
     * valid for 3650 days) , the generated certificate and key will be saved in file that specifies the path
     *
     * @param issuer   issuer information
     * @param savePath path of the generated keys and certificate
     */
    public void generateKPAndRootCert(X500NameInfo issuer, String savePath) {
        generateKPAndRootCert(issuer,savePath,"ca");
    }

    /**
     * generate RSA keyPair and CA certificate by default configuration (signature algorithm is SHA256WITHRSA,
     * valid for 3650 days) , the generated certificate and key will be saved in file that specifies the path
     *
     * @param issuer   issuer information
     * @param savePath path of the generated keys and certificate
     * @param fileName filename
     */
    public void generateKPAndRootCert(X500NameInfo issuer, String savePath, String fileName) {
        try {
            FileOperationUtils.mkdir(savePath);
            Date beginDate = new Date();
            Date endDate = new Date(beginDate.getTime() + CertConstants.DEFAULT_VALIDITY);
            KeyPair keyPair = KeyUtils.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            CertUtils.writeKey(privateKey, savePath + "/" + fileName + "_pri.key");
            LOG.info("privateKey save success, file path :~" + savePath + "/" + fileName + "_pri.key");
            CertUtils.writeKey(publicKey, savePath + "/" + fileName + "_pub.key");
            LOG.info("publicKey save success, file path :~" + savePath + "/" + fileName + "_pub.key");

            X509Certificate certificate = createRootCertificate(CertConstants.DEFAULT_SIGNATURE_ALGORITHM, issuer,
                    null, beginDate, endDate, publicKey, privateKey);
            CertUtils.writeCrt(certificate, savePath + "/" + fileName + ".crt");
            LOG.info("CA certificate save success, file path :~" + savePath + "/" + fileName + ".crt");
        } catch (Exception e) {
            LOG.error("generateKPAndRootCert failed ,", e);
        }
    }

    /**
     * generate CA certificate by default configuration (signature algorithm is SHA256WITHRSA, valid for 3650 days)
     *
     * @param issuer        issuer
     * @param privateKeyStr string of the privateKey
     * @return string of generated certificate
     */
    public String generateRootCertByDefaultConf(X500NameInfo issuer, String privateKeyStr) {
        return generateRootCertByDefaultConf(issuer, privateKeyStr, null,null);
    }

    /**
     * generate childCert by default configuration (signature algorithm is SHA256WITHRSA, valid for 3650 days)
     *
     * @param caStr     string of the CA certificate
     * @param csrStr    string of the certificate request
     * @param priKeyStr string of the parent's privateKey
     * @return string of generated certificate
     */
    public String generateChildCertByDefaultConf(String caStr, String csrStr, String priKeyStr) {
        return generateChildCertByDefaultConf(true, null, caStr, csrStr, priKeyStr);
    }


    /**
     * generate childCert by default configuration (signature algorithm is SHA256WITHRSA, valid for 3650 days)
     * the generated certificate is saved in a file
     *
     * @param caPath         CA certificate file path
     * @param csrPath        path of certificate request
     * @param keyPath        path of the parent's privateKey
     * @param exportFilePath file path of  generated certificate
     * @return string of generated certificate
     */
    public String generateChildCertByDefaultConf(String caPath, String csrPath, String keyPath, String exportFilePath, String certName) {
        return generateChildCertByDefaultConf(true, null, caPath, csrPath, keyPath, exportFilePath, certName );
    }

    /**
     * generate certRequest by default configuration (signature algorithm is SHA256WITHRSA, valid for 3650 days)
     *
     * @param subject subject of the csr
     * @param priKey  string of the child's privateKey
     * @return string of generated certRequest
     */
    public String generateCertRequestByDefaultConf(X500NameInfo subject, String priKey) {
        return generateCertRequestByDefaultConf(subject, priKey, null,null);
    }

    /**
     * generate CA certificate by default configuration (signature algorithm is SHA256WITHRSA, valid for 3650 days)
     * the generated certificate is saved in a file
     *
     * @param issuer        issuer
     * @param privateKeyStr string of the privateKey
     * @param certSavePath  save path of generated certificate
     * @return string of generated certificate
     */
    public String generateRootCertByDefaultConf(X500NameInfo issuer, String privateKeyStr, String certSavePath, String fileName) {
        try {
            if (privateKeyStr == null) {
                throw new NullPointerException("privateKeyStr is null");
            }
            if (certSavePath != null && !FileOperationUtils.exist(certSavePath)) {
                FileOperationUtils.mkdir(certSavePath);
            }
            Date beginDate = new Date();
            Date endDate = new Date(beginDate.getTime() + CertConstants.DEFAULT_VALIDITY);
            PrivateKey privateKey = null;
            PublicKey publicKey = null;
            try {
                privateKey = KeyUtils.getRSAPrivateKey(privateKeyStr);
                publicKey = KeyUtils.getRSAPublicKey(privateKey);
            } catch (Exception e) {
                LOG.error("KeyUtils.getRSAPrivateKey failed ", e);
            }
            if (privateKey == null || publicKey == null) {
                return null;
            }
            X509Certificate certificate = createRootCertificate(CertConstants.DEFAULT_SIGNATURE_ALGORITHM, issuer,
                    null, beginDate, endDate, publicKey, privateKey);
            if (certSavePath != null) {
                CertUtils.writeCrt(certificate, certSavePath + "/" + fileName +  ".crt");
                LOG.info("CA certificate save success, file path :~" + certSavePath + "/" + fileName + ".crt");
            }
            return CertUtils.readPEMAsString(certificate);
        } catch (Exception e) {
            LOG.error("generateRootCertByDefaultConf failed ,", e);
        }
        return null;
    }

    /**
     * generate certRequest by default configuration (signature algorithm is SHA256WITHRSA, valid for 3650 days)
     *
     * @param subject        subject of the csr
     * @param priKey         string of the child's privateKey
     * @param exportFilePath save path of generated certRequest
     * @return string of generated certRequest
     */
    public String generateCertRequestByDefaultConf(X500NameInfo subject, String priKey, String exportFilePath, String csrName) {
        try {
            if (exportFilePath != null && !FileOperationUtils.exist(exportFilePath)) {
                FileOperationUtils.mkdir(exportFilePath);
            }
            PrivateKey privateKey = null;
            PublicKey publicKey = null;
            privateKey = KeyUtils.getRSAPrivateKey(priKey);
            publicKey = KeyUtils.getRSAPublicKey(privateKey);
            PKCS10CertificationRequest request = createCertRequest(subject, publicKey, privateKey,
                    CertConstants.DEFAULT_SIGNATURE_ALGORITHM);
            if (exportFilePath != null) {
                CertUtils.writeCsr(request, exportFilePath + "/" + csrName + ".csr");
                LOG.info("PKCS10CertificationRequest save success, file path :~" + exportFilePath);
            }
            return CertUtils.readPEMAsString(request);
        } catch (Exception e) {
            LOG.error("Error generate csr", e);
        }
        return null;
    }


    /**
     * generate childCert by default configuration (signature algorithm is SHA256WITHRSA, valid for 3650 days)
     *
     * @param isCaCert  certificate mark
     * @param keyUsage  scenarios where the certificate can be used
     * @param caStr     string of the CA certificate
     * @param csrStr    string of the certificate request
     * @param priKeyStr string of the parent's privateKey
     * @return string of the generated certificate
     */
    public String generateChildCertByDefaultConf(boolean isCaCert, KeyUsage keyUsage, String caStr, String csrStr,
                                                 String priKeyStr) {
        if (caStr == null || csrStr == null || priKeyStr == null) {
            throw new NullPointerException("param null");
        }
        X509Certificate parentCert = null;
        PKCS10CertificationRequest request = null;
        PrivateKey parentPriKey = null;
        try {
            parentCert = CertUtils.convertStrToCert(caStr);
            request = CertUtils.convertStrToCsr(csrStr);
            parentPriKey = KeyUtils.getRSAPrivateKey(priKeyStr);
        } catch (Exception e) {
            LOG.error("string convert pemObject failed ", e);
        }
        if (parentCert == null) {
            throw new RuntimeException("caStr can not convert to certificate");
        }
        if (request == null) {
            throw new RuntimeException("csrStr can not convert to csr");
        }
        if (parentPriKey == null) {
            throw new RuntimeException("caStr can not convert to key");
        }
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + CertConstants.DEFAULT_VALIDITY);
        X509Certificate childCert = createChildCertificate(isCaCert, CertConstants.DEFAULT_SIGNATURE_ALGORITHM,
                parentCert, request, keyUsage, beginDate, endDate, parentPriKey);
        return CertUtils.readPEMAsString(childCert);
    }

    /**
     * generate childCert by default configuration (signature algorithm is SHA256WITHRSA, valid for 3650 days)
     *
     * @param isCaCert       certificate mark
     * @param keyUsage       scenarios where the certificate can be used
     * @param caPath         path of CA certificate
     * @param csrPth         path of certificate request
     * @param keyPath        path of the parent's privateKey
     * @param exportFilePath save path of generated certificate
     * @return string of the generated certificate
     */
    public String generateChildCertByDefaultConf(boolean isCaCert, KeyUsage keyUsage, String caPath, String csrPth,
                                                 String keyPath, String exportFilePath, String certName) {
        try {
            if (!FileOperationUtils.exist(caPath)) {
                throw new FileNotFoundException("caPath does't exist, path = " + caPath);
            }
            if (!FileOperationUtils.exist(csrPth)) {
                throw new FileNotFoundException("csrPth does't exist, path = " + csrPth);
            }
            if (!FileOperationUtils.exist(keyPath)) {
                throw new FileNotFoundException("keyPath does't exist, path = " + keyPath);
            }
            if (exportFilePath != null && !FileOperationUtils.exist(exportFilePath)) {
                FileOperationUtils.mkdir(exportFilePath);
            }
            Date beginDate = new Date();
            Date endDate = new Date(beginDate.getTime() + CertConstants.DEFAULT_VALIDITY);
            X509Certificate childCert = null;
            try {
                X509Certificate parentCertificate = CertUtils.readCrt(caPath);
                PKCS10CertificationRequest request = CertUtils.readCsr(csrPth);
                PrivateKey parentPriKey = (PrivateKey) CertUtils.readRSAKey(keyPath);
                if (parentCertificate == null || request == null || parentPriKey == null) {
                    return null;
                }
                childCert = createChildCertificate(isCaCert, CertConstants.DEFAULT_SIGNATURE_ALGORITHM,
                        parentCertificate, request, keyUsage,
                        beginDate, endDate, parentPriKey);
            } catch (Exception e) {
                LOG.error("X509CertHandler.createChildCert failed ", e);
            }
            if (exportFilePath != null) {
                CertUtils.writeCrt(childCert, exportFilePath + "/" + certName + ".crt");
                LOG.info("CA certificate save success, file path :~" + exportFilePath);
            }
            return CertUtils.readPEMAsString(childCert);
        } catch (Exception e) {
            LOG.error("generateChildCertByDefaultConf failed ,", e);
        }
        return null;
    }


    /**
     * create RootCertificate
     *
     * @param signAlg    signature algorithm,the type of the corresponding key
     * @param issuer     issuer
     * @param keyUsage   scenarios where the certificate can be used
     * @param beginDate  beginDate of the certificate
     * @param endDate    endDate of the certificate
     * @param publicKey  the public key bound by the certificate,used to decrypt the signature
     * @param privateKey the private key used for encryption to generate the signature
     * @return the generated certificate
     */
    public X509Certificate createRootCertificate(String signAlg, X500NameInfo issuer, KeyUsage keyUsage,
                                                 Date beginDate, Date endDate,
                                                 PublicKey publicKey, PrivateKey privateKey) {
        X509Certificate rootCert = null;
        try {
            rootCert = X509CertHandler.createRootCert(signAlg, new X500Name(issuer.toString()), keyUsage,
                    beginDate, endDate, publicKey, privateKey);
        } catch (Exception e) {
            LOG.error("X509CertHandler.createRootCert failed ", e);
        }
        return rootCert;
    }

    /**
     * create ChildCertificate
     *
     * @param isCaCert          root certificate mark
     * @param signAlg           signature algorithm,the type of the corresponding key
     * @param parentCertificate certificate of the issuer
     * @param request           certification request
     * @param keyUsage          scenarios where the certificate can be used
     * @param beginDate         beginDate of the certificate
     * @param endDate           endDate of the certificate
     * @param privateKey        the private key used for encryption to generate the signature
     * @return the generated certificate
     */
    public X509Certificate createChildCertificate(boolean isCaCert, String signAlg, X509Certificate parentCertificate,
                                                  PKCS10CertificationRequest request, KeyUsage keyUsage,
                                                  Date beginDate, Date endDate, PrivateKey privateKey) {
        X509Certificate childCert = null;
        try {
            childCert = X509CertHandler.createChildCert(isCaCert, signAlg, parentCertificate, request, keyUsage,
                    beginDate, endDate, privateKey);
        } catch (Exception e) {
            LOG.error("X509CertHandler.createChildCert failed ", e);
        }
        return childCert;
    }


    /**
     * create CertificationRequest
     *
     * @param subject subject of the csr
     * @param pubKey  the public key bound by the certificate,used to decrypt the signature
     * @param priKey  the private key used for encryption to generate the signature
     * @param signAlg signature algorithm,the type of the corresponding key
     * @return the certificate request
     */
    public PKCS10CertificationRequest createCertRequest(X500NameInfo subject, PublicKey pubKey, PrivateKey priKey,
                                                        String signAlg) {
        PKCS10CertificationRequest request = null;
        try {
            request = X509CertHandler.createCSR(new X500Name(subject.toString()), pubKey, priKey, signAlg);
        } catch (OperatorCreationException e) {
            LOG.error("X509CertHandler.createCSR failed ", e);
        }
        return request;
    }

    /**
     * revoke certificate
     * @param caCertificate Certificate of ca
     * @param caPrivateKey PrivateKey of ca
     * @param revokeCertificates revokeCertificates
     * @param signAlg signAlg
     * @return X509CRL
     */
    public X509CRL createCRL(X509Certificate caCertificate,
                             PrivateKey caPrivateKey,
                             List<X509Certificate> revokeCertificates,
                             String signAlg) {
        return createCRL(caCertificate,caPrivateKey,revokeCertificates,signAlg,1,null);
    }

    /**
     * revoke certificate
     * @param caCertificate Certificate of ca
     * @param caPrivateKey PrivateKey of ca
     * @param revokeCertificates revokeCertificates
     * @param signAlg signAlg
     * @param reason the reason code, as indicated in CRLReason, i.e CRLReason.keyCompromise, or 0 if not to be used.
     * @param period date of next CRL update
     * @return X509CRL
     */
    public X509CRL createCRL(X509Certificate caCertificate,
                             PrivateKey caPrivateKey,
                             List<X509Certificate> revokeCertificates,
                             String signAlg,
                             int reason,
                             Date period) {
        X509CRL x509CRL = null;
        try {
            x509CRL = X509CertHandler.revokeCert(caCertificate, caPrivateKey, revokeCertificates,signAlg, reason, period);
        } catch (OperatorCreationException | CRLException e) {
            LOG.error("X509CertHandler.createCSR failed ", e);
        }
        return x509CRL;
    }

    /**
     * verify cert
     * @param X509certificateRoot root X509Certificate
     * @param X509CertificateChain chain of X509Certificate
     * @return result of verify
     */
    public boolean verify(X509Certificate X509certificateRoot, List<X509Certificate> X509CertificateChain) {
        return verify(X509certificateRoot,X509CertificateChain,null);
    }

    /**
     * verify cert
     * @param X509certificateRoot root X509Certificate
     * @param X509CertificateChain chain of X509Certificate
     * @param X509crl certificate revocation lists
     * @return result of verify
     */
    public boolean verify(X509Certificate X509certificateRoot, List<X509Certificate> X509CertificateChain, X509CRL X509crl) {
        int nSize = X509CertificateChain.size();
        X509Certificate[] arX509certificate = new X509Certificate[nSize];
        X509CertificateChain.toArray(arX509certificate);
        List<BigInteger> list = new ArrayList<>();
        //chain validation
        Principal principalLast = null;
        for (int i = 0; i < nSize; i++) {
            X509Certificate x509Certificate = arX509certificate[i];
            Principal principalIssuer = x509Certificate.getIssuerDN();
            Principal principalSubject = x509Certificate.getSubjectDN();
            list.add(x509Certificate.getSerialNumber());

            if (principalLast != null) {
                if (principalIssuer.equals(principalLast)) {
                    try {
                        PublicKey publickey = arX509certificate[i - 1].getPublicKey();
                        arX509certificate[i].verify(publickey);
                    } catch (Exception e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            principalLast = principalSubject;
        }
        // revoke validation
        if (X509crl != null) {
            try {
                if (!X509crl.getIssuerDN().equals(X509certificateRoot.getSubjectDN()))
                    return false;
                X509crl.verify(X509certificateRoot.getPublicKey());
            } catch (Exception e) {
                return false;
            }
            try {
                Set setEntries = X509crl.getRevokedCertificates();
                if (setEntries != null && !setEntries.isEmpty()) {
                    for (Object setEntry : setEntries) {
                        X509CRLEntry X509crlentry = (X509CRLEntry) setEntry;
                        if (list.contains(X509crlentry.getSerialNumber()))
                            return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        // data validation
        try {
            PublicKey publickey = X509certificateRoot.getPublicKey();
            arX509certificate[0].verify(publickey);
        } catch (Exception e) {
            return false;
        }
        Date date = new Date();
        for (int i = 0; i < nSize; i++) {
            try {
                arX509certificate[i].checkValidity(date);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }


}
