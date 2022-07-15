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
/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.cert.toolkit.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuzhichu
 * @author wesleywang
 */
public class CertUtils {

    protected static final Logger LOG = LoggerFactory.getLogger(CertUtils.class);

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private static SubjectKeyIdentifier getSubjectKeyId(final PublicKey publicKey) throws OperatorCreationException {
        final SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        final DigestCalculator digCalc = new BcDigestCalculatorProvider()
                .get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));

        return new X509ExtensionUtils(digCalc).createSubjectKeyIdentifier(publicKeyInfo);
    }

    private static AuthorityKeyIdentifier getAuthorityKeyId(final PublicKey publicKey)
            throws OperatorCreationException {
        final SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        final DigestCalculator digCalc = new BcDigestCalculatorProvider()
                .get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));

        return new X509ExtensionUtils(digCalc).createAuthorityKeyIdentifier(publicKeyInfo);
    }

    public static PEMKeyPair readKey(String filePath) throws FileNotFoundException {
        Object object = readPEMObject(filePath);
        if (object instanceof PEMKeyPair) {
            return (PEMKeyPair) object;
        }
        return null;
    }

    public static Key readRSAKey(String filePath) throws Exception {
        Object object = readPEMObject(filePath);
        if (object instanceof PEMKeyPair) {
            return KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(((PEMKeyPair) object).getPrivateKeyInfo().getEncoded()));
        }
        return null;
    }

    public static void writeKey(Key key, String filePath) {
        writeToFile(key, filePath);
    }

    public static X509Certificate readCrt(String filePath) throws CertificateException, FileNotFoundException {
        Object object = readPEMObject(filePath);
        if (object instanceof X509CertificateHolder) {
            return new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) object);
        }
        return null;
    }

    public static X509CRL readCrl(String filePath) throws FileNotFoundException, CRLException {
        Object object = readPEMObject(filePath);
        if (object instanceof X509CRLHolder) {
            return new JcaX509CRLConverter().setProvider("BC").getCRL((X509CRLHolder) object);
        }
        return null;
    }

    public static void writeCrl(X509CRL crl, String filePath) {
        writeToFile(crl, filePath);
    }

    public static X509Certificate convertStrToCert(String crtStr) throws CertificateException {
        Object object = readStringAsPEM(crtStr);
        if (object instanceof X509CertificateHolder) {
            return new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) object);
        }
        return null;
    }

    public static PKCS10CertificationRequest convertStrToCsr(String csrStr) {
        Object object = readStringAsPEM(csrStr);
        if (object instanceof PKCS10CertificationRequest) {
            return (PKCS10CertificationRequest) object;
        }
        return null;
    }

    public static void writeCrt(X509Certificate certificate, String filePath) {
        writeToFile(certificate, filePath);
    }

    public static PKCS10CertificationRequest readCsr(String filePath) throws FileNotFoundException {
        Object object = readPEMObject(filePath);
        if (object instanceof PKCS10CertificationRequest) {
            return (PKCS10CertificationRequest) object;
        }
        return null;
    }

    public static void writeCsr(PKCS10CertificationRequest request, String filePath) {
        writeToFile(request, filePath);
    }

    public static void writeToFile(Object object, String filePath) {
        try (JcaPEMWriter pw = new JcaPEMWriter(new FileWriter(filePath))) {
            pw.writeObject(object);
        } catch (IOException e) {
            LOG.error("writeObject failed", e);
        }
    }

    public static Object readPEMObject(String filePath) throws FileNotFoundException {
        if (!FileOperationUtils.exist(filePath)) {
            throw new FileNotFoundException("filePath does't exist，path = " + filePath);
        }
        PemReader pemReader = null;
        Object object = null;
        try {
            pemReader = new PemReader(new FileReader(filePath));
            PEMParser pemParser = new PEMParser(pemReader);
            object = pemParser.readObject();
        } catch (IOException e) {
            LOG.error("readPEMObject failed", e);
        } finally {
            if (pemReader != null) {
                try {
                    pemReader.close();
                } catch (IOException e) {
                    LOG.error("pemReader.close failed", e);
                }
            }
        }
        return object;
    }

    public static String readPEMAsString(Object object) {
        String result = null;
        StringWriter writer = new StringWriter();
        JcaPEMWriter pw = new JcaPEMWriter(writer);
        try {
            pw.writeObject(object);
        } catch (IOException e) {
            LOG.error("pw.writeObject failed ", e);
        } finally {
            try {
                writer.close();
                pw.close();
            } catch (IOException e) {
                LOG.error("io close failed", e);
            }
        }
        if (writer.getBuffer() != null) {
            result = writer.getBuffer().toString();
        }
        return result;
    }

    public static Object readStringAsPEM(String string) {
        StringReader reader = new StringReader(string);
        PemReader pemReader = new PemReader(reader);
        PEMParser pemParser = new PEMParser(pemReader);
        Object object = null;
        try {
            object = pemParser.readObject();
        } catch (IOException e) {
            LOG.error("readPEMObject failed", e);
        } finally {
            try {
                reader.close();
                pemReader.close();
            } catch (IOException e) {
                LOG.error("pemReader.close failed", e);
            }
        }
        return object;
    }

    /**
     * Save the PFX file that contains the public key, private key, and certificate
     * chain alias
     *
     * @param alias         certificate chain alias
     * @param privKey       private key
     * @param pwd           password
     * @param certChain     certificate chain
     * @param saveDirectory save directory
     */
    public static void savePfx(String alias, PrivateKey privKey, String pwd, List<X509Certificate> certChain,
            String saveDirectory, String name) throws Exception {
        FileOutputStream out = null;
        try {
            KeyStore outputKeyStore = KeyStore.getInstance("pkcs12");
            outputKeyStore.load(null, pwd.toCharArray());
            X509Certificate[] arX509certificate = new X509Certificate[certChain.size()];
            outputKeyStore.setKeyEntry(alias, privKey, pwd.toCharArray(), certChain.toArray(arX509certificate));
            out = new FileOutputStream(saveDirectory + "/" + name + ".pfx");
            outputKeyStore.store(out, pwd.toCharArray());
        } finally {
            if (out != null)
                out.close();
        }
    }
    
    /**
     * Save the PFX file that contains the public key, private key, and certificate
     * chain alias
     *
     * @param alias         certificate chain alias
     * @param privKey       private key
     * @param pwd           password
     * @param certChain     certificate chain
     * @param saveDirectory save directory
     */
    public static void saveJks(String alias, PrivateKey privKey, String pwd, List<X509Certificate> certChain,
            String saveDirectory, String name) throws Exception {
        FileOutputStream out = null;
        try {
            KeyStore outputKeyStore = KeyStore.getInstance("JKS");
            outputKeyStore.load(null, pwd.toCharArray());
            X509Certificate[] arX509certificate = new X509Certificate[certChain.size()];
            outputKeyStore.setKeyEntry(alias, privKey, pwd.toCharArray(), certChain.toArray(arX509certificate));
            out = new FileOutputStream(saveDirectory + "/" + name + ".jks");
            outputKeyStore.store(out, pwd.toCharArray());
        } finally {
            if (out != null)
                out.close();
        }
    }

    /**
     * read PriKey from Pfx
     * 
     * @param filePath pfx filepath
     * @param pwd      password
     * @return PrivateKey
     */
    public static PrivateKey readPriKeyFromPfx(String filePath, String pwd) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        FileInputStream fis = new FileInputStream(filePath);
        // If the keystore password is empty(""), then we have to set
        // to null, otherwise it won't work!!!
        char[] nPassword = null;
        if ((pwd != null) && !pwd.trim().equals("")) {
            nPassword = pwd.toCharArray();
        }
        ks.load(fis, nPassword);
        fis.close();
        Enumeration<String> enum1 = ks.aliases();
        String keyAlias = null;
        if (enum1.hasMoreElements()) {
            keyAlias = enum1.nextElement();
        }
        return (PrivateKey) ks.getKey(keyAlias, nPassword);
    }
    
    /**
     * read PriKey from Pfx
     * 
     * @param filePath pfx filepath
     * @param pwd      password
     * @return PrivateKey
     */
    public static PrivateKey readPriKeyFromJks(String filePath, String pwd) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream(filePath);
        // If the keystore password is empty(""), then we have to set
        // to null, otherwise it won't work!!!
        char[] nPassword = null;
        if ((pwd != null) && !pwd.trim().equals("")) {
            nPassword = pwd.toCharArray();
        }
        ks.load(fis, nPassword);
        fis.close();
        Enumeration<String> enum1 = ks.aliases();
        String keyAlias = null;
        if (enum1.hasMoreElements()) {
            keyAlias = enum1.nextElement();
        }
        return (PrivateKey) ks.getKey(keyAlias, nPassword);
    }

    public static void importCertToTrustStore(final String alias, final X509Certificate cert, String filename, String password)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        if (StringUtils.isBlank(password)) {
            password = "0xCafebabe";
        }
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType()); // jks
        try (final InputStream is = new FileInputStream(filename)) {
            ks.load(is, password.toCharArray());
        } catch (FileNotFoundException e) {
            ks.load(null, password.toCharArray());
        }
        ks.setCertificateEntry(alias, cert);
        final File f = new File(filename);
        final File dir = f.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        try (final OutputStream os = new FileOutputStream(filename)) {
            ks.store(os, password.toCharArray());
        }
    }
}
